package Repositories.Interfaces;

import Filters.Interfaces.AssignmentFilter;
import Models.Permission;
import Models.Role;
import Models.TemporaryAssignment;
import Models.User;
import Models.interfaces.RoleAssignment;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements IRepository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignmentsById;
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignmentsById = new HashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");

        User user = assignment.user();
        Role role = assignment.role();

        if (!userManager.exists(user.username())) {
            throw new IllegalArgumentException("User '" + user.username() + "' does not exist");
        }

        if (!roleManager.exists(role.id())) {
            throw new IllegalArgumentException("Role '" + role.name() + "' does not exist");
        }

        boolean hasActiveAssignment = assignmentsById.values().stream()
                .filter(a -> a.user().equals(user) && a.role().equals(role))
                .anyMatch(RoleAssignment::isActive);

        if (hasActiveAssignment) {
            throw new IllegalStateException("User '" + user.username() +
                    "' already has an active assignment for role '" +
                    role.name() + "'");
        }

        if (assignmentsById.containsKey(assignment.assignmentId())) {
            throw new IllegalArgumentException("Assignment with ID '" +
                    assignment.assignmentId() + "' already exists");
        }

        assignmentsById.put(assignment.assignmentId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        return assignmentsById.remove(assignment.assignmentId()) != null;
    }

    @Override
    public boolean removeById(String id) {
        return assignmentsById.remove(id) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignmentsById.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return assignmentsById.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Sorter cannot be null");

        return assignmentsById.values().stream()
                .filter(filter)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignmentsById.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user) && a.role().equals(role))
                .anyMatch(RoleAssignment::isActive);
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> userPermissions = getUserPermissions(user);

        for (Permission p : userPermissions) {
            if (p.name().equalsIgnoreCase(permissionName) &&
                    p.resource().equalsIgnoreCase(resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> permissions = new HashSet<>();

        List<RoleAssignment> userAssignments = findByUser(user).stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());

        for (RoleAssignment assignment : userAssignments) {
            permissions.addAll(assignment.role().permissions());
        }

        return permissions;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with ID '" + assignmentId + "' not found");
        }


    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with ID '" + assignmentId + "' not found");
        }

        if (!(assignment instanceof TemporaryAssignment temp)) {
            throw new IllegalArgumentException("Assignment is not temporary");
        }

        temp.extend(newExpirationDate);
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    @Override
    public boolean exists(String id) {
        return assignmentsById.containsKey(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentManager that = (AssignmentManager) o;
        return Objects.equals(assignmentsById, that.assignmentsById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentsById);
    }
}