package Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedBy cannot be null or empty");
        }
        if (assignedAt == null || assignedAt.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedAt cannot be null or empty");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Assigned by: %s at %s", assignedBy, assignedAt));
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(String.format("\nReason: %s", reason));
        }
        return sb.toString();
    }
}