package Models;

import Models.interfaces.RoleAssignment;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {

    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = generateId();
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }

    public AbstractRoleAssignment(String assignmentId, User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = Objects.requireNonNull(assignmentId, "Assignment ID cannot be null");
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }

    private String generateId() {
        return "assign_" + UUID.randomUUID().toString();
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] %s assigned to %s by %s at %s",
                assignmentType(),
                role().name(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt()));

        if (metadata().reason() != null && !metadata().reason().trim().isEmpty()) {
            sb.append(String.format("\nReason: %s", metadata().reason()));
        }

        sb.append(String.format("\nStatus: %s", isActive() ? "ACTIVE" : "INACTIVE"));

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    @Override
    public String toString() {
        return String.format("AbstractRoleAssignment{id='%s', user='%s', role='%s', type='%s', active=%s}",
                assignmentId, user.username(), role.name(), assignmentType(), isActive());
    }

}