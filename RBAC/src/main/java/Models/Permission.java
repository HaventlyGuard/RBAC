package Models;

import java.util.Objects;

public record Permission(String name, String resource, String description) {

    public Permission {
        if (name == null) {
            throw new IllegalArgumentException("Permission name cannot be null");
        }

        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }

        for (int i = 0; i < trimmedName.length(); i++) {
            if (trimmedName.charAt(i) == ' ') {
                throw new IllegalArgumentException("Permission name cannot contain spaces");
            }
        }

        for (int i = 0; i < trimmedName.length(); i++) {
            char c = trimmedName.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_')) {
                throw new IllegalArgumentException("Permission name can only contain letters, numbers, and underscore");
            }
        }

        name = trimmedName.toUpperCase();

        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        String trimmedResource = resource.trim();
        if (trimmedResource.isEmpty()) {
            throw new IllegalArgumentException("Resource cannot be empty");
        }

        for (int i = 0; i < trimmedResource.length(); i++) {
            char c = trimmedResource.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-')) {
                throw new IllegalArgumentException("Resource can only contain letters, numbers, underscore, and hyphen");
            }
        }

        resource = trimmedResource.toLowerCase();

        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }

        String trimmedDescription = description.trim();
        if (trimmedDescription.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches;
        if (namePattern == null || namePattern.trim().isEmpty()) {
            nameMatches = true;
        } else {
            String pattern = namePattern.trim().toUpperCase();
            nameMatches = containsIgnoreCase(this.name, pattern);
        }

        boolean resourceMatches;
        if (resourcePattern == null || resourcePattern.trim().isEmpty()) {
            resourceMatches = true;
        } else {
            String pattern = resourcePattern.trim().toLowerCase();
            resourceMatches = containsIgnoreCase(this.resource, pattern);
        }

        return nameMatches && resourceMatches;
    }

    private boolean containsIgnoreCase(String text, String pattern) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resource, description);
    }
}