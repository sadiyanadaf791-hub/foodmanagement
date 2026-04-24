package com.project.foodwaste.service;

import com.project.foodwaste.entity.Donation;
import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.repository.DonationRepository;
import com.project.foodwaste.socket.DonationAlertServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationAlertServer alertServer;

    @Autowired
    public DonationService(DonationRepository donationRepository,
                           DonationAlertServer alertServer) {
        this.donationRepository = donationRepository;
        this.alertServer = alertServer;
    }

    @Transactional
    public Donation createDonation(Donation donation) {
        donation.setStatus(DonationStatus.AVAILABLE);
        Donation saved = donationRepository.save(donation);

        // Broadcast real-time alert to connected NGO clients
        String alertMessage = String.format(
            "{\"type\":\"NEW_DONATION\",\"id\":%d,\"food\":\"%s\",\"qty\":%d,\"location\":\"%s\",\"donor\":\"%s\"}",
            saved.getId(), saved.getFoodName(), saved.getQuantity(),
            saved.getLocation(), saved.getDonor().getFullName()
        );
        alertServer.broadcastAlert(alertMessage);

        return saved;
    }

    public Optional<Donation> findById(Long id) {
        return donationRepository.findById(id);
    }

    public List<Donation> findByDonor(User donor) {
        return donationRepository.findByDonor(donor);
    }

    public Page<Donation> findByDonor(User donor, Pageable pageable) {
        return donationRepository.findByDonor(donor, pageable);
    }

    @Transactional
    public List<Donation> findAvailableDonations() {
        return donationRepository.findAvailableDonations();
    }

    @Transactional
    public List<Donation> getDashboardDonations() {
        return donationRepository.findDashboardDonations();
    }

    public Page<Donation> findByStatus(DonationStatus status, Pageable pageable) {
        return donationRepository.findByStatus(status, pageable);
    }

    public Page<Donation> findAll(Pageable pageable) {
        return donationRepository.findAll(pageable);
    }

    public Page<Donation> searchByFoodName(String query, Pageable pageable) {
        return donationRepository.findByFoodNameContainingIgnoreCase(query, pageable);
    }

    public Page<Donation> searchByFoodNameAndStatus(String query, DonationStatus status, Pageable pageable) {
        return donationRepository.findByFoodNameContainingIgnoreCaseAndStatus(query, status, pageable);
    }

    @Transactional
    public Donation updateDonation(Donation donation) {
        return donationRepository.save(donation);
    }

    @Transactional
    public void updateStatus(Long donationId, DonationStatus status) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));
        donation.setStatus(status);
        donationRepository.save(donation);
    }

    @Transactional
    public void deleteDonation(Long id) {
        donationRepository.deleteById(id);
    }

    public long countAll() {
        return donationRepository.count();
    }

    public long countByStatus(DonationStatus status) {
        return donationRepository.countByStatus(status);
    }

    public long sumPickedUpQuantity() {
        return donationRepository.sumPickedUpQuantity();
    }

    public long sumTotalQuantity() {
        return donationRepository.sumTotalQuantity();
    }
}
