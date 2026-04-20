package com.project.foodwaste.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private DateUtil() {
        // Utility class
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "";
    }

    public static String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";

        long hours = ChronoUnit.HOURS.between(dateTime, LocalDateTime.now());
        if (hours < 24) return hours + "h ago";

        long days = ChronoUnit.DAYS.between(dateTime, LocalDateTime.now());
        if (days < 7) return days + "d ago";

        return formatDate(dateTime.toLocalDate());
    }

    public static boolean isExpired(LocalDate expiryDate) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public static long daysUntilExpiry(LocalDate expiryDate) {
        if (expiryDate == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}
