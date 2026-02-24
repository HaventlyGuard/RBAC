package Filters;

import Models.Role;

import java.util.Comparator;

public class RoleSorters {

    private RoleSorters() {}

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::name, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(role -> role.permissions().size());
    }
}