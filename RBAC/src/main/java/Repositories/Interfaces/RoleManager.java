package Repositories.Interfaces;

import Models.Role;
import Models.Permission;
import Filters.Interfaces.RoleFilter;
import java.util.*;
import java.util.stream.Collectors;

public class RoleManager {

    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;

    public RoleManager() {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
    }

    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        rolesById.put(role.id(), role);
        rolesByName.put(role.name(), role);
    }

    public boolean remove(Role role) {
        if (role == null) return false;
        Role removed = rolesById.remove(role.id());
        if (removed != null) {
            rolesByName.remove(role.name());
            return true;
        }
        return false;
    }

    public boolean removeById(String id) {
        Role removed = rolesById.remove(id);
        if (removed != null) {
            rolesByName.remove(removed.name());
            return true;
        }
        return false;
    }

    public void update(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        rolesById.put(role.id(), role);
        rolesByName.put(role.name(), role);
    }

    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    public boolean exists(String id) {
        return rolesById.containsKey(id);
    }

    public boolean existsByName(String name) {
        return rolesByName.containsKey(name);
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (filter.test(role)) {
                result.add(role);
            }
        }
        return result;
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        return rolesById.values().parallelStream()
                .filter(role -> filter.test(role))
                .collect(Collectors.toList());
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        if (roleName == null || permission == null) {
            throw new IllegalArgumentException("Role name and permission cannot be null");
        }
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        if (roleName == null || permission == null) {
            throw new IllegalArgumentException("Role name and permission cannot be null");
        }
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (role.hasPermission(permissionName, resource)) {
                result.add(role);
            }
        }
        return result;
    }

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        // This method is for back-reference, currently does nothing
    }

    public int count() {
        return rolesById.size();
    }

    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }
}