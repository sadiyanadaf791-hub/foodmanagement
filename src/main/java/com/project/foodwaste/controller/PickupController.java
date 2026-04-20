package com.project.foodwaste.controller;

import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.PickupStatus;
import com.project.foodwaste.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pickup")
public class PickupController {

    private final PickupTrackingService pickupTrackingService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public PickupController(PickupTrackingService pickupTrackingService,
                             UserService userService, NotificationService notificationService) {
        this.pickupTrackingService = pickupTrackingService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String pickupTracking(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("pickups", pickupTrackingService.findAll());
        model.addAttribute("statuses", PickupStatus.values());
        return "pickup-tracking";
    }

    @PostMapping("/update/{id}")
    public String updateStatus(@PathVariable Long id,
                                @RequestParam String status,
                                RedirectAttributes redirectAttributes) {
        try {
            pickupTrackingService.updatePickupStatus(id, PickupStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("successMessage", "Pickup status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/pickup";
    }
}
