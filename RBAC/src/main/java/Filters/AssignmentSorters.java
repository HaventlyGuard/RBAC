package Filters;

import Models.interfaces.RoleAssignment;

import java.util.Comparator;

public class AssignmentSorters {

    private AssignmentSorters() {}

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(
                assignment -> assignment.user().username(),
                String.CASE_INSENSITIVE_ORDER
        );
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(
                assignment -> assignment.role().name(),
                String.CASE_INSENSITIVE_ORDER
        );
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(
                assignment -> assignment.metadata().assignedAt()
        );
    }

    public static Comparator<RoleAssignment> byExpirationDate() {
        return (a1, a2) -> {
            String d1 = a1 instanceof Models.TemporaryAssignment temp1 ?
                    temp1.getExpiresAt() : "9999-12-31 23:59";
            String d2 = a2 instanceof Models.TemporaryAssignment temp2 ?
                    temp2.getExpiresAt() : "9999-12-31 23:59";
            return d1.compareTo(d2);
        };
    }
}