package org.example;

import Models.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

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
            System.out.println("  name normalized: " + perm1._name); // READ
            System.out.println("  resource normalized: " + perm1._resource); // users
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

        System.out.println("=== Testing Role Record ===\n");

        Permission readUsers = new Permission("READ", "users", "Can view user list");
        Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readReports = new Permission("READ", "reports", "Can view reports");
        Permission writeReports = new Permission("WRITE", "reports", "Can create and edit reports");

        System.out.println("Test 1: Creating a role");
        Role adminRole = new Role("Administrator", "Full system access");
        System.out.println("OK - Role created: " + adminRole);
        System.out.println("  ID: " + adminRole.id());
        System.out.println();

        System.out.println("Test 2: Adding permissions");
        adminRole = adminRole.addPermission(readUsers)
                .addPermission(writeUsers)
                .addPermission(deleteUsers)
                .addPermission(readReports)
                .addPermission(writeReports);
        System.out.println("OK - Added 5 permissions");
        System.out.println("  Current permissions count: " + adminRole.permissions().size());
        System.out.println();

        System.out.println("Test 3: Checking permissions");
        System.out.println("  hasPermission(READ, users): " +
                adminRole.hasPermission("READ", "users"));
        System.out.println("  hasPermission(WRITE, users): " +
                adminRole.hasPermission("WRITE", "users"));
        System.out.println("  hasPermission(DELETE, reports): " +
                adminRole.hasPermission("DELETE", "reports"));
        System.out.println("  hasPermission(readUsers): " +
                adminRole.hasPermission(readUsers));
        System.out.println();

        System.out.println("Test 4: Removing a permission");
        adminRole = adminRole.removePermission(deleteUsers);
        System.out.println("OK - Removed DELETE on users permission");
        System.out.println("  hasPermission(DELETE, users): " +
                adminRole.hasPermission("DELETE", "users"));
        System.out.println("  Current permissions count: " + adminRole.permissions().size());
        System.out.println();

        System.out.println("Test 5: Creating another role");
        Role viewerRole = new Role("Viewer", "Can view data only");
        viewerRole = viewerRole.addPermission(readUsers)
                .addPermission(readReports);
        System.out.println("OK - Role created: " + viewerRole);
        System.out.println("  ID: " + viewerRole.id());
        System.out.println();

        System.out.println("Test 6: Format() method output");
        System.out.println("Administrator role format:");
        System.out.println(adminRole.format());

        System.out.println("Viewer role format:");
        System.out.println(viewerRole.format());

        System.out.println("Test 7: Testing equals and hashCode");
        Role sameRole = new Role(adminRole.id(), "Different Name", "Different Desc",
                new HashSet<>());
        System.out.println("  adminRole.equals(sameRole): " + adminRole.equals(sameRole));
        System.out.println("  adminRole.hashCode() == sameRole.hashCode(): " +
                (adminRole.hashCode() == sameRole.hashCode()));

        System.out.println("  adminRole.equals(viewerRole): " +
                adminRole.equals(viewerRole));
        System.out.println();

        System.out.println("Test 8: Validation tests");
        try {
            new Role("", "Description");
            System.out.println("  FAIL - Should have thrown exception for empty name");
        } catch (IllegalArgumentException e) {
            System.out.println("  OK - Empty name validation: " + e.getMessage());
        }

        try {
            new Role("Admin", "");
            System.out.println("  FAIL - Should have thrown exception for empty description");
        } catch (IllegalArgumentException e) {
            System.out.println("  OK - Empty description validation: " + e.getMessage());
        }

        try {
            adminRole.addPermission(null);
            System.out.println("  FAIL - Should have thrown exception for null permission");
        } catch (IllegalArgumentException e) {
            System.out.println("  OK - Null permission validation: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Test 9: Unmodifiable collection test");
        try {
            System.out.println("  FAIL - Should have thrown exception for modification");
        } catch (UnsupportedOperationException e) {
            System.out.println("  OK - getPermissions() returns unmodifiable collection");
        }
        System.out.println();

        System.out.println("Test 10: Creating role with explicit ID");
        Set<Permission> savedPermissions = new HashSet<>();
        savedPermissions.add(readUsers);
        savedPermissions.add(writeUsers);

        Role restoredRole = new Role("role_restored_123", "Restored Role",
                "Role restored from storage", savedPermissions);
        System.out.println("OK - Restored role: " + restoredRole);
        System.out.println("  ID: " + restoredRole.id());
        System.out.println("  Name: " + restoredRole.name());
        System.out.println("  Permissions count: " + restoredRole.permissions().size());


        System.out.println("=== Testing AssignmentMetadata Record ===\n");

        System.out.println("Test 1: Creating metadata with reason");
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial role assignment");
        System.out.println("OK - Created with reason:");
        System.out.println(meta1.format());
        System.out.println();

        System.out.println("Test 2: Creating metadata without reason");
        AssignmentMetadata meta2 = AssignmentMetadata.now("john_doe", null);
        System.out.println("OK - Created without reason:");
        System.out.println(meta2.format());
        System.out.println();

        System.out.println("Test 3: Creating metadata with empty reason string");
        AssignmentMetadata meta3 = AssignmentMetadata.now("manager", "");
        System.out.println("OK - Created with empty reason:");
        System.out.println(meta3.format());
        System.out.println();

        System.out.println("Test 4: Creating metadata with custom date");
        AssignmentMetadata meta4 = new AssignmentMetadata("admin", "2026-02-15 10:30:00", "Custom date test");
        System.out.println("OK - Created with custom date:");
        System.out.println(meta4.format());
        System.out.println();

        System.out.println("Test 5: Validation tests");
        try {
            new AssignmentMetadata(null, "2026-02-15 10:30:00", "Test");
            System.out.println("FAIL - Should have thrown exception for null assignedBy");
        } catch (IllegalArgumentException e) {
            System.out.println("OK - Null assignedBy validation: " + e.getMessage());
        }

        try {
            new AssignmentMetadata("admin", null, "Test");
            System.out.println("FAIL - Should have thrown exception for null assignedAt");
        } catch (IllegalArgumentException e) {
            System.out.println("OK - Null assignedAt validation: " + e.getMessage());
        }

        try {
            new AssignmentMetadata("", "2026-02-15 10:30:00", "Test");
            System.out.println("FAIL - Should have thrown exception for empty assignedBy");
        } catch (IllegalArgumentException e) {
            System.out.println("OK - Empty assignedBy validation: " + e.getMessage());
        }

        try {
            new AssignmentMetadata("admin", "", "Test");
            System.out.println("FAIL - Should have thrown exception for empty assignedAt");
        } catch (IllegalArgumentException e) {
            System.out.println("OK - Empty assignedAt validation: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Test 6: Accessor methods");
        AssignmentMetadata meta5 = AssignmentMetadata.now("system", "Automated assignment");
        System.out.println("  assignedBy: " + meta5.assignedBy());
        System.out.println("  assignedAt: " + meta5.assignedAt());
        System.out.println("  reason: " + meta5.reason());
        System.out.println();

        System.out.println("Test 7: equals and hashCode");
        AssignmentMetadata meta6 = AssignmentMetadata.now("admin", "Test");
        AssignmentMetadata meta7 = new AssignmentMetadata(meta6.assignedBy(), meta6.assignedAt(), meta6.reason());
        System.out.println("  meta6.equals(meta7): " + meta6.equals(meta7));
        System.out.println("  meta6.hashCode() == meta7.hashCode(): " + (meta6.hashCode() == meta7.hashCode()));

        AssignmentMetadata meta8 = AssignmentMetadata.now("different", "Different");
        System.out.println("  meta6.equals(meta8): " + meta6.equals(meta8));
        System.out.println();

        System.out.println("Test 8: Different date formats");
        AssignmentMetadata meta9 = new AssignmentMetadata("admin", "2026-02-15", "Date only");
        AssignmentMetadata meta10 = new AssignmentMetadata("admin", "2026-02-15T10:30:00", "ISO format");
        AssignmentMetadata meta11 = new AssignmentMetadata("admin", "15/02/2026 10:30", "European format");

        System.out.println("  " + meta9.format());
        System.out.println("  " + meta10.format());
        System.out.println("  " + meta11.format());



        System.out.println(" Testing TemporaryAssignment.isActive() \n");

        User user = new User("john_doe", "John Doe", "john@example.com");
        Permission perm = new Permission("READ", "reports", "Can read reports");
        Role role = new Role("Viewer", "Can view reports");
        role = role.addPermission(perm);
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Temporary access");

        // Тест 1:Дата в будущем
        System.out.println("Test 1: Future date");
        String futureDate = LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment future = new TemporaryAssignment(user, role, metadata, futureDate, false);

        System.out.println("  Expires at: " + futureDate);
        System.out.println("  isActive(): " + future.isActive()); // Должно быть true
        System.out.println("  isExpired(): " + future.isExpired()); // Должно быть false
        System.out.println();

        System.out.println("Test 2: Past date");
        String pastDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment past = new TemporaryAssignment(user, role, metadata, pastDate, false);

        System.out.println("  Expires at: " + pastDate);
        System.out.println("  isActive(): " + past.isActive()); // Должно быть false
        System.out.println("  isExpired(): " + past.isExpired()); // Должно быть true
        System.out.println();

        System.out.println("Test 3: Exact date comparison");
        String exactDate = LocalDateTime.now().plusHours(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment exact = new TemporaryAssignment(user, role, metadata, exactDate, false);

        System.out.println("  Expires at: " + exactDate);
        System.out.println("  Now is before expiry? " + exact.isActive()); // Должно быть true
        System.out.println();

        System.out.println("Test 4: Testing with custom time");
        LocalDateTime testTime = LocalDateTime.of(2026, 2, 15, 10, 30);
        TemporaryAssignment test = new TemporaryAssignment(user, role, metadata, "2026-02-15 12:00", false);

        System.out.println("  Expires at: 2026-02-15 12:00");
        System.out.println("  Test time: " + testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("  isActive(testTime): " + test.isActive(testTime)); // true (10:30 < 12:00)

        LocalDateTime testTime2 = LocalDateTime.of(2026, 2, 15, 13, 00);
        System.out.println("  Test time: " + testTime2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("  isActive(testTime2): " + test.isActive(testTime2)); // false (13:00 > 12:00)
        System.out.println();

        System.out.println("Test 5: Boundary cases");

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment now = new TemporaryAssignment(user, role, metadata, nowStr, false);
        System.out.println("  Expires now: " + nowStr);
        System.out.println("  isActive(): " + now.isActive()); // Зависит от микросекунд

        try {
            new TemporaryAssignment(user, role, metadata, "2026/02/15", false);
            System.out.println("  FAIL - Should have thrown exception for invalid format");
        } catch (IllegalArgumentException e) {
            System.out.println("  OK - Invalid format caught: " + e.getMessage());
        }
    }
}

