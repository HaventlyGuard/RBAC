package Repositories.Interfaces;

import Filters.Interfaces.UserFilter;
import Models.User;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements IRepository<User> {

    private final Map<String, User> usersByUsername;
    private final Map<String, User> usersByEmail;

    public UserManager() {
        this.usersByUsername = new HashMap<>();
        this.usersByEmail = new HashMap<>();
    }

    @Override
    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");

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
    }

    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }

        User removed = usersByUsername.remove(user.username());
        if (removed != null) {
            usersByEmail.remove(user.email());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeById(String username) {
        User user = usersByUsername.get(username);
        if (user != null) {
            usersByUsername.remove(username);
            usersByEmail.remove(user.email());
            return true;
        }
        return false;
    }

    @Override
    public Optional<User> findById(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    public List<User> findByFilter(UserFilter filter) {
        List<User> result = new ArrayList<>();
        for (User user : usersByUsername.values()) {
            if (filter.test(user)) {
                result.add(user);
            }
        }
        return result;
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        return usersByUsername.values().parallelStream()
                .filter(user -> filter.test(user))
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Sorter cannot be null");

        return usersByUsername.values().stream()
                .filter(filter)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    @Override
    public int count() {
        return usersByUsername.size();
    }

    @Override
    public void clear() {
        usersByUsername.clear();
        usersByEmail.clear();
    }

    @Override
    public boolean exists(String username) {
        return usersByUsername.containsKey(username);
    }

    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }

    public void update(String username, String newFullName, String newEmail) {
        User existing = usersByUsername.get(username);
        if (existing == null) {
            throw new IllegalArgumentException("User with username '" + username + "' not found");
        }

        if (!existing.email().equals(newEmail) && usersByEmail.containsKey(newEmail)) {
            throw new IllegalArgumentException("Email '" + newEmail + "' is already in use");
        }

        User updatedUser = new User(username, newFullName, newEmail);

        usersByUsername.remove(username);
        usersByEmail.remove(existing.email());

        usersByUsername.put(username, updatedUser);
        usersByEmail.put(newEmail, updatedUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(usersByUsername, that.usersByUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersByUsername);
    }
}