package com.project.foodwaste.controller;

import com.project.foodwaste.entity.User;
import com.project.foodwaste.service.NotificationService;
import com.project.foodwaste.service.SdgStatisticsService;
import com.project.foodwaste.service.UserService;
import com.project.foodwaste.socket.DonationAlertServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ApiController {

    private final DonationAlertServer alertServer;
    private final NotificationService notificationService;
    private final UserService userService;
    private final SdgStatisticsService sdgStatisticsService;

    @Autowired
    public ApiController(DonationAlertServer alertServer, NotificationService notificationService,
                          UserService userService, SdgStatisticsService sdgStatisticsService) {
        this.alertServer = alertServer;
        this.notificationService = notificationService;
        this.userService = userService;
        this.sdgStatisticsService = sdgStatisticsService;
    }

    @GetMapping("/api/socket/alerts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> response = new HashMap<>();
        response.put("connectedClients", alertServer.getConnectedClientCount());
        response.put("latestAlert", alertServer.getLatestAlert());
        response.put("recentAlerts", alertServer.getRecentAlerts());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/notifications/count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getNotificationCount(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", notificationService.countUnread(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sdg-dashboard")
    public String sdgDashboard(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("metrics", sdgStatisticsService.getSdgMetrics());
        return "sdg-dashboard";
    }
}
