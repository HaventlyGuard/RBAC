package Utils;

import Models.*;
import Models.interfaces.RoleAssignment;
import Repositories.Repo.*;
import Validations.FormatUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ParallelReportGenerator {

    private final ThreadSafeUserManager userManager;
    private final ThreadSafeAssignmentManager assignmentManager;

    public ParallelReportGenerator(ThreadSafeUserManager userManager,
                                   ThreadSafeAssignmentManager assignmentManager) {
        this.userManager = userManager;
        this.assignmentManager = assignmentManager;
    }

    public String generateUserReportParallel() {
        long startTime = System.currentTimeMillis();

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            return "No users found.\n";
        }

        Map<User, Set<Permission>> userPermissionsMap = new ConcurrentHashMap<>();

        users.parallelStream().forEach(user -> {
            Set<Permission> perms = new HashSet<>();
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    perms.addAll(ra.role().permissions());
                }
            }
            userPermissionsMap.put(user, perms);
        });

        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("USER REPORT (Parallel)"));
        sb.append("\n\n");

        String[] headers = {"Username", "Full Name", "Email", "Roles Count", "Permissions Count"};
        List<String[]> rows = new ArrayList<>();

        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            Set<Permission> perms = userPermissionsMap.get(user);

            rows.add(new String[]{
                    user.username(),
                    FormatUtils.truncate(user.fullName(), 20),
                    user.email(),
                    String.valueOf(assignments.size()),
                    String.valueOf(perms != null ? perms.size() : 0)
            });
        }

        sb.append(FormatUtils.formatTable(headers, rows));

        long endTime = System.currentTimeMillis();
        sb.append("\n\nGenerated in parallel mode. Time: ").append(endTime - startTime).append(" ms");

        return sb.toString();
    }

    public String generatePermissionMatrixParallel() {
        long startTime = System.currentTimeMillis();

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            return "No users found.\n";
        }

        Set<String> allResources = ConcurrentHashMap.newKeySet();
        Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();

        users.parallelStream().forEach(user -> {
            Set<Permission> perms = new HashSet<>();
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    perms.addAll(ra.role().permissions());
                }
            }

            Set<String> permKeys = ConcurrentHashMap.newKeySet();
            for (Permission perm : perms) {
                String key = perm.name() + ":" + perm.resource();
                permKeys.add(key);
                allResources.add(perm.resource());
            }
            userPermissions.put(user.username(), permKeys);
        });

        List<String> resources = new ArrayList<>(allResources);
        Collections.sort(resources);

        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("PERMISSION MATRIX (Parallel)"));
        sb.append("\n\n");

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
                boolean canRead = perms != null && perms.contains("READ:" + resource);
                boolean canWrite = perms != null && perms.contains("WRITE:" + resource);
                boolean canDelete = perms != null && perms.contains("DELETE:" + resource);

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

        long endTime = System.currentTimeMillis();
        sb.append("\n\nGenerated in parallel mode. Time: ").append(endTime - startTime).append(" ms");
        sb.append("\nLegend: R=Read, W=Write, D=Delete\n");

        return sb.toString();
    }
}