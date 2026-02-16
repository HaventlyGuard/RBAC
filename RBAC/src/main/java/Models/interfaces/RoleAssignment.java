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
}
