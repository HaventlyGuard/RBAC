package Repositories.Interfaces;

import Filters.Interfaces.RoleFilter;
import Models.Permission;
import Models.Role;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements IRepository<Role> {

    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;
    private final AssignmentManager assignmentManager;

    public RoleManager() {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
        this.assignmentManager = null;
    }

    public RoleManager(AssignmentManager assignmentManager) {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
        this.assignmentManager = assignmentManager;
    }

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("assignmentManager");
            field.setAccessible(true);
            field.set(this, assignmentManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set assignment manager", e);
        }
    }

    @Override
    public void add(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");

        if (rolesById.containsKey(role.id())) {
            throw new IllegalArgumentException("Role with ID '" + role.id() + "' already exists");
        }

        if (rolesByName.containsKey(role.name())) {
            throw new IllegalArgumentException("Role with name '" + role.name() + "' already exists");
        }

        rolesById.put(role.id(), role);
        rolesByName.put(role.name(), role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }

        if (assignmentManager != null && !assignmentManager.findByRole(role).isEmpty()) {
            throw new IllegalStateException("Cannot remove role '" + role.name() +
                    "' because it is assigned to users");
        }

        Role removed = rolesById.remove(role.id());
        if (removed != null) {
            rolesByName.remove(role.name());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeById(String id) {
        Role role = rolesById.get(id);
        if (role != null) {
            return remove(role);
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    public List<Role> findByFilter(RoleFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return rolesById.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Sorter cannot be null");

        return rolesById.values().stream()
                .filter(filter)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    @Override
    public boolean exists(String id) {
        return rolesById.containsKey(id);
    }

    public boolean existsByName(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }

        Role updatedRole = role.addPermission(permission);

        rolesById.remove(role.id());
        rolesByName.remove(role.name());

        rolesById.put(updatedRole.id(), updatedRole);
        rolesByName.put(updatedRole.name(), updatedRole);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }

        Role updatedRole = role.removePermission(permission);

        rolesById.remove(role.id());
        rolesByName.remove(role.name());

        rolesById.put(updatedRole.id(), updatedRole);
        rolesByName.put(updatedRole.name(), updatedRole);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(rolesById, that.rolesById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById);
    }
}