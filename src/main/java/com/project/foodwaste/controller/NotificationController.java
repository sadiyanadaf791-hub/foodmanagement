package com.project.foodwaste.controller;

import com.project.foodwaste.entity.User;
import com.project.foodwaste.service.NotificationService;
import com.project.foodwaste.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String listNotifications(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("notifications", notificationService.getNotificationsForUser(user));
        return "notifications";
    }

    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }
}
