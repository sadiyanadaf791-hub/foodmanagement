package com.project.foodwaste.repository;

import com.project.foodwaste.entity.Donation;
import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonor(User donor);

    List<Donation> findByStatus(DonationStatus status);

    Page<Donation> findByStatus(DonationStatus status, Pageable pageable);

    List<Donation> findByDonorAndStatus(User donor, DonationStatus status);

    Page<Donation> findByDonor(User donor, Pageable pageable);

    @Query("SELECT d FROM Donation d WHERE d.expiryDate <= :date AND d.status = 'AVAILABLE'")
    List<Donation> findExpiredDonations(@Param("date") LocalDate date);

    @Query("SELECT d FROM Donation d WHERE d.status = 'AVAILABLE' ORDER BY d.createdAt DESC")
    List<Donation> findAvailableDonations();

    Page<Donation> findByFoodNameContainingIgnoreCaseAndStatus(String foodName, DonationStatus status, Pageable pageable);

    Page<Donation> findByFoodNameContainingIgnoreCase(String foodName, Pageable pageable);

    Page<Donation> findByLocationContainingIgnoreCaseAndStatus(String location, DonationStatus status, Pageable pageable);

    long countByStatus(DonationStatus status);

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Donation d WHERE d.status = 'PICKED_UP'")
    long sumPickedUpQuantity();

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Donation d")
    long sumTotalQuantity();
}
