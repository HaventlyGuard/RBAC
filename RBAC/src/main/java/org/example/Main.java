package org.example;

import Models.Permission;
import Models.User;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println(" Test User");

        try {
            User user1 = User.validate("john_doe", "John Doe", "john@example.com");
            System.out.println("Success: " + user1.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user2 = User.validate("jo", "John Doe", "john@example.com");
            System.out.println("Success: " + user2.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user3 = User.validate("thisusernameiswaytoolong123", "John Doe", "john@example.com");
            System.out.println("Success: " + user3.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user4 = User.validate("john@doe", "John Doe", "john@example.com");
            System.out.println("Success: " + user4.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user5 = User.validate("джон_доу", "John Doe", "john@example.com");
            System.out.println("Success: " + user5.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user6 = User.validate("jane_doe", "Jane Doe", "jane.example.com");
            System.out.println("Success: " + user6.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user7 = User.validate("bob", "Bob Smith", "bob@example");
            System.out.println("Success: " + user7.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user8 = User.validate("alice", "Alice Wonder", "alice@");
            System.out.println("Success: " + user8.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user9 = User.validate("charlie", "", "charlie@example.com");
            System.out.println("Success: " + user9.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        System.out.println("Testing Permission ");

        try {
            Permission perm1 = new Permission("read", "users", "Can read user data");
            System.out.println("Success: " + perm1.format());
            System.out.println("  name normalized: " + perm1.name()); // READ
            System.out.println("  resource normalized: " + perm1.resource()); // users
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            Permission perm2 = new Permission("READ USERS", "users", "Test");
            System.out.println("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            System.out.println("Success (spaces in name): " + e.getMessage());
        }

        try {
            Permission perm3 = new Permission("WRITE", "reports", "   ");
            System.out.println("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            System.out.println("Success (empty description): " + e.getMessage());
        }

        try {
            Permission perm4 = new Permission("READ@", "users", "Test");
            System.out.println("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            System.out.println("Success (invalid characters): " + e.getMessage());
        }

        try {
            Permission perm5 = new Permission("READ", "user-profiles", "Can read user profiles");
            System.out.println("Success (resource with hyphen): " + perm5.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\nTesting matches()");
        Permission testPerm = new Permission("READ", "user_profiles", "Can read user profiles");

        System.out.println("Permission: " + testPerm.format());
        System.out.println("  matches('READ', 'user'): " + testPerm.matches("READ", "user"));
        System.out.println("  matches('read', 'user'): " + testPerm.matches("read", "user"));
        System.out.println("  matches('WRITE', 'user'): " + testPerm.matches("WRITE", "user"));
        System.out.println("  matches('READ', 'profile'): " + testPerm.matches("READ", "profile"));
        System.out.println("  matches(null, 'user'): " + testPerm.matches(null, "user"));
        System.out.println("  matches('READ', null): " + testPerm.matches("READ", null));
        System.out.println("  matches('', ''): " + testPerm.matches("", ""));
        System.out.println("  matches('RE', 'prof'): " + testPerm.matches("RE", "prof"));

        System.out.println("\nDifferent permission examples");

        Permission[] permissions = {
                new Permission("CREATE", "users", "Create new users"),
                new Permission("UPDATE", "reports", "Update existing reports"),
                new Permission("DELETE", "settings", "Delete system settings"),
                new Permission("EXPORT", "analytics", "Export analytics data"),
                new Permission("MANAGE", "user_roles", "Manage user roles and permissions")
        };

        for (Permission p : permissions) {
            System.out.println("  " + p.format());
        }

        System.out.println("\n Pattern search");
        String searchName = "MANAGE";
        String searchResource = "user";

        System.out.println("Searching: name contains '" + searchName + "', resource contains '" + searchResource + "'");
        for (Permission p : permissions) {
            if (p.matches(searchName, searchResource)) {
                System.out.println(" Found: " + p.format());
            }
        }
    }
}

