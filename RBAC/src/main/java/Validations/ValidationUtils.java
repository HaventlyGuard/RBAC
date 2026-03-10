package Validations;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2})?$");

    private ValidationUtils() {
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null) return false;
        return DATE_PATTERN.matcher(date.trim()).matches();
    }

    public static String normalizeString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public static void validateUsername(String username) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username. Must be 3-20 characters and contain only letters, numbers, and underscore");
        }
    }

    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static void validateDate(String date) {
        if (!isValidDate(date)) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD or YYYY-MM-DD HH:MM");
        }
    }
}