package com.project.foodwaste.repository;

import com.project.foodwaste.entity.PickupTracking;
import com.project.foodwaste.entity.Request;
import com.project.foodwaste.entity.enums.PickupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickupTrackingRepository extends JpaRepository<PickupTracking, Long> {

    Optional<PickupTracking> findByRequest(Request request);

    List<PickupTracking> findByStatus(PickupStatus status);

    long countByStatus(PickupStatus status);

    @Query("SELECT p FROM PickupTracking p JOIN FETCH p.request r JOIN FETCH r.donation d JOIN FETCH d.donor WHERE r.ngo = :ngo")
    List<PickupTracking> findByRequestNgo(@org.springframework.data.repository.query.Param("ngo") com.project.foodwaste.entity.User ngo);
}
