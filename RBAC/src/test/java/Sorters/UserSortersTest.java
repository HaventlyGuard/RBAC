package Sorters;

import Filters.UserSorters;
import Models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserSortersTest {

    private List<User> users;

    @BeforeEach
    void setUp() {
        users = new ArrayList<>();
        users.add(User.validate("john_doe", "John Doe", "john@example.com"));
        users.add(User.validate("alice_brown", "Alice Brown", "alice@example.com"));
        users.add(User.validate("bob_wilson", "Bob Wilson", "bob@example.com"));
    }

    @Test
    @DisplayName("Sort by username")
    void testSortByUsername() {
        Comparator<User> comparator = UserSorters.byUsername();
        users.sort(comparator);

        assertEquals("alice_brown", users.get(0).username());
        assertEquals("bob_wilson", users.get(1).username());
        assertEquals("john_doe", users.get(2).username());
    }

    @Test
    @DisplayName("Sort by full name")
    void testSortByFullName() {
        Comparator<User> comparator = UserSorters.byFullName();
        users.sort(comparator);

        assertEquals("Alice Brown", users.get(0).fullName());
        assertEquals("Bob Wilson", users.get(1).fullName());
        assertEquals("John Doe", users.get(2).fullName());
    }

    @Test
    @DisplayName("Sort by email")
    void testSortByEmail() {
        Comparator<User> comparator = UserSorters.byEmail();
        users.sort(comparator);

        assertEquals("alice@example.com", users.get(0).email());
        assertEquals("bob@example.com", users.get(1).email());
        assertEquals("john@example.com", users.get(2).email());
    }
}