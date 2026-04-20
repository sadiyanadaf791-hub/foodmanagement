package com.project.foodwaste.repository;

import com.project.foodwaste.entity.Donation;
import com.project.foodwaste.entity.Request;
import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByNgo(User ngo);

    Page<Request> findByNgo(User ngo, Pageable pageable);

    List<Request> findByDonation(Donation donation);

    List<Request> findByNgoAndStatus(User ngo, RequestStatus status);

    List<Request> findByDonationAndStatus(Donation donation, RequestStatus status);

    Optional<Request> findByNgoAndDonation(User ngo, Donation donation);

    boolean existsByNgoAndDonation(User ngo, Donation donation);

    long countByStatus(RequestStatus status);

    @Query("SELECT r FROM Request r JOIN FETCH r.donation d JOIN FETCH d.donor WHERE d.donor = :donor")
    List<Request> findRequestsForDonor(@Param("donor") User donor);
}
