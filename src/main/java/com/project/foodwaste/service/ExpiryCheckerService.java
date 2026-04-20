package com.project.foodwaste.service;

import com.project.foodwaste.entity.Donation;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.repository.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ExpiryCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(ExpiryCheckerService.class);
    private final DonationRepository donationRepository;
    private final NotificationService notificationService;
    private final ExecutorService executorService;

    @Autowired
    public ExpiryCheckerService(DonationRepository donationRepository, NotificationService notificationService) {
        this.donationRepository = donationRepository;
        this.notificationService = notificationService;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    @Scheduled(fixedRate = 60000)
    @Async("taskExecutor")
    public void checkExpiredDonations() {
        logger.info("Running expiry check on thread: {}", Thread.currentThread().getName());
        List<Donation> expired = donationRepository.findExpiredDonations(LocalDate.now());

        if (expired.isEmpty()) {
            logger.debug("No expired donations found");
            return;
        }

        logger.info("Found {} expired donations, processing in parallel", expired.size());

        for (Donation donation : expired) {
            executorService.submit(() -> processExpiredDonation(donation));
        }
    }

    @Transactional
    public void processExpiredDonation(Donation donation) {
        try {
            String threadName = Thread.currentThread().getName();
            logger.info("Processing expired donation ID {} on thread {}", donation.getId(), threadName);

            donation.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(donation);

            notificationService.createNotification(
                donation.getDonor(),
                "Donation Expired",
                "Your donation '" + donation.getFoodName() + "' has expired and is no longer available.",
                "EXPIRY"
            );
        } catch (Exception e) {
            logger.error("Error processing expired donation ID {}: {}", donation.getId(), e.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
