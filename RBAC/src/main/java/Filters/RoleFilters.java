package Filters;

import Filters.Interfaces.RoleFilter;
import Models.Permission;

public class RoleFilters {

    private RoleFilters() {
    }

    public static RoleFilter byName(String name) {
        return role -> role.name().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        return role -> containsIgnoreCase(role.name(), substring);
    }

    public static RoleFilter hasPermission(Permission permission) {
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return role -> role.permissions().size() >= n;
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
}