package Validations;

import Models.*;
import Models.interfaces.RoleAssignment;
import Repositories.Interfaces.AssignmentManager;
import Repositories.Interfaces.RoleManager;
import Repositories.Interfaces.UserManager;
import Validations.AuditLog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager, AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatUtils.formatHeader("USER REPORT"));
        sb.append("\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found.\n");
            return sb.toString();
        }

        String[] headers = {"Username", "Full Name", "Email", "Roles", "Permissions"};
        List<String[]> rows = new ArrayList<>();

        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            Set<Permission> permissions = assignmentManager.getUserPermissions(user);

            String rolesStr = assignments.stream()
                    .map(ra -> ra.role().name() + (ra.isActive() ? "" : " (inactive)"))
                    .collect(Collectors.joining(", "));

            if (rolesStr.isEmpty()) rolesStr = "None";

            rows.add(new String[]{
                    user.username(),
                    FormatUtils.truncate(user.fullName(), 20),
                    user.email(),
                    FormatUtils.truncate(rolesStr, 25),
                    String.valueOf(permissions.size())
            });
        }

        sb.append(FormatUtils.formatTable(headers, rows));

        sb.append("\n\nSUMMARY:\n");
        sb.append(String.format("  Total Users: %d\n", users.size()));

        long activeAssignments = assignmentManager.getActiveAssignments().size();
        sb.append(String.format("  Active Assignments: %d\n", activeAssignments));

        double avgRoles = users.stream()
                .mapToInt(u -> assignmentManager.findByUser(u).size())
                .average()
                .orElse(0);
        sb.append(String.format("  Average Roles per User: %.2f\n", avgRoles));

        auditLog.log("REPORT_GENERATE", "system", "users", "Generated user report");

        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager, AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatUtils.formatHeader("ROLE REPORT"));
        sb.append("\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("No roles found.\n");
            return sb.toString();
        }

        String[] headers = {"Role Name", "Description", "Permissions", "Users", "Active"};
        List<String[]> rows = new ArrayList<>();

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();

            rows.add(new String[]{
                    role.name(),
                    FormatUtils.truncate(role.description(), 25),
                    String.valueOf(role.permissions().size()),
                    String.valueOf(assignments.size()),
                    String.valueOf(activeCount)
            });
        }

        sb.append(FormatUtils.formatTable(headers, rows));

        sb.append("\n\nPERMISSION SUMMARY:\n");
        Map<String, Long> permCounts = new HashMap<>();
        for (Role role : roles) {
            for (Permission perm : role.permissions()) {
                String key = perm.name() + " on " + perm.resource();
                permCounts.put(key, permCounts.getOrDefault(key, 0L) + 1);
            }
        }

        permCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append(String.format("  %s: used in %d roles\n", e.getKey(), e.getValue())));

        auditLog.log("REPORT_GENERATE", "system", "roles", "Generated role report");

        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager, AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();

        sb.append(FormatUtils.formatHeader("PERMISSION MATRIX"));
        sb.append("\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found.\n");
            return sb.toString();
        }

        Set<String> allResources = new TreeSet<>();
        Map<String, Set<String>> userPermissions = new HashMap<>();

        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            Set<String> permKeys = new HashSet<>();

            for (Permission perm : perms) {
                String key = perm.name() + ":" + perm.resource();
                permKeys.add(key);
                allResources.add(perm.resource());
            }

            userPermissions.put(user.username(), permKeys);
        }

        List<String> resources = new ArrayList<>(allResources);
        String[] headers = new String[resources.size() + 1];
        headers[0] = "User";
        for (int i = 0; i < resources.size(); i++) {
            headers[i + 1] = FormatUtils.truncate(resources.get(i), 10);
        }

        List<String[]> rows = new ArrayList<>();
        for (User user : users) {
            String[] row = new String[resources.size() + 1];
            row[0] = user.username();

            Set<String> perms = userPermissions.get(user.username());

            for (int i = 0; i < resources.size(); i++) {
                String resource = resources.get(i);
                boolean canRead = perms.contains("READ:" + resource);
                boolean canWrite = perms.contains("WRITE:" + resource);
                boolean canDelete = perms.contains("DELETE:" + resource);

                StringBuilder cell = new StringBuilder();
                if (canRead) cell.append("R");
                if (canWrite) cell.append("W");
                if (canDelete) cell.append("D");
                if (cell.length() == 0) cell.append("-");

                row[i + 1] = cell.toString();
            }

            rows.add(row);
        }

        sb.append(FormatUtils.formatTable(headers, rows));

        sb.append("\nLegend: R=Read, W=Write, D=Delete\n");

        auditLog.log("REPORT_GENERATE", "system", "matrix", "Generated permission matrix");

        return sb.toString();
    }

    public void exportToFile(String report, String filename, AuditLog auditLog) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Report saved to: " + filename);
            auditLog.log("REPORT_SAVE", "system", filename, "Saved report to file");
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
            auditLog.log("REPORT_ERROR", "system", filename, "Error: " + e.getMessage());
        }
    }
}