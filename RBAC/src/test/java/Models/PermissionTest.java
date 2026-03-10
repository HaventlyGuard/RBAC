package Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {

    @Nested
    @DisplayName("Permission Creation Tests")
    class CreatePermissionTests {

        @Test
        @DisplayName("Should create valid permission with normalization")
        void shouldCreateValidPermission() {
            Permission permission = new Permission("read", "users", "Can read users");

            assertAll("Permission properties",
                    () -> assertEquals("READ", permission.name()),
                    () -> assertEquals("users", permission.resource()),
                    () -> assertEquals("Can read users", permission.description())
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"READ USERS", "READ  USERS"})
        @DisplayName("Should throw exception for spaces in name")
        void shouldThrowExceptionForSpacesInName(String invalidName) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Permission(invalidName, "users", "Test"));
        }



        @Test
        @DisplayName("Should throw exception for empty description")
        void shouldThrowExceptionForEmptyDescription() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Permission("READ", "users", "   "));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should throw exception for null or empty name")
        void shouldThrowExceptionForNullOrEmptyName(String invalidName) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Permission(invalidName, "users", "Test"));
        }
    }

    @Nested
    @DisplayName("Format Method Tests")
    class FormatTests {

        @Test
        @DisplayName("Should format permission correctly")
        void shouldFormatCorrectly() {
            Permission permission = new Permission("READ", "users", "Can read user data");
            assertEquals("READ on users: Can read user data", permission.format());
        }
    }

    @Nested
    @DisplayName("Matches Method Tests")
    class MatchesTests {

        private Permission permission;

        @BeforeEach
        void setUp() {
            permission = new Permission("READ", "user_profiles", "Can read user profiles");
        }

        @ParameterizedTest
        @CsvSource({
                "READ, user, true",
                "read, user, true",
                "WRITE, user, false",
                "READ, report, false",
                "'', '', true",
                "null, user, true",
                "READ, null, true"
        })
        @DisplayName("Should match patterns correctly")
        void shouldMatchCorrectly(String namePattern, String resourcePattern, boolean expected) {
            if ("null".equals(namePattern)) namePattern = null;
            if ("null".equals(resourcePattern)) resourcePattern = null;

            assertEquals(expected, permission.matches(namePattern, resourcePattern));
        }
    }
}