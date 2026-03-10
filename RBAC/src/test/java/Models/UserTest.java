package Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.validate("john_doe", "John Doe", "john@example.com");
    }

    @Test
    @DisplayName("Should create valid user")
    void shouldCreateValidUser() {
        assertAll("User properties",
                () -> assertEquals("john_doe", validUser.username()),
                () -> assertEquals("John Doe", validUser.fullName()),
                () -> assertEquals("john@example.com", validUser.email())
        );
    }

    @Test
    @DisplayName("Should format user correctly")
    void shouldFormatUser() {
        String expected = "john_doe (John Doe) <john@example.com>";
        assertEquals(expected, validUser.format());
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception for invalid username - too short")
        void shouldThrowForInvalidUsernameTooShort() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> User.validate("jo", "John Doe", "john@example.com"));

            assertEquals("Username must be between 3 and 20 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid username - invalid characters")
        void shouldThrowForInvalidUsernameCharacters() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> User.validate("john@doe", "John Doe", "john@example.com"));

            assertTrue(exception.getMessage().contains("can only contain latin letters"));
        }

        @Test
        @DisplayName("Should throw exception for invalid email")
        void shouldThrowForInvalidEmail() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> User.validate("john_doe", "John Doe", "invalid-email"));

            assertTrue(exception.getMessage().contains("Email"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should throw exception for null or empty username")
        void shouldThrowForNullOrEmptyUsername(String invalidUsername) {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> User.validate(invalidUsername, "John Doe", "john@example.com"));

            assertTrue(exception.getMessage().contains("Username cannot be null or empty"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should throw exception for empty full name")
        void shouldThrowForEmptyFullName(String invalidFullName) {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> User.validate("john_doe", invalidFullName, "john@example.com"));

            assertTrue(exception.getMessage().contains("Full name cannot be null or empty"));
        }
    }
}