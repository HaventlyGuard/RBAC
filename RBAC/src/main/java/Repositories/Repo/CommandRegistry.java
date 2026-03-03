package Repositories.Repo;


import Filters.*;
import Filters.Interfaces.AssignmentFilter;
import Filters.Interfaces.RoleFilter;
import Filters.Interfaces.UserFilter;
import Models.*;
import Models.interfaces.RoleAssignment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    private final CommandParser parser;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CommandRegistry(CommandParser parser) {
        this.parser = parser;
        registerAllCommands();
    }

    private void registerAllCommands() {
        registerUserCommands();
        registerRoleCommands();
        registerAssignmentCommands();
        registerPermissionCommands();
        registerSearchCommands();
        registerSystemCommands();
    }

    private void registerUserCommands() {
        parser.registerCommand("user-list", "List all users (optionally with filters)", (scanner, system) -> {
            System.out.println("\n=== User List ===\n");

            List<User> users = system.getUserManager().findAll();

            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }

            printUserTable(users);

            System.out.printf("\nTotal: %d users\n", users.size());
        });

        parser.registerCommand("user-create", "Create a new user", (scanner, system) -> {
            System.out.println("\n=== Create New User ===\n");

            try {
                System.out.print("Enter username (3-20 chars, letters, numbers, underscore): ");
                String username = scanner.nextLine().trim();

                System.out.print("Enter full name: ");
                String fullName = scanner.nextLine().trim();

                System.out.print("Enter email: ");
                String email = scanner.nextLine().trim();

                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);

                System.out.println("\n✓ User created successfully!");
                System.out.println("  " + user.format());

            } catch (IllegalArgumentException e) {
                System.out.println("\n✗ Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View detailed user information", (scanner, system) -> {
            System.out.println("\n=== View User ===\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();
            System.out.println("\n" + "=".repeat(60));
            System.out.println("USER INFORMATION");
            System.out.println("=".repeat(60));
            System.out.println("Username:    " + user.username());
            System.out.println("Full Name:   " + user.fullName());
            System.out.println("Email:       " + user.email());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            System.out.println("\n" + "=".repeat(60));
            System.out.println("ASSIGNED ROLES (" + assignments.size() + ")");
            System.out.println("=".repeat(60));

            if (assignments.isEmpty()) {
                System.out.println("No roles assigned.");
            } else {
                for (int i = 0; i < assignments.size(); i++) {
                    RoleAssignment ra = assignments.get(i);
                    System.out.printf("\n%d. %s\n", i + 1, ra.summary());
                }
            }

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            System.out.println("\n" + "=".repeat(60));
            System.out.println("EFFECTIVE PERMISSIONS (" + permissions.size() + ")");
            System.out.println("=".repeat(60));

            if (permissions.isEmpty()) {
                System.out.println("No permissions.");
            } else {
                Map<String, List<Permission>> byResource = permissions.stream()
                        .collect(Collectors.groupingBy(Permission::resource));

                byResource.forEach((resource, perms) -> {
                    System.out.println("\n" + resource.toUpperCase() + ":");
                    perms.forEach(p -> System.out.println("  - " + p.format()));
                });
            }
        });

        parser.registerCommand("user-update", "Update user information", (scanner, system) -> {
            System.out.println("\n=== Update User ===\n");

            System.out.print("Enter username to update: ");
            String username = scanner.nextLine().trim();

            if (!system.getUserManager().exists(username)) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            try {
                System.out.print("Enter new full name (Enter to keep current): ");
                String newFullName = scanner.nextLine().trim();

                System.out.print("Enter new email (Enter to keep current): ");
                String newEmail = scanner.nextLine().trim();

                User current = system.getUserManager().findByUsername(username).get();

                if (newFullName.isEmpty()) newFullName = current.fullName();
                if (newEmail.isEmpty()) newEmail = current.email();

                system.getUserManager().update(username, newFullName, newEmail);
                System.out.println("\n✓ User updated successfully!");

            } catch (IllegalArgumentException e) {
                System.out.println("\n✗ Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete a user", (scanner, system) -> {
            System.out.println("\n=== Delete User ===\n");

            System.out.print("Enter username to delete: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("\n⚠ This user has " + assignments.size() + " active assignments:");
                assignments.forEach(a -> System.out.println("  - " + a.summary()));
            }

            System.out.print("\nAre you sure you want to delete this user? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                for (RoleAssignment ra : assignments) {
                    system.getAssignmentManager().remove(ra);
                }

                system.getUserManager().removeById(username);
                System.out.println("\n✓ User deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
        });

        parser.registerCommand("user-search", "Search users by filters", (scanner, system) -> {
            System.out.println("\n=== Search Users ===\n");

            System.out.println("Select filter type:");
            System.out.println("1. Username contains");
            System.out.println("2. Email contains");
            System.out.println("3. Email domain");
            System.out.println("4. Full name contains");
            System.out.println("5. Show all users");

            System.out.print("\nChoice: ");
            String choice = scanner.nextLine().trim();

            UserFilter filter = null;
            String filterDesc = "";

            switch (choice) {
                case "1":
                    System.out.print("Enter username substring: ");
                    String usernameSub = scanner.nextLine().trim();
                    filter = UserFilters.byUsernameContains(usernameSub);
                    filterDesc = "username contains '" + usernameSub + "'";
                    break;
                case "2":
                    System.out.print("Enter email substring: ");
                    String emailSub = scanner.nextLine().trim();
                    filter = user -> user.email().toLowerCase().contains(emailSub.toLowerCase());
                    filterDesc = "email contains '" + emailSub + "'";
                    break;
                case "3":
                    System.out.print("Enter email domain (e.g., @company.com): ");
                    String domain = scanner.nextLine().trim();
                    filter = UserFilters.byEmailDomain(domain);
                    filterDesc = "email domain '" + domain + "'";
                    break;
                case "4":
                    System.out.print("Enter full name substring: ");
                    String nameSub = scanner.nextLine().trim();
                    filter = UserFilters.byFullNameContains(nameSub);
                    filterDesc = "full name contains '" + nameSub + "'";
                    break;
                case "5":
                    filter = user -> true;
                    filterDesc = "all users";
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            List<User> results = system.getUserManager().findByFilter(filter);

            System.out.println("\n=== Search Results: " + filterDesc + " ===\n");

            if (results.isEmpty()) {
                System.out.println("No users found.");
            } else {
                printUserTable(results);
                System.out.printf("\nFound: %d users\n", results.size());
            }
        });
    }

    private void registerRoleCommands() {
        parser.registerCommand("role-list", "List all roles", (scanner, system) -> {
            System.out.println("\n=== Role List ===\n");

            List<Role> roles = system.getRoleManager().findAll();

            if (roles.isEmpty()) {
                System.out.println("No roles found.");
                return;
            }

            roles.sort(RoleSorters.byName());

            System.out.printf("%-30s %-10s %-20s %s\n",
                    "NAME", "PERMISSIONS", "ID", "DESCRIPTION");
            System.out.println("-".repeat(80));

            for (Role role : roles) {
                System.out.printf("%-30s %-10d %-20s %s\n",
                        role.name(),
                        role.permissions().size(),
                        role.id().substring(0, Math.min(8, role.id().length())) + "...",
                        role.description().length() > 30 ?
                                role.description().substring(0, 27) + "..." :
                                role.description());
            }

            System.out.printf("\nTotal: %d roles\n", roles.size());
        });

        parser.registerCommand("role-create", "Create a new role", (scanner, system) -> {
            System.out.println("\n=== Create New Role ===\n");

            try {
                System.out.print("Enter role name: ");
                String name = scanner.nextLine().trim();

                System.out.print("Enter role description: ");
                String description = scanner.nextLine().trim();

                if (system.getRoleManager().existsByName(name)) {
                    System.out.println("✗ Role with name '" + name + "' already exists");
                    return;
                }

                Role role = new Role(name, description);
                system.getRoleManager().add(role);

                System.out.println("\n✓ Role created successfully!");
                System.out.println("  ID: " + role.id());

                boolean addingPermissions = true;
                while (addingPermissions) {
                    System.out.print("\nAdd permission? (yes/no): ");
                    String addMore = scanner.nextLine().trim().toLowerCase();

                    if (addMore.equals("yes") || addMore.equals("y")) {
                        System.out.print("Enter permission name (e.g., READ): ");
                        String permName = scanner.nextLine().trim();

                        System.out.print("Enter resource (e.g., users): ");
                        String resource = scanner.nextLine().trim();

                        System.out.print("Enter description: ");
                        String permDesc = scanner.nextLine().trim();

                        try {
                            Permission perm = new Permission(permName, resource, permDesc);
                            system.getRoleManager().addPermissionToRole(name, perm);
                            System.out.println("  ✓ Permission added: " + perm.format());
                        } catch (IllegalArgumentException e) {
                            System.out.println("  ✗ Error: " + e.getMessage());
                        }
                    } else {
                        addingPermissions = false;
                    }
                }

            } catch (IllegalArgumentException e) {
                System.out.println("\n✗ Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "View detailed role information", (scanner, system) -> {
            System.out.println("\n=== View Role ===\n");

            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("✗ Role not found: " + name);
                return;
            }

            Role role = roleOpt.get();
            System.out.println("\n" + role.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("Users with this role (" + assignments.size() + "):");
                assignments.stream()
                        .filter(RoleAssignment::isActive)
                        .forEach(a -> System.out.println("  - " + a.user().username()));
            }
        });

        parser.registerCommand("role-update", "Update role name or description", (scanner, system) -> {
            System.out.println("\n=== Update Role ===\n");

            System.out.print("Enter role name to update: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("✗ Role not found: " + name);
                return;
            }

            Role current = roleOpt.get();

            System.out.println("Current name: " + current.name());
            System.out.print("Enter new name (Enter to keep current): ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) newName = current.name();

            System.out.println("Current description: " + current.description());
            System.out.print("Enter new description (Enter to keep current): ");
            String newDesc = scanner.nextLine().trim();
            if (newDesc.isEmpty()) newDesc = current.description();

            try {
                Role updatedRole = new Role(current.id(), newName, newDesc, current.permissions());

                system.getRoleManager().remove(current);
                system.getRoleManager().add(updatedRole);

                System.out.println("\n✓ Role updated successfully!");

            } catch (IllegalArgumentException e) {
                System.out.println("\n✗ Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Delete a role", (scanner, system) -> {
            System.out.println("\n=== Delete Role ===\n");

            System.out.print("Enter role name to delete: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("✗ Role not found: " + name);
                return;
            }

            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("\n⚠ This role is assigned to " + assignments.size() + " users:");
                assignments.stream()
                        .filter(RoleAssignment::isActive)
                        .forEach(a -> System.out.println("  - " + a.user().username()));

                System.out.print("\nAre you sure you want to delete this role? (yes/no): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (!confirm.equals("yes") && !confirm.equals("y")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }

                for (RoleAssignment ra : assignments) {
                    system.getAssignmentManager().remove(ra);
                }
            }

            system.getRoleManager().removeById(role.id());
            System.out.println("\n✓ Role deleted successfully!");
        });

        parser.registerCommand("role-add-permission", "Add a permission to a role", (scanner, system) -> {
            System.out.println("\n=== Add Permission to Role ===\n");

            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();

            if (!system.getRoleManager().existsByName(roleName)) {
                System.out.println("✗ Role not found: " + roleName);
                return;
            }

            try {
                System.out.print("Enter permission name (e.g., READ): ");
                String permName = scanner.nextLine().trim();

                System.out.print("Enter resource (e.g., users): ");
                String resource = scanner.nextLine().trim();

                System.out.print("Enter description: ");
                String description = scanner.nextLine().trim();

                Permission permission = new Permission(permName, resource, description);
                system.getRoleManager().addPermissionToRole(roleName, permission);

                System.out.println("\n✓ Permission added to role '" + roleName + "'");
                System.out.println("  " + permission.format());

            } catch (IllegalArgumentException e) {
                System.out.println("\n✗ Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Remove a permission from a role", (scanner, system) -> {
            System.out.println("\n=== Remove Permission from Role ===\n");

            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("✗ Role not found: " + roleName);
                return;
            }

            Role role = roleOpt.get();
            List<Permission> permissions = new ArrayList<>(role.permissions());

            if (permissions.isEmpty()) {
                System.out.println("This role has no permissions.");
                return;
            }

            System.out.println("\nPermissions for role '" + roleName + "':");
            for (int i = 0; i < permissions.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, permissions.get(i).format());
            }

            System.out.print("\nEnter number of permission to remove (0 to cancel): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    System.out.println("Cancelled.");
                    return;
                }

                if (choice < 1 || choice > permissions.size()) {
                    System.out.println("✗ Invalid choice.");
                    return;
                }

                Permission toRemove = permissions.get(choice - 1);
                system.getRoleManager().removePermissionFromRole(roleName, toRemove);

                System.out.println("\n✓ Permission removed successfully!");

            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input.");
            }
        });

        parser.registerCommand("role-search", "Search roles by filters", (scanner, system) -> {
            System.out.println("\n=== Search Roles ===\n");

            System.out.println("Select filter type:");
            System.out.println("1. Name contains");
            System.out.println("2. Has specific permission");
            System.out.println("3. Has at least N permissions");
            System.out.println("4. Show all roles");

            System.out.print("\nChoice: ");
            String choice = scanner.nextLine().trim();

            RoleFilter filter = null;
            String filterDesc = "";

            switch (choice) {
                case "1":
                    System.out.print("Enter name substring: ");
                    String nameSub = scanner.nextLine().trim();
                    filter = RoleFilters.byNameContains(nameSub);
                    filterDesc = "name contains '" + nameSub + "'";
                    break;
                case "2":
                    System.out.print("Enter permission name: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Enter resource: ");
                    String resource = scanner.nextLine().trim();
                    filter = RoleFilters.hasPermission(permName, resource);
                    filterDesc = "has permission " + permName + " on " + resource;
                    break;
                case "3":
                    System.out.print("Enter minimum number of permissions: ");
                    try {
                        int n = Integer.parseInt(scanner.nextLine().trim());
                        filter = RoleFilters.hasAtLeastNPermissions(n);
                        filterDesc = "has at least " + n + " permissions";
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                        return;
                    }
                    break;
                case "4":
                    filter = role -> true;
                    filterDesc = "all roles";
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            List<Role> results = system.getRoleManager().findByFilter(filter);

            System.out.println("\n=== Search Results: " + filterDesc + " ===\n");

            if (results.isEmpty()) {
                System.out.println("No roles found.");
            } else {
                results.sort(RoleSorters.byName());
                for (Role role : results) {
                    System.out.printf("  %s (%d permissions)\n",
                            role.name(), role.permissions().size());
                }
                System.out.printf("\nFound: %d roles\n", results.size());
            }
        });
    }

    private void registerAssignmentCommands() {
        parser.registerCommand("assign-role", "Assign a role to a user", (scanner, system) -> {
            System.out.println("\n=== Assign Role to User ===\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles available to assign.");
                return;
            }

            System.out.println("\nAvailable roles:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.printf("%d. %s - %s\n", i + 1, roles.get(i).name(), roles.get(i).description());
            }

            System.out.print("\nSelect role number: ");
            try {
                int roleChoice = Integer.parseInt(scanner.nextLine().trim());
                if (roleChoice < 1 || roleChoice > roles.size()) {
                    System.out.println("✗ Invalid choice.");
                    return;
                }

                Role selectedRole = roles.get(roleChoice - 1);

                if (system.getAssignmentManager().userHasRole(user, selectedRole)) {
                    System.out.println("⚠ User already has this role assigned.");
                    System.out.print("Assign anyway? (yes/no): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (!confirm.equals("yes") && !confirm.equals("y")) {
                        System.out.println("Assignment cancelled.");
                        return;
                    }
                }

                System.out.print("\nAssignment type (permanent/temporary): ");
                String type = scanner.nextLine().trim().toLowerCase();

                System.out.print("Enter reason for assignment: ");
                String reason = scanner.nextLine().trim();

                AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);


                    System.out.print("Enter expiration date (yyyy-MM-dd HH:mm): ");
                    String expiresAt = scanner.nextLine().trim();

                    try {
                        LocalDateTime.parse(expiresAt, dateFormatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("✗ Invalid date format. Using default: +30 days");
                        expiresAt = LocalDateTime.now().plusDays(30).format(dateFormatter);
                    }

                    System.out.print("Auto-renew? (yes/no): ");
                    boolean autoRenew = scanner.nextLine().trim().toLowerCase().startsWith("y");

                    TemporaryAssignment assignment = new TemporaryAssignment(
                            user, selectedRole, metadata, expiresAt, autoRenew);
                    system.getAssignmentManager().add(assignment);

                    System.out.println("\n✓ Temporary role assigned successfully!");
                    System.out.println(assignment.summary());



            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input.");
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Revoke a role from a user", (scanner, system) -> {
            System.out.println("\n=== Revoke Role from User ===\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());

            if (assignments.isEmpty()) {
                System.out.println("User has no active role assignments.");
                return;
            }

            System.out.println("\nActive assignments for " + username + ":");
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment ra = assignments.get(i);
                System.out.printf("%d. %s - %s (since %s)\n",
                        i + 1,
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.metadata().assignedAt());
            }

            System.out.print("\nSelect assignment to revoke (0 to cancel): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    System.out.println("Cancelled.");
                    return;
                }

                if (choice < 1 || choice > assignments.size()) {
                    System.out.println("✗ Invalid choice.");
                    return;
                }

                RoleAssignment toRevoke = assignments.get(choice - 1);

                System.out.print("Are you sure? (yes/no): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (confirm.equals("yes") || confirm.equals("y")) {
                    system.getAssignmentManager().revokeAssignment(toRevoke.assignmentId());
                    System.out.println("\n✓ Role revoked successfully!");
                } else {
                    System.out.println("Revocation cancelled.");
                }

            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input.");
            }
        });

        parser.registerCommand("assignment-list", "List all assignments", (scanner, system) -> {
            System.out.println("\n=== All Assignments ===\n");

            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();

            if (assignments.isEmpty()) {
                System.out.println("No assignments found.");
                return;
            }

            assignments.sort(AssignmentSorters.byAssignmentDate());

            System.out.printf("%-15s %-15s %-10s %-8s %-20s\n",
                    "USER", "ROLE", "TYPE", "STATUS", "ASSIGNED AT");
            System.out.println("-".repeat(75));

            for (RoleAssignment ra : assignments) {
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.printf("%-15s %-15s %-10s %-8s %-20s\n",
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType(),
                        status,
                        ra.metadata().assignedAt());
            }

            System.out.printf("\nTotal: %d assignments\n", assignments.size());
        });

        parser.registerCommand("assignment-list-user", "List assignments for a specific user", (scanner, system) -> {
            System.out.println("\n=== User Assignments ===\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);

            if (assignments.isEmpty()) {
                System.out.println("User has no assignments.");
                return;
            }

            System.out.println("\nAssignments for " + username + ":");
            for (RoleAssignment ra : assignments) {
                System.out.println("\n" + ra.summary());
            }

            long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();
            System.out.printf("\nTotal: %d (%d active, %d inactive)\n",
                    assignments.size(), activeCount, assignments.size() - activeCount);
        });

        parser.registerCommand("assignment-list-role", "List users with a specific role", (scanner, system) -> {
            System.out.println("\n=== Role Assignments ===\n");

            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("✗ Role not found: " + roleName);
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);

            if (assignments.isEmpty()) {
                System.out.println("No users have this role assigned.");
                return;
            }

            System.out.println("\nUsers with role '" + roleName + "':");
            System.out.printf("%-15s %-10s %-20s\n", "USERNAME", "STATUS", "ASSIGNED AT");
            System.out.println("-".repeat(50));

            for (RoleAssignment ra : assignments) {
                System.out.printf("%-15s %-10s %-20s\n",
                        ra.user().username(),
                        ra.isActive() ? "ACTIVE" : "INACTIVE",
                        ra.metadata().assignedAt());
            }

            long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();
            System.out.printf("\nTotal: %d users (%d active)\n", assignments.size(), activeCount);
        });

        parser.registerCommand("assignment-active", "List all active assignments", (scanner, system) -> {
            System.out.println("\n=== Active Assignments ===\n");

            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();

            if (active.isEmpty()) {
                System.out.println("No active assignments.");
                return;
            }

            System.out.printf("%-15s %-15s %-10s %-20s\n",
                    "USER", "ROLE", "TYPE", "ASSIGNED AT");
            System.out.println("-".repeat(65));

            for (RoleAssignment ra : active) {
                System.out.printf("%-15s %-15s %-10s %-20s\n",
                        ra.user().username(),
                        ra.role().name(),
                        ra.assignmentType(),
                        ra.metadata().assignedAt());
            }

            System.out.printf("\nTotal: %d active assignments\n", active.size());
        });

        parser.registerCommand("assignment-expired", "List all expired assignments", (scanner, system) -> {
            System.out.println("\n=== Expired Assignments ===\n");

            List<RoleAssignment> all = system.getAssignmentManager().findAll();
            List<RoleAssignment> expired = all.stream()
                    .filter(ra -> !ra.isActive())
                    .filter(ra -> ra instanceof TemporaryAssignment)
                    .collect(Collectors.toList());

            if (expired.isEmpty()) {
                System.out.println("No expired assignments.");
                return;
            }

            System.out.printf("%-15s %-15s %-20s\n",
                    "USER", "ROLE", "EXPIRED AT");
            System.out.println("-".repeat(55));

            for (RoleAssignment ra : expired) {
                TemporaryAssignment temp = (TemporaryAssignment) ra;
                System.out.printf("%-15s %-15s %-20s\n",
                        ra.user().username(),
                        ra.role().name(),
                        temp.getExpiresAt());
            }

            System.out.printf("\nTotal: %d expired assignments\n", expired.size());
        });

        parser.registerCommand("assignment-extend", "Extend a temporary assignment", (scanner, system) -> {
            System.out.println("\n=== Extend Temporary Assignment ===\n");

            System.out.print("Enter assignment ID (or username): ");
            String input = scanner.nextLine().trim();

            List<RoleAssignment> assignments;

            if (input.contains("-")) {
                Optional<RoleAssignment> opt = system.getAssignmentManager().findById(input);
                assignments = opt.map(List::of).orElse(List.of());
            } else {
                Optional<User> userOpt = system.getUserManager().findByUsername(input);
                if (userOpt.isEmpty()) {
                    System.out.println("✗ User not found: " + input);
                    return;
                }
                assignments = system.getAssignmentManager().findByUser(userOpt.get()).stream()
                        .filter(ra -> ra instanceof TemporaryAssignment)
                        .filter(RoleAssignment::isActive)
                        .collect(Collectors.toList());
            }

            if (assignments.isEmpty()) {
                System.out.println("No active temporary assignments found.");
                return;
            }

            TemporaryAssignment toExtend;
            if (assignments.size() == 1) {
                toExtend = (TemporaryAssignment) assignments.get(0);
                System.out.println("Found assignment: " + toExtend.summary());
            } else {
                System.out.println("\nMultiple temporary assignments found:");
                for (int i = 0; i < assignments.size(); i++) {
                    TemporaryAssignment ta = (TemporaryAssignment) assignments.get(i);
                    System.out.printf("%d. %s (expires: %s)\n",
                            i + 1, ta.role().name(), ta.getExpiresAt());
                }

                System.out.print("\nSelect assignment: ");
                try {
                    int choice = Integer.parseInt(scanner.nextLine().trim());
                    if (choice < 1 || choice > assignments.size()) {
                        System.out.println("✗ Invalid choice.");
                        return;
                    }
                    toExtend = (TemporaryAssignment) assignments.get(choice - 1);
                } catch (NumberFormatException e) {
                    System.out.println("✗ Invalid input.");
                    return;
                }
            }

            System.out.print("Enter new expiration date (yyyy-MM-dd HH:mm): ");
            String newDate = scanner.nextLine().trim();

            try {
                LocalDateTime.parse(newDate, dateFormatter);
                toExtend.extend(newDate);
                System.out.println("\n✓ Assignment extended successfully!");
                System.out.println(toExtend.summary());
            } catch (DateTimeParseException e) {
                System.out.println("✗ Invalid date format.");
            }
        });

        parser.registerCommand("assignment-search", "Search assignments by filters", (scanner, system) -> {
            System.out.println("\n=== Search Assignments ===\n");

            System.out.println("Select filter type:");
            System.out.println("1. By username");
            System.out.println("2. By role name");
            System.out.println("3. By type (permanent/temporary)");
            System.out.println("4. Active only");
            System.out.println("5. Inactive only");
            System.out.println("6. Assigned after date");
            System.out.println("7. Expiring before date");
            System.out.println("8. Show all assignments");

            System.out.print("\nChoice: ");
            String choice = scanner.nextLine().trim();

            AssignmentFilter filter = null;
            String filterDesc = "";

            switch (choice) {
                case "1":
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine().trim();
                    filter = AssignmentFilters.byUsername(username);
                    filterDesc = "user '" + username + "'";
                    break;
                case "2":
                    System.out.print("Enter role name: ");
                    String roleName = scanner.nextLine().trim();
                    filter = AssignmentFilters.byRoleName(roleName);
                    filterDesc = "role '" + roleName + "'";
                    break;
                case "3":
                    System.out.print("Enter type (permanent/temporary): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    filter = AssignmentFilters.byType(type);
                    filterDesc = "type '" + type + "'";
                    break;
                case "4":
                    filter = AssignmentFilters.activeOnly();
                    filterDesc = "active only";
                    break;
                case "5":
                    filter = AssignmentFilters.inactiveOnly();
                    filterDesc = "inactive only";
                    break;
                case "6":
                    System.out.print("Enter date (yyyy-MM-dd HH:mm): ");
                    String afterDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.assignedAfter(afterDate);
                    filterDesc = "assigned after " + afterDate;
                    break;
                case "7":
                    System.out.print("Enter date (yyyy-MM-dd HH:mm): ");
                    String beforeDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.expiringBefore(beforeDate);
                    filterDesc = "expiring before " + beforeDate;
                    break;
                case "8":
                    filter = ra -> true;
                    filterDesc = "all assignments";
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);

            System.out.println("\n=== Search Results: " + filterDesc + " ===\n");

            if (results.isEmpty()) {
                System.out.println("No assignments found.");
            } else {
                results.sort(AssignmentSorters.byAssignmentDate());
                for (RoleAssignment ra : results) {
                    System.out.println(ra.summary());
                    System.out.println("-".repeat(50));
                }
                System.out.printf("\nFound: %d assignments\n", results.size());
            }
        });
    }

    private void registerPermissionCommands() {
        parser.registerCommand("permissions-user", "Show all permissions for a user", (scanner, system) -> {
            System.out.println("\n=== User Permissions ===\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();
            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);

            if (permissions.isEmpty()) {
                System.out.println("User has no permissions.");
                return;
            }

            Map<String, List<Permission>> byResource = permissions.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            System.out.println("\nPermissions for " + username + ":");
            System.out.println("=".repeat(60));

            byResource.forEach((resource, perms) -> {
                System.out.println("\n" + resource.toUpperCase() + ":");
                perms.forEach(p -> System.out.println("  - " + p.format()));
            });

            System.out.printf("\nTotal: %d permissions\n", permissions.size());
        });

        parser.registerCommand("permissions-check", "Check if user has a specific permission", (scanner, system) -> {
            System.out.println("\n=== Check Permission ===\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("✗ User not found: " + username);
                return;
            }

            User user = userOpt.get();

            System.out.print("Enter permission name (e.g., READ): ");
            String permName = scanner.nextLine().trim();

            System.out.print("Enter resource (e.g., users): ");
            String resource = scanner.nextLine().trim();

            boolean hasPermission = system.getAssignmentManager().userHasPermission(user, permName, resource);

            if (hasPermission) {
                System.out.println("\n✓ User HAS the permission: " + permName + " on " + resource);

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user).stream()
                        .filter(RoleAssignment::isActive)
                        .filter(ra -> ra.role().hasPermission(permName, resource))
                        .collect(Collectors.toList());

                System.out.println("Granted through these roles:");
                assignments.forEach(ra -> System.out.println("  - " + ra.role().name()));
            } else {
                System.out.println("\n✗ User does NOT have this permission.");
            }
        });
    }

    private void registerSearchCommands() {
        parser.registerCommand("search", "Global search across users, roles, and assignments", (scanner, system) -> {
            System.out.println("\n=== Global Search ===\n");

            System.out.print("Enter search term: ");
            String term = scanner.nextLine().trim().toLowerCase();

            if (term.isEmpty()) {
                System.out.println("Search term cannot be empty.");
                return;
            }

            System.out.println("\nSearch results for '" + term + "':\n");

            boolean found = false;

            List<User> users = system.getUserManager().findByFilter(
                    u -> u.username().toLowerCase().contains(term) ||
                            u.fullName().toLowerCase().contains(term) ||
                            u.email().toLowerCase().contains(term));

            if (!users.isEmpty()) {
                System.out.println("USERS (" + users.size() + "):");
                users.forEach(u -> System.out.println("  - " + u.format()));
                System.out.println();
                found = true;
            }

            List<Role> roles = system.getRoleManager().findByFilter(
                    r -> r.name().toLowerCase().contains(term) ||
                            r.description().toLowerCase().contains(term));

            if (!roles.isEmpty()) {
                System.out.println("ROLES (" + roles.size() + "):");
                roles.forEach(r -> System.out.println("  - " + r.name() + ": " + r.description()));
                System.out.println();
                found = true;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().findByFilter(
                    a -> a.user().username().toLowerCase().contains(term) ||
                            a.role().name().toLowerCase().contains(term));

            if (!assignments.isEmpty()) {
                System.out.println("ASSIGNMENTS (" + assignments.size() + "):");
                assignments.forEach(a -> System.out.println("  - " + a.summary()));
                System.out.println();
                found = true;
            }

            if (!found) {
                System.out.println("No matches found.");
            }
        });
    }

    private void registerSystemCommands() {
        parser.registerCommand("help", "Show this help message", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Show system statistics", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Clear the screen", (scanner, system) -> {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        });

        parser.registerCommand("exit", "Exit the program", (scanner, system) -> {
            System.out.print("Are you sure you want to exit? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("yes") || confirm.equals("y")) {
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Exit cancelled.");
            }
        });

        parser.registerCommand("save", "Save data to file (not implemented)", (scanner, system) -> {
            System.out.println("Save functionality not yet implemented.");
        });

        parser.registerCommand("load", "Load data from file (not implemented)", (scanner, system) -> {
            System.out.println("Load functionality not yet implemented.");
        });

        parser.registerCommand("whoami", "Show current user", (scanner, system) -> {
            System.out.println("Current user: " + system.getCurrentUser());
        });

        parser.registerCommand("set-user", "Set current user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            try {
                system.setCurrentUser(username);
                System.out.println("Current user set to: " + username);
            } catch (IllegalArgumentException e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        });
    }

    private void printUserTable(List<User> users) {
        System.out.printf("%-20s %-30s %-30s\n", "USERNAME", "FULL NAME", "EMAIL");
        System.out.println("-".repeat(80));

        for (User user : users) {
            String fullName = user.fullName().length() > 27 ?
                    user.fullName().substring(0, 24) + "..." :
                    user.fullName();
            String email = user.email().length() > 27 ?
                    user.email().substring(0, 24) + "..." :
                    user.email();

            System.out.printf("%-20s %-30s %-30s\n",
                    user.username(), fullName, email);
        }
    }
}