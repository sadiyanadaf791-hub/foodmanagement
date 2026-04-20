package com.project.foodwaste.controller;

import com.project.foodwaste.entity.Donation;
import com.project.foodwaste.entity.User;
import com.project.foodwaste.entity.enums.DonationStatus;
import com.project.foodwaste.service.DonationService;
import com.project.foodwaste.service.NotificationService;
import com.project.foodwaste.service.UserService;
import com.project.foodwaste.util.FileUploadUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/donations")
public class DonationController {

    private final DonationService donationService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Autowired
    public DonationController(DonationService donationService, UserService userService,
                               NotificationService notificationService) {
        this.donationService = donationService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String listDonations(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String status,
                                Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Donation> donations;

        if (search != null && !search.isEmpty() && status != null && !status.isEmpty()) {
            donations = donationService.searchByFoodNameAndStatus(search, DonationStatus.valueOf(status), pageRequest);
        } else if (search != null && !search.isEmpty()) {
            donations = donationService.searchByFoodName(search, pageRequest);
        } else if (status != null && !status.isEmpty()) {
            donations = donationService.findByStatus(DonationStatus.valueOf(status), pageRequest);
        } else {
            donations = donationService.findAll(pageRequest);
        }

        model.addAttribute("donations", donations);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("statuses", DonationStatus.values());
        return "donations";
    }

    @GetMapping("/new")
    public String newDonationForm(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("donation", new Donation());
        return "donation-form";
    }

    @PostMapping("/new")
    public String createDonation(@Valid @ModelAttribute("donation") Donation donation,
                                  BindingResult result, Authentication auth,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        if (result.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("unreadCount", notificationService.countUnread(user));
            return "donation-form";
        }
        try {
            donation.setDonor(user);
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = FileUploadUtil.saveFile(uploadDir, imageFile);
                donation.setImagePath(filename);
            }
            donationService.createDonation(donation);
            redirectAttributes.addFlashAttribute("successMessage", "Donation created successfully!");
            return "redirect:/donations";
        } catch (Exception e) {
            model.addAttribute("currentUser", user);
            model.addAttribute("errorMessage", "Error creating donation: " + e.getMessage());
            return "donation-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editDonationForm(@PathVariable Long id, Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Donation donation = donationService.findById(id).orElseThrow();
        if (!donation.getDonor().getId().equals(user.getId())) {
            return "redirect:/donations";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("donation", donation);
        return "donation-form";
    }

    @PostMapping("/edit/{id}")
    public String updateDonation(@PathVariable Long id, @Valid @ModelAttribute("donation") Donation donation,
                                  BindingResult result, Authentication auth,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Donation existing = donationService.findById(id).orElseThrow();
        if (result.hasErrors()) {
            model.addAttribute("currentUser", user);
            return "donation-form";
        }
        try {
            existing.setFoodName(donation.getFoodName());
            existing.setDescription(donation.getDescription());
            existing.setQuantity(donation.getQuantity());
            existing.setUnit(donation.getUnit());
            existing.setExpiryDate(donation.getExpiryDate());
            existing.setLocation(donation.getLocation());
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = FileUploadUtil.saveFile(uploadDir, imageFile);
                existing.setImagePath(filename);
            }
            donationService.updateDonation(existing);
            redirectAttributes.addFlashAttribute("successMessage", "Donation updated successfully!");
            return "redirect:/donations";
        } catch (Exception e) {
            model.addAttribute("currentUser", user);
            model.addAttribute("errorMessage", "Error updating donation: " + e.getMessage());
            return "donation-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteDonation(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        Donation donation = donationService.findById(id).orElseThrow();
        if (donation.getDonor().getId().equals(user.getId())) {
            donationService.deleteDonation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Donation deleted successfully!");
        }
        return "redirect:/donations";
    }
}
