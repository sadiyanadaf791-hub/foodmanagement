package com.project.foodwaste.controller;

import com.project.foodwaste.entity.User;
import com.project.foodwaste.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public RequestController(RequestService requestService, UserService userService,
                              NotificationService notificationService) {
        this.requestService = requestService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String listRequests(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));

        switch (user.getRole()) {
            case NGO:
                model.addAttribute("requests", requestService.findByNgo(user));
                break;
            case DONOR:
                model.addAttribute("requests", requestService.findRequestsForDonor(user));
                break;
            case ADMIN:
                model.addAttribute("requests", requestService.findAll());
                break;
        }
        return "requests";
    }

    @PostMapping("/new/{donationId}")
    public String createRequest(@PathVariable Long donationId,
                                 @RequestParam(required = false) String message,
                                 Authentication auth, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        try {
            requestService.createRequest(user, donationId, message);
            redirectAttributes.addFlashAttribute("successMessage", "Request submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/donations";
    }

    @PostMapping("/accept/{id}")
    public String acceptRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.acceptRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Request accepted!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/requests";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Request rejected.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/requests";
    }
}
