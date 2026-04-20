package com.project.foodwaste.controller;

import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.entity.enums.RequestStatus;
import com.project.foodwaste.entity.enums.Role;
import com.project.foodwaste.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final DonationService donationService;
    private final RequestService requestService;
    private final NotificationService notificationService;
    private final PickupTrackingService pickupTrackingService;

    @Autowired
    public AdminController(UserService userService, DonationService donationService,
                            RequestService requestService, NotificationService notificationService,
                            PickupTrackingService pickupTrackingService) {
        this.userService = userService;
        this.donationService = donationService;
        this.requestService = requestService;
        this.notificationService = notificationService;
        this.pickupTrackingService = pickupTrackingService;
    }

    @GetMapping
    public String adminDashboard(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));

        model.addAttribute("totalUsers", userService.countTotal());
        model.addAttribute("totalDonors", userService.countByRole(Role.DONOR));
        model.addAttribute("totalNgos", userService.countByRole(Role.NGO));
        model.addAttribute("totalDonations", donationService.countAll());
        model.addAttribute("availableDonations", donationService.countByStatus(DonationStatus.AVAILABLE));
        model.addAttribute("pickedUpDonations", donationService.countByStatus(DonationStatus.PICKED_UP));
        model.addAttribute("expiredDonations", donationService.countByStatus(DonationStatus.EXPIRED));
        model.addAttribute("totalRequests", requestService.countAll());
        model.addAttribute("pendingRequests", requestService.countByStatus(RequestStatus.PENDING));
        model.addAttribute("acceptedRequests", requestService.countByStatus(RequestStatus.ACCEPTED));
        model.addAttribute("allUsers", userService.findAllUsers());
        model.addAttribute("allDonations", donationService.findAvailableDonations());

        return "admin";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated!");
        return "redirect:/admin";
    }
}
