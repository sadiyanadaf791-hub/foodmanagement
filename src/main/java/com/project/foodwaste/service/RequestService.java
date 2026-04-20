package com.project.foodwaste.service;

import com.project.foodwaste.entity.*;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.entity.enums.PickupStatus;
import com.project.foodwaste.entity.enums.RequestStatus;
import com.project.foodwaste.repository.DonationRepository;
import com.project.foodwaste.repository.RequestRepository;
import com.project.foodwaste.repository.PickupTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final DonationRepository donationRepository;
    private final PickupTrackingRepository pickupTrackingRepository;
    private final NotificationService notificationService;

    @Autowired
    public RequestService(RequestRepository requestRepository,
                          DonationRepository donationRepository,
                          PickupTrackingRepository pickupTrackingRepository,
                          NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.donationRepository = donationRepository;
        this.pickupTrackingRepository = pickupTrackingRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Request createRequest(User ngo, Long donationId, String message) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (requestRepository.existsByNgoAndDonation(ngo, donation)) {
            throw new RuntimeException("You have already requested this donation");
        }

        if (donation.getStatus() != DonationStatus.AVAILABLE) {
            throw new RuntimeException("This donation is no longer available");
        }

        Request request = new Request();
        request.setNgo(ngo);
        request.setDonation(donation);
        request.setMessage(message);
        request.setStatus(RequestStatus.PENDING);

        donation.setStatus(DonationStatus.REQUESTED);
        donationRepository.save(donation);

        Request saved = requestRepository.save(request);

        // Notify the donor
        notificationService.createNotification(
            donation.getDonor(),
            "New Food Request",
            ngo.getFullName() + " has requested your donation: " + donation.getFoodName(),
            "REQUEST"
        );

        return saved;
    }

    @Transactional
    public void acceptRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(RequestStatus.ACCEPTED);
        request.getDonation().setStatus(DonationStatus.ACCEPTED);
        requestRepository.save(request);
        donationRepository.save(request.getDonation());

        // Create pickup tracking
        PickupTracking tracking = new PickupTracking();
        tracking.setRequest(request);
        tracking.setStatus(PickupStatus.PENDING);
        pickupTrackingRepository.save(tracking);

        // Reject other pending requests for the same donation
        List<Request> otherRequests = requestRepository
                .findByDonationAndStatus(request.getDonation(), RequestStatus.PENDING);
        for (Request other : otherRequests) {
            if (!other.getId().equals(requestId)) {
                other.setStatus(RequestStatus.REJECTED);
                requestRepository.save(other);
                notificationService.createNotification(
                    other.getNgo(),
                    "Request Declined",
                    "Your request for " + other.getDonation().getFoodName() + " was declined as the donation was accepted by another NGO.",
                    "REJECTED"
                );
            }
        }

        // Notify the NGO
        notificationService.createNotification(
            request.getNgo(),
            "Request Accepted",
            "Your request for " + request.getDonation().getFoodName() + " has been accepted!",
            "ACCEPTED"
        );
    }

    @Transactional
    public void rejectRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        // Check if there are other pending requests; if not, revert donation status
        List<Request> pendingRequests = requestRepository
                .findByDonationAndStatus(request.getDonation(), RequestStatus.PENDING);
        if (pendingRequests.isEmpty()) {
            request.getDonation().setStatus(DonationStatus.AVAILABLE);
            donationRepository.save(request.getDonation());
        }

        notificationService.createNotification(
            request.getNgo(),
            "Request Declined",
            "Your request for " + request.getDonation().getFoodName() + " has been declined.",
            "REJECTED"
        );
    }

    public List<Request> findByNgo(User ngo) {
        return requestRepository.findByNgo(ngo);
    }

    public Page<Request> findByNgo(User ngo, Pageable pageable) {
        return requestRepository.findByNgo(ngo, pageable);
    }

    public List<Request> findByDonation(Donation donation) {
        return requestRepository.findByDonation(donation);
    }

    public List<Request> findRequestsForDonor(User donor) {
        return requestRepository.findRequestsForDonor(donor);
    }

    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    public long countByStatus(RequestStatus status) {
        return requestRepository.countByStatus(status);
    }

    public long countAll() {
        return requestRepository.count();
    }
}
