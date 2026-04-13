package Repositories.Repo;

import Models.Role;
import Models.Permission;
import Filters.Interfaces.RoleFilter;
import Repositories.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeRoleManager implements Repository<Role> {

    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;
    private final ReentrantReadWriteLock lock;

    public ThreadSafeRoleManager() {
        this.rolesById = new ConcurrentHashMap<>();
        this.rolesByName = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void add(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");

        lock.writeLock().lock();
        try {
            if (rolesById.containsKey(role.id())) {
                throw new IllegalArgumentException("Role with ID already exists");
            }
            if (rolesByName.containsKey(role.name())) {
                throw new IllegalArgumentException("Role with name already exists");
            }
            rolesById.put(role.id(), role);
            rolesByName.put(role.name(), role);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;

        lock.writeLock().lock();
        try {
            Role removed = rolesById.remove(role.id());
            if (removed != null) {
                rolesByName.remove(role.name());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeById(String id) {
        lock.writeLock().lock();
        try {
            Role removed = rolesById.remove(id);
            if (removed != null) {
                rolesByName.remove(removed.name());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Role> findByName(String name) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesByName.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Role> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilter(RoleFilter filter) {
        lock.readLock().lock();
        try {
            List<Role> result = new ArrayList<>();
            for (Role role : rolesById.values()) {
                if (filter.test(role)) {
                    result.add(role);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        lock.readLock().lock();
        try {
            return rolesById.values().parallelStream()
                    .filter(filter::test)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException("Role not found: " + roleName);
            }
            role.addPermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException("Role not found: " + roleName);
            }
            role.removePermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return rolesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            rolesById.clear();
            rolesByName.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(String id) {
        lock.readLock().lock();
        try {
            return rolesById.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean existsByName(String name) {
        lock.readLock().lock();
        try {
            return rolesByName.containsKey(name);
        } finally {
            lock.readLock().unlock();
        }
    }
}