package Repositories;

import Models.User;
import Repositories.Interfaces.UserManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;
    private User john;
    private User jane;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        john = User.validate("john_doe", "John Doe", "john@example.com");
        jane = User.validate("jane_smith", "Jane Smith", "jane@company.com");

        userManager.add(john);
        userManager.add(jane);
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudTests {

        @Test
        @DisplayName("Should add user")
        void shouldAddUser() {
            User bob = User.validate("bob_wilson", "Bob Wilson", "bob@example.com");
            userManager.add(bob);

            assertEquals(3, userManager.count());
            assertTrue(userManager.exists("bob_wilson"));
        }

        @Test
        @DisplayName("Should throw exception when adding duplicate username")
        void shouldThrowExceptionForDuplicateUsername() {
            User duplicate = User.validate("john_doe", "Johnny Doe", "johnny@example.com");

            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> userManager.add(duplicate));

            assertTrue(exception.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("Should remove user")
        void shouldRemoveUser() {
            boolean removed = userManager.remove(john);

            assertTrue(removed);
            assertEquals(1, userManager.count());
            assertFalse(userManager.exists("john_doe"));
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindByUsername() {
            Optional<User> found = userManager.findByUsername("john_doe");

            assertTrue(found.isPresent());
            assertEquals(john, found.get());
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindByEmail() {
            Optional<User> found = userManager.findByEmail("jane@company.com");

            assertTrue(found.isPresent());
            assertEquals(jane, found.get());
        }
    }
}