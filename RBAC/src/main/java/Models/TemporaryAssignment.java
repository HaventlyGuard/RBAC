package Models;
import Models.AbstractRoleAssignment;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    public boolean isExpired() {
        return !isActive();
    }
}