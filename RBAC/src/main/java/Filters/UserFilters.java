package Filters;

import Filters.Interfaces.UserFilter;
import Models.User;

public class UserFilters {

    private UserFilters() {}

    public static UserFilter byUsername(String username) {
        return user -> user.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        return user -> containsIgnoreCase(user.username(), substring);
    }

    public static UserFilter byEmail(String email) {
        return user -> user.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return user -> user.email().toLowerCase().endsWith(domain.toLowerCase());
    }

    public static UserFilter byFullNameContains(String substring) {
        return user -> containsIgnoreCase(user.fullName(), substring);
    }

    private static boolean containsIgnoreCase(String text, String pattern) {
        if (text == null || pattern == null) {
            return false;
        }

        String textLower = text.toLowerCase();
        String patternLower = pattern.toLowerCase();

        for (int i = 0; i <= textLower.length() - patternLower.length(); i++) {
            boolean found = true;
            for (int j = 0; j < patternLower.length(); j++) {
                if (textLower.charAt(i + j) != patternLower.charAt(j)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    public static boolean test(User user) {
        return true;
    }
}