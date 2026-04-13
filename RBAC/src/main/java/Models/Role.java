package Models;

import java.util.*;

public class Role {
    private final String id;
    private String name;
    private String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        this.id = generateId();
        setName(name);
        setDescription(description);
        this.permissions = new HashSet<>();
    }

    public Role(String id, String name, String description, Set<Permission> permissions) {
        this.id = id;
        setName(name);
        setDescription(description);
        this.permissions = new HashSet<>(permissions);
    }

    private String generateId() {
        return "role_" + UUID.randomUUID().toString();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        this.name = trimmedName;
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        String trimmedDesc = description.trim();
        if (trimmedDesc.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description = trimmedDesc;
    }

    public Role addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        permissions.add(permission);
        return this;
    }

    public Role removePermission(Permission permission) {
        permissions.remove(permission);
        return this;
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

    public Set<Permission> permissions() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}