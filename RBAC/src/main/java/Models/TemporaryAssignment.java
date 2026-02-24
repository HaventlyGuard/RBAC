package Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);

        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");
        }

        try {
            LocalDateTime.parse(expiresAt, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd HH:mm", e);
        }

        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return isActive(LocalDateTime.now());
    }

    public boolean isActive(LocalDateTime now) {
        try {
            LocalDateTime expirationDateTime = LocalDateTime.parse(expiresAt, FORMATTER);
            return now.isBefore(expirationDateTime);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    // Добавленныйметод getExpiresAt()
    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("ExpiresAt cannot be null or empty");
        }
        try {
            LocalDateTime.parse(expiresAt, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd HH:mm", e);
        }
        this.expiresAt = expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public boolean isExpired() {
        return !isActive();
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("New expiration date cannot be null or empty");
        }
        try {
            LocalDateTime.parse(newExpirationDate, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd HH:mm", e);
        }
        this.expiresAt = newExpirationDate;
    }

    public String getTimeRemaining() {
        if (isExpired()) {
            return "Expired";
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiration = LocalDateTime.parse(expiresAt, FORMATTER);

            long days = ChronoUnit.DAYS.between(now, expiration);
            long hours = ChronoUnit.HOURS.between(now, expiration) % 24;
            long minutes = ChronoUnit.MINUTES.between(now, expiration) % 60;

            StringBuilder sb = new StringBuilder();
            if (days > 0) {
                sb.append(days).append(" day").append(days > 1 ? "s" : "");
            }
            if (hours > 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
            }
            if (minutes > 0 && days == 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
            }
            if (sb.length() == 0) {
                sb.append("Less than a minute");
            }

            return sb.toString();
        } catch (DateTimeParseException e) {
            return "Unknown";
        }
    }

    @Override
    public String summary() {
        String baseSummary = super.summary();
        StringBuilder sb = new StringBuilder(baseSummary);
        // Убираем последнюю строку со статусом, чтобы заменить её своей
        int lastNewLine = sb.lastIndexOf("\n");
        if (lastNewLine > 0) {
            sb.setLength(lastNewLine);
        }

        sb.append(String.format("\nExpires: %s", expiresAt));
        if (autoRenew) {
            sb.append(" (Auto-renew enabled)");
        }
        sb.append(String.format("\nTime remaining: %s", getTimeRemaining()));
        sb.append(String.format("\nStatus: %s", isActive() ? "ACTIVE" : "EXPIRED"));

        return sb.toString();
    }
}