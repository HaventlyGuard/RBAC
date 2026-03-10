package Filters;

import Models.User;
import Filters.Interfaces.UserFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserFiltersTest {

    private List<User> users;
    private User john;
    private User jane;
    private User bob;

    @BeforeEach
    void setUp() {
        john = User.validate("john_doe", "John Doe", "john@example.com");
        jane = User.validate("jane_smith", "Jane Smith", "jane@company.com");
        bob = User.validate("bob_wilson", "Bob Wilson", "bob@example.com");
        users = Arrays.asList(john, jane, bob);
    }

    @Test
    @DisplayName("Filter by username exact match")
    void testByUsername() {
        UserFilter filter = UserFilters.byUsername("john_doe");
        List<User> result = users.stream().filter(filter).collect(Collectors.toList());

        assertEquals(1, result.size());
        assertEquals(john, result.get(0));
    }

    @Test
    @DisplayName("Filter by username contains")
    void testByUsernameContains() {
        UserFilter filter = UserFilters.byUsernameContains("john");
        List<User> result = users.stream().filter(filter).collect(Collectors.toList());

        assertEquals(1, result.size());
        assertEquals(john, result.get(0));
    }

    @Test
    @DisplayName("Filter by email domain")
    void testByEmailDomain() {
        UserFilter filter = UserFilters.byEmailDomain("@company.com");
        List<User> result = users.stream().filter(filter).collect(Collectors.toList());

        assertEquals(1, result.size());
        assertEquals(jane, result.get(0));
    }

    @Test
    @DisplayName("Filter by full name contains")
    void testByFullNameContains() {
        UserFilter filter = UserFilters.byFullNameContains("Smith");
        List<User> result = users.stream().filter(filter).collect(Collectors.toList());

        assertEquals(1, result.size());
        assertEquals(jane, result.get(0));
    }

    @Test
    @DisplayName("Combine filters with AND")
    void testAndFilter() {
        UserFilter filter1 = UserFilters.byUsernameContains("john");
        UserFilter filter2 = UserFilters.byEmailDomain("@example.com");
        UserFilter combined = filter1.and(filter2);

        List<User> result = users.stream().filter(combined).collect(Collectors.toList());

        assertEquals(1, result.size());
        assertEquals(john, result.get(0));
    }

    @Test
    @DisplayName("Combine filters with OR")
    void testOrFilter() {
        UserFilter filter1 = UserFilters.byUsername("john_doe");
        UserFilter filter2 = UserFilters.byUsername("jane_smith");
        UserFilter combined = filter1.or(filter2);

        List<User> result = users.stream().filter(combined).collect(Collectors.toList());

        assertEquals(2, result.size());
    }
}