package Repositories.Repo;

import Models.User;
import Filters.UserFilters;
import Repositories.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ThreadSafeUserManager implements Repository<User> {

    private final Map<String, User> usersByUsername;
    private final Map<String, User> usersByEmail;
    private final ReentrantReadWriteLock lock;

    public ThreadSafeUserManager() {
        this.usersByUsername = new ConcurrentHashMap<>();
        this.usersByEmail = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");

        lock.writeLock().lock();
        try {
            String username = user.username();
            String email = user.email();

            if (usersByUsername.containsKey(username)) {
                throw new IllegalArgumentException("User with username '" + username + "' already exists");
            }

            if (usersByEmail.containsKey(email)) {
                throw new IllegalArgumentException("User with email '" + email + "' already exists");
            }

            usersByUsername.put(username, user);
            usersByEmail.put(email, user);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(User user) {
        if (user == null) return false;

        lock.writeLock().lock();
        try {
            User removed = usersByUsername.remove(user.username());
            if (removed != null) {
                usersByEmail.remove(user.email());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeById(String username) {
        lock.writeLock().lock();
        try {
            User removed = usersByUsername.remove(username);
            if (removed != null) {
                usersByEmail.remove(removed.email());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<User> findById(String username) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(usersByUsername.get(username));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(usersByEmail.get(email));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<User> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(usersByUsername.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> findByFilter(UserFilters filter) {
        lock.readLock().lock();
        try {
            List<User> result = new ArrayList<>();
            for (User user : usersByUsername.values()) {
                if (filter.test(user)) {
                    result.add(user);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> findByFilterParallel(UserFilters filter) {
        lock.readLock().lock();
        try {
            return usersByUsername.values().parallelStream()
                    .filter(user -> filter.test(user))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return usersByUsername.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            usersByUsername.clear();
            usersByEmail.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(String username) {
        lock.readLock().lock();
        try {
            return usersByUsername.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean existsByEmail(String email) {
        lock.readLock().lock();
        try {
            return usersByEmail.containsKey(email);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void update(String username, String newFullName, String newEmail) {
        lock.writeLock().lock();
        try {
            User existing = usersByUsername.get(username);
            if (existing == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }

            if (!existing.email().equals(newEmail) && usersByEmail.containsKey(newEmail)) {
                throw new IllegalArgumentException("Email already in use: " + newEmail);
            }

            User updatedUser = new User(username, newFullName, newEmail);
            usersByEmail.remove(existing.email());
            usersByUsername.put(username, updatedUser);
            usersByEmail.put(newEmail, updatedUser);
        } finally {
            lock.writeLock().unlock();
        }
    }
}