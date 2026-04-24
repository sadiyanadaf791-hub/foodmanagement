package com.project.foodwaste.controller;

import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.RequestStatus;
import com.project.foodwaste.entity.enums.PickupStatus;
import com.project.foodwaste.service.UserService;
import com.project.foodwaste.service.DonationService;
import com.project.foodwaste.service.RequestService;
import com.project.foodwaste.service.NotificationService;
import com.project.foodwaste.repository.PickupTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ngo")
public class NgoController {

    private final UserService userService;
    private final DonationService donationService;
    private final RequestService requestService;
    private final NotificationService notificationService;
    private final PickupTrackingRepository pickupTrackingRepository;

    @Autowired
    public NgoController(UserService userService, DonationService donationService,
                         RequestService requestService, NotificationService notificationService,
                         PickupTrackingRepository pickupTrackingRepository) {
        this.userService = userService;
        this.donationService = donationService;
        this.requestService = requestService;
        this.notificationService = notificationService;
        this.pickupTrackingRepository = pickupTrackingRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        
        model.addAttribute("availableDonations", donationService.getDashboardDonations());
        var myRequests = requestService.getDashboardRequestsByNgo(user);
        model.addAttribute("myRequests", myRequests);
        model.addAttribute("totalRequests", myRequests.size());
        model.addAttribute("acceptedRequests", myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED).count());
        model.addAttribute("pendingRequests", myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING).count());
        
        var myPickups = pickupTrackingRepository.findByRequestNgo(user);
        model.addAttribute("myPickups", myPickups);
        model.addAttribute("pendingPickups", myPickups.stream()
                .filter(p -> p.getStatus() == PickupStatus.PENDING || p.getStatus() == PickupStatus.ACCEPTED || p.getStatus() == PickupStatus.PICKED).count());
        model.addAttribute("completedDeliveries", myPickups.stream()
                .filter(p -> p.getStatus() == PickupStatus.DELIVERED).count());

        return "dashboard";
    }

    @GetMapping("/donations")
    public String donations(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("availableDonations", donationService.findAvailableDonations());
        return "ngo-donations";
    }

    @GetMapping("/requests")
    public String requests(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("myRequests", requestService.findByNgo(user));
        return "ngo-requests";
    }
}
