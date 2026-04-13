package Repositories.Repo;

import Models.*;
import Filters.Interfaces.AssignmentFilter;
import Models.interfaces.RoleAssignment;
import Repositories.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ThreadSafeAssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignmentsById;
    private final ReentrantReadWriteLock lock;
    private final ThreadSafeUserManager userManager;
    private final ThreadSafeRoleManager roleManager;

    public ThreadSafeAssignmentManager(ThreadSafeUserManager userManager, ThreadSafeRoleManager roleManager) {
        this.assignmentsById = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");

        lock.writeLock().lock();
        try {
            assignmentsById.put(assignment.assignmentId(), assignment);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;

        lock.writeLock().lock();
        try {
            return assignmentsById.remove(assignment.assignmentId()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeById(String id) {
        lock.writeLock().lock();
        try {
            return assignmentsById.remove(id) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(assignmentsById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(assignmentsById.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignmentsById.values()) {
                if (assignment.user().equals(user)) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByRole(Role role) {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignmentsById.values()) {
                if (assignment.role().equals(role)) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> getActiveAssignments() {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignmentsById.values()) {
                if (assignment.isActive()) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Permission> getUserPermissions(User user) {
        lock.readLock().lock();
        try {
            Set<Permission> permissions = new HashSet<>();
            for (RoleAssignment assignment : assignmentsById.values()) {
                if (assignment.user().equals(user) && assignment.isActive()) {
                    permissions.addAll(assignment.role().permissions());
                }
            }
            return permissions;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return assignmentsById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            assignmentsById.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(String id) {
        lock.readLock().lock();
        try {
            return assignmentsById.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }
}