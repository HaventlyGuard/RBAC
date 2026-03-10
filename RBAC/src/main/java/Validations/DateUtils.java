package Validations;

import Validations.ValidationUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATETIME_FULL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtils() {
    }

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FULL_FORMATTER);
    }

    public static String getCurrentDateTimeShort() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);
        return d1.isBefore(d2);
    }

    public static boolean isAfter(String date1, String date2) {
        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);
        return d1.isAfter(d2);
    }

    public static boolean isEqual(String date1, String date2) {
        LocalDateTime d1 = parseDate(date1);
        LocalDateTime d2 = parseDate(date2);
        return d1.isEqual(d2);
    }

    public static String addDays(String date, int days) {
        LocalDateTime dt = parseDate(date);
        return dt.plusDays(days).format(DATETIME_FORMATTER);
    }

    public static String formatRelativeTime(String date) {
        LocalDateTime target = parseDate(date);
        LocalDateTime now = LocalDateTime.now();

        if (target.isBefore(now)) {
            long days = ChronoUnit.DAYS.between(target, now);
            long hours = ChronoUnit.HOURS.between(target, now) % 24;
            long minutes = ChronoUnit.MINUTES.between(target, now) % 60;

            if (days > 0) {
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else if (hours > 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (minutes > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else {
                return "just now";
            }
        } else {
            long days = ChronoUnit.DAYS.between(now, target);
            long hours = ChronoUnit.HOURS.between(now, target) % 24;

            if (days > 0) {
                return "in " + days + " day" + (days > 1 ? "s" : "");
            } else if (hours > 0) {
                return "in " + hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return "in less than an hour";
            }
        }
    }

    public static String formatExpirationStatus(String expiresAt) {
        if (isBefore(expiresAt, getCurrentDateTimeShort())) {
            return "EXPIRED (" + formatRelativeTime(expiresAt) + ")";
        } else {
            return "Active (" + formatRelativeTime(expiresAt) + ")";
        }
    }

    private static LocalDateTime parseDate(String date) {
        try {
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }

            if (date.length() == 10) {
                return LocalDate.parse(date, DATE_FORMATTER).atStartOfDay();
            } else if (date.length() == 16) {
                return LocalDateTime.parse(date, DATETIME_FORMATTER);
            } else {
                return LocalDateTime.parse(date, DATETIME_FULL_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date);
        }
    }

    public static boolean isValidDate(String date) {
        return ValidationUtils.isValidDate(date);
    }
}