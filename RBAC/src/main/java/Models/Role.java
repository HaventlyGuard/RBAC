package Models;

import java.util.*;

public record Role(String id, String name, String description, Set<Permission> permissions) {

    public Role {
        Objects.requireNonNull(id, "ID cannot be null");

        if (name == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        name = trimmedName;

        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        String trimmedDesc = description.trim();
        if (trimmedDesc.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        description = trimmedDesc;

        Objects.requireNonNull(permissions, "Permissions set cannot be null");
    }

    public Role(String name, String description) {
        this(generateId(), name, description, new HashSet<>());
    }

    private static String generateId() {
        return "role_" + UUID.randomUUID().toString();
    }

    public Role withName(String name) {
        return new Role(this.id, name, this.description, this.permissions);
    }

    public Role withDescription(String description) {
        return new Role(this.id, this.name, description, this.permissions);
    }

    public Role addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        Set<Permission> newPermissions = new HashSet<>(this.permissions);
        newPermissions.add(permission);
        return new Role(this.id, this.name, this.description, newPermissions);
    }

    public Role removePermission(Permission permission) {
        Set<Permission> newPermissions = new HashSet<>(this.permissions);
        newPermissions.remove(permission);
        return new Role(this.id, this.name, this.description, newPermissions);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        for (Permission p : permissions) {
            if (p.name().equalsIgnoreCase(permissionName) &&
                    p.resource().equalsIgnoreCase(resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role: %s [ID: %s]\n", name, id));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Permissions (%d):\n", permissions.size()));

        if (permissions.isEmpty()) {
            sb.append(" - No permissions assigned\n");
        } else {
            List<Permission> sortedPermissions = new ArrayList<>(permissions);
            sortedPermissions.sort(Comparator.comparing(Permission::name)
                    .thenComparing(Permission::resource));

            for (Permission p : sortedPermissions) {
                sb.append(" - ").append(p.format()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Role{id='%s', name='%s', permissions=%d}",
                id, name, permissions.size());
    }
}