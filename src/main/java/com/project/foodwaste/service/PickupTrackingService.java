package com.project.foodwaste.service;

import com.project.foodwaste.entity.PickupTracking;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.entity.enums.PickupStatus;
import com.project.foodwaste.repository.DonationRepository;
import com.project.foodwaste.repository.PickupTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PickupTrackingService {

    private final PickupTrackingRepository pickupTrackingRepository;
    private final DonationRepository donationRepository;
    private final NotificationService notificationService;

    @Autowired
    public PickupTrackingService(PickupTrackingRepository pickupTrackingRepository,
                                  DonationRepository donationRepository,
                                  NotificationService notificationService) {
        this.pickupTrackingRepository = pickupTrackingRepository;
        this.donationRepository = donationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void updatePickupStatus(Long trackingId, PickupStatus newStatus) {
        PickupTracking tracking = pickupTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new RuntimeException("Pickup tracking not found"));

        tracking.setStatus(newStatus);

        if (newStatus == PickupStatus.PICKED_UP) {
            tracking.setCompletedTime(LocalDateTime.now());
            tracking.getRequest().getDonation().setStatus(DonationStatus.PICKED_UP);
            donationRepository.save(tracking.getRequest().getDonation());

            // Notify the donor
            notificationService.createNotification(
                tracking.getRequest().getDonation().getDonor(),
                "Pickup Completed",
                "Your donation '" + tracking.getRequest().getDonation().getFoodName()
                    + "' has been picked up by " + tracking.getRequest().getNgo().getFullName(),
                "PICKUP"
            );
        }

        pickupTrackingRepository.save(tracking);
    }

    public Optional<PickupTracking> findById(Long id) {
        return pickupTrackingRepository.findById(id);
    }

    public List<PickupTracking> findByStatus(PickupStatus status) {
        return pickupTrackingRepository.findByStatus(status);
    }

    public List<PickupTracking> findAll() {
        return pickupTrackingRepository.findAll();
    }

    public long countByStatus(PickupStatus status) {
        return pickupTrackingRepository.countByStatus(status);
    }
}
