package com.project.foodwaste;

import com.project.foodwaste.entity.Donation;
import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.entity.enums.Role;
import com.project.foodwaste.repository.DonationRepository;
import com.project.foodwaste.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;

@SpringBootApplication
public class FoodWasteApplication {

    private static final Logger logger = LoggerFactory.getLogger(FoodWasteApplication.class);

    public static void main(String[] args) {
        int port = isPortAvailable(8080) ? 8080 : 8081;
        System.setProperty("server.port", String.valueOf(port));
        SpringApplication.run(FoodWasteApplication.class, args);
        logger.info("Food Waste Management System started at http://localhost:{}", port);
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Bean
    CommandLineRunner seedData(UserRepository userRepository, DonationRepository donationRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                logger.info("Database already seeded, skipping...");
                return;
            }

            logger.info("Seeding default users and sample data...");

            User admin = new User("admin", passwordEncoder.encode("admin123"),
                    "admin@foodwaste.org", "System Administrator", Role.ADMIN);
            admin.setPhone("+1-555-0100");
            admin.setOrganizationName("FoodWaste Platform");
            admin.setAddress("100 Platform Drive, San Francisco, CA");
            userRepository.save(admin);

            User donor = new User("donor", passwordEncoder.encode("donor123"),
                    "donor@foodwaste.org", "Green Valley Restaurant", Role.DONOR);
            donor.setPhone("+1-555-0200");
            donor.setOrganizationName("Green Valley Restaurant");
            donor.setAddress("456 Oak Street, San Francisco, CA");
            userRepository.save(donor);

            User donor2 = new User("donor2", passwordEncoder.encode("donor123"),
                    "donor2@foodwaste.org", "Fresh Bakery Co.", Role.DONOR);
            donor2.setPhone("+1-555-0201");
            donor2.setOrganizationName("Fresh Bakery Co.");
            donor2.setAddress("789 Baker Lane, San Francisco, CA");
            userRepository.save(donor2);

            User ngo = new User("ngouser", passwordEncoder.encode("ngo123"),
                    "ngo@foodwaste.org", "Hunger Relief Foundation", Role.NGO);
            ngo.setPhone("+1-555-0300");
            ngo.setOrganizationName("Hunger Relief Foundation");
            ngo.setAddress("321 Help Avenue, San Francisco, CA");
            userRepository.save(ngo);

            User ngo2 = new User("ngo2", passwordEncoder.encode("ngo123"),
                    "ngo2@foodwaste.org", "Community Food Bank", Role.NGO);
            ngo2.setPhone("+1-555-0301");
            ngo2.setOrganizationName("Community Food Bank");
            ngo2.setAddress("555 Community Blvd, San Francisco, CA");
            userRepository.save(ngo2);

            // Sample donations
            Donation d1 = new Donation();
            d1.setDonor(donor);
            d1.setFoodName("Fresh Vegetable Platter");
            d1.setDescription("Assorted fresh vegetables including carrots, broccoli, and bell peppers");
            d1.setQuantity(25);
            d1.setUnit("kg");
            d1.setExpiryDate(LocalDate.now().plusDays(3));
            d1.setLocation("456 Oak Street, San Francisco");
            d1.setStatus(DonationStatus.AVAILABLE);
            donationRepository.save(d1);

            Donation d2 = new Donation();
            d2.setDonor(donor);
            d2.setFoodName("Cooked Rice & Curry");
            d2.setDescription("Freshly prepared basmati rice with mixed vegetable curry, serves 40 people");
            d2.setQuantity(15);
            d2.setUnit("kg");
            d2.setExpiryDate(LocalDate.now().plusDays(1));
            d2.setLocation("456 Oak Street, San Francisco");
            d2.setStatus(DonationStatus.AVAILABLE);
            donationRepository.save(d2);

            Donation d3 = new Donation();
            d3.setDonor(donor2);
            d3.setFoodName("Artisan Bread Loaves");
            d3.setDescription("12 freshly baked sourdough and whole wheat bread loaves");
            d3.setQuantity(8);
            d3.setUnit("kg");
            d3.setExpiryDate(LocalDate.now().plusDays(2));
            d3.setLocation("789 Baker Lane, San Francisco");
            d3.setStatus(DonationStatus.AVAILABLE);
            donationRepository.save(d3);

            Donation d4 = new Donation();
            d4.setDonor(donor2);
            d4.setFoodName("Pastries and Desserts");
            d4.setDescription("Mixed pastries including croissants, muffins, and danish");
            d4.setQuantity(5);
            d4.setUnit("kg");
            d4.setExpiryDate(LocalDate.now().plusDays(1));
            d4.setLocation("789 Baker Lane, San Francisco");
            d4.setStatus(DonationStatus.AVAILABLE);
            donationRepository.save(d4);

            logger.info("Database seeded successfully with {} users and {} donations",
                    userRepository.count(), donationRepository.count());
        };
    }
}
