package com.project.foodwaste.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test/ngo")
public class TestNgoController {

    @GetMapping("/dashboard")
    public String testDashboard(Model model) {
        model.addAttribute("name", "Test NGO");
        model.addAttribute("totalRequests", 5);
        model.addAttribute("accepted", 2);
        model.addAttribute("pending", 3);
        
        return "test-ngo-dashboard";
    }
}
