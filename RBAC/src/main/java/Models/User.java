package Models;

import java.util.regex.Pattern;

public class User {
    private String _username;
    private String _fullName;
    private String _email;

    public User(String username, String fullName, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        _username = username;
        _email = email;
        _fullName = fullName;

        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 20) {
            throw new IllegalArgumentException("Username must be between 3 and 20 characters");
        }

        for (int i = 0; i < trimmedUsername.length(); i++) {
            char c = trimmedUsername.charAt(i);
            boolean isValidChar = (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    c == '_';

            if (!isValidChar) {
                throw new IllegalArgumentException("Username can only contain latin letters, numbers, and underscore");
            }
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String trimmedEmail = email.trim();
        int atIndex = trimmedEmail.indexOf('@');
        int lastDotIndex = trimmedEmail.lastIndexOf('.');

        if (atIndex <= 0 || atIndex == trimmedEmail.length() - 1) {
            throw new IllegalArgumentException("Email must contain @ character at a valid position");
        }

        if (lastDotIndex <= atIndex + 1 || lastDotIndex == trimmedEmail.length() - 1) {
            throw new IllegalArgumentException("Email must contain a dot after @ with at least one character after it");
        }
    }
    public String username() {
        return _username;
    }

    public String fullName() {
        return _fullName;
    }

    public String email() {
        return _email;
    }

    public static User validate(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }

    public String format() {
        return String.format("%s (%s) <%s>", _username, _fullName, _email);
    }
}