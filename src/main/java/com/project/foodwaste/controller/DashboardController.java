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
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final DonationService donationService;
    private final RequestService requestService;
    private final NotificationService notificationService;

    @Autowired
    public DashboardController(UserService userService, DonationService donationService,
                                RequestService requestService, NotificationService notificationService) {
        this.userService = userService;
        this.donationService = donationService;
        this.requestService = requestService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));

        switch (user.getRole()) {
            case ADMIN:
                loadAdminDashboard(model);
                break;
            case DONOR:
                loadDonorDashboard(user, model);
                break;
            case NGO:
                loadNgoDashboard(user, model);
                break;
        }

        return "dashboard";
    }

    @GetMapping("/donor/dashboard")
    public String donorDashboard(Authentication auth, Model model) {
        return dashboard(auth, model);
    }


    private void loadAdminDashboard(Model model) {
        model.addAttribute("totalDonations", donationService.countAll());
        model.addAttribute("totalUsers", userService.countTotal());
        model.addAttribute("totalDonors", userService.countByRole(Role.DONOR));
        model.addAttribute("totalNgos", userService.countByRole(Role.NGO));
        model.addAttribute("availableDonations", donationService.countByStatus(DonationStatus.AVAILABLE));
        model.addAttribute("pickedUpDonations", donationService.countByStatus(DonationStatus.PICKED_UP));
        model.addAttribute("pendingRequests", requestService.countByStatus(RequestStatus.PENDING));
        model.addAttribute("mealsSaved", donationService.sumPickedUpQuantity() * 2);
        model.addAttribute("recentDonations", donationService.getDashboardDonations());
    }

    private void loadDonorDashboard(User user, Model model) {
        var donations = donationService.findByDonor(user);
        model.addAttribute("myDonations", donations);
        model.addAttribute("totalMyDonations", donations.size());
        model.addAttribute("activeDonations", donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.AVAILABLE).count());
        model.addAttribute("pickedUp", donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.PICKED_UP).count());
        model.addAttribute("incomingRequests", requestService.getDashboardRequestsByDonor(user));
    }

    private void loadNgoDashboard(User user, Model model) {
        model.addAttribute("availableDonations", donationService.getDashboardDonations());
        var myRequests = requestService.getDashboardRequestsByNgo(user);
        model.addAttribute("myRequests", myRequests);
        model.addAttribute("totalRequests", myRequests.size());
        model.addAttribute("acceptedRequests", myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED).count());
        model.addAttribute("pendingRequests", myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING).count());
    }
}
