package Models.interfaces;

import Models.AssignmentMetadata;
import Models.Role;
import Models.User;

public interface RoleAssignment {
    String assignmentId();
    User user();
    Role role();
    AssignmentMetadata metadata();
    boolean isActive();
    String assignmentType();
    default String summary() {
        return String.format("[%s] %s assigned to %s by %s at %s - Status: %s",
                assignmentType(),
                role().name(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt(),
                isActive() ? "ACTIVE" : "INACTIVE"
        );
    }
}
