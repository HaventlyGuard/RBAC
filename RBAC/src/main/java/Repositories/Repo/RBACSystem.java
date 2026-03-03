package Repositories.Repo;

import Models.*;
import Models.interfaces.RoleAssignment;
import Repositories.Interfaces.AssignmentManager;
import Repositories.Interfaces.RoleManager;
import Repositories.Interfaces.UserManager;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.roleManager.setAssignmentManager(assignmentManager);
        this.currentUser = "system";
    }

    public void initialize() {
        System.out.println("Initializing RBAC system with default data...");

        Permission readUsers = new Permission("READ", "users", "Can read user data");
        Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readRoles = new Permission("READ", "roles", "Can view roles");
        Permission writeRoles = new Permission("WRITE", "roles", "Can create and edit roles");
        Permission deleteRoles = new Permission("DELETE", "roles", "Can delete roles");
        Permission readReports = new Permission("READ", "reports", "Can view reports");
        Permission writeReports = new Permission("WRITE", "reports", "Can create reports");
        Permission readAssignments = new Permission("READ", "assignments", "Can view assignments");
        Permission writeAssignments = new Permission("WRITE", "assignments", "Can manage assignments");

        Role adminRole = new Role("Admin", "Full system access")
                .addPermission(readUsers)
                .addPermission(writeUsers)
                .addPermission(deleteUsers)
                .addPermission(readRoles)
                .addPermission(writeRoles)
                .addPermission(deleteRoles)
                .addPermission(readReports)
                .addPermission(writeReports)
                .addPermission(readAssignments)
                .addPermission(writeAssignments);

        Role managerRole = new Role("Manager", "Can manage users and view reports")
                .addPermission(readUsers)
                .addPermission(writeUsers)
                .addPermission(readRoles)
                .addPermission(readReports)
                .addPermission(writeReports)
                .addPermission(readAssignments);

        Role viewerRole = new Role("Viewer", "Can only view data")
                .addPermission(readUsers)
                .addPermission(readRoles)
                .addPermission(readReports)
                .addPermission(readAssignments);

        Role reporterRole = new Role("Reporter", "Can create and view reports")
                .addPermission(readReports)
                .addPermission(writeReports);

        // Add roles to manager
        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);
        roleManager.add(reporterRole);

        // Create admin user
        User admin = User.validate("admin", "System Administrator", "admin@rbac.local");
        userManager.add(admin);

        // Create test users
        User john = User.validate("john_doe", "John Doe", "john@example.com");
        User jane = User.validate("jane_smith", "Jane Smith", "jane@company.com");
        User bob = User.validate("bob_wilson", "Bob Wilson", "bob@example.com");

        userManager.add(john);
        userManager.add(jane);
        userManager.add(bob);


        AssignmentMetadata reporterMeta = AssignmentMetadata.now("system", "Temporary reporter access");
        String expiresAt = LocalDateTime.now().plusMonths(1).format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment reporterAssignment = new TemporaryAssignment(bob, reporterRole, reporterMeta, expiresAt, false);
        assignmentManager.add(reporterAssignment);

        setCurrentUser("admin");
        System.out.println("Initialization complete. Logged in as: admin");
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        if (userManager.exists(username)) {
            this.currentUser = username;
        } else {
            throw new IllegalArgumentException("User '" + username + "' does not exist");
        }
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n RBAC System Statistics \n\n");

        // Basic counts
        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int assignmentCount = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        sb.append(String.format("Users: %d\n", userCount));
        sb.append(String.format("Roles: %d\n", roleCount));
        sb.append(String.format("Assignments: %d total\n", assignmentCount));
        sb.append(String.format("  - Active: %d\n", activeAssignments));
        sb.append(String.format("  - Expired/Inactive: %d\n\n", expiredAssignments));

        // Average roles per user
        if (userCount > 0) {
            Map<User, List<RoleAssignment>> userAssignments = new HashMap<>();
            for (User user : userManager.findAll()) {
                userAssignments.put(user, assignmentManager.findByUser(user));
            }

            double avgRoles = userAssignments.values().stream()
                    .mapToInt(List::size)
                    .average()
                    .orElse(0);
            sb.append(String.format("Average roles per user: %.2f\n\n", avgRoles));
        }

        sb.append("Top 5 most assigned roles:\n");
        Map<Role, Long> roleCounts = assignmentManager.findAll().stream()
                .collect(Collectors.groupingBy(
                        RoleAssignment::role,
                        Collectors.counting()
                ));

        roleCounts.entrySet().stream()
                .sorted(Map.Entry.<Role, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry ->
                        sb.append(String.format("  - %s: %d assignments\n",
                                entry.getKey().name(), entry.getValue()))
                );

        sb.append("\nPermission distribution:\n");
        Map<String, Long> permissionCounts = new HashMap<>();
        for (Role role : roleManager.findAll()) {
            for (Permission perm : role.permissions()) {
                String key = perm.name() + " on " + perm.resource();
                permissionCounts.put(key,
                        permissionCounts.getOrDefault(key, 0L) + 1);
            }
        }

        permissionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry ->
                        sb.append(String.format("  - %s: used in %d roles\n",
                                entry.getKey(), entry.getValue()))
                );

        return sb.toString();
    }
}