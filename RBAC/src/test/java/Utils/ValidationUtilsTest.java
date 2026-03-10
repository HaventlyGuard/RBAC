package Utils;

import Validations.ValidationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    @DisplayName("Valid usernames should pass validation")
    void testValidUsernames() {
        assertTrue(ValidationUtils.isValidUsername("john_doe"));
        assertTrue(ValidationUtils.isValidUsername("admin123"));
        assertTrue(ValidationUtils.isValidUsername("a_very_long_username"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jo", "a", "thisusernameiswaytoolong123", "john@doe", "john doe"})
    @DisplayName("Invalid usernames should fail validation")
    void testInvalidUsernames(String invalidUsername) {
        assertFalse(ValidationUtils.isValidUsername(invalidUsername));
    }

    @Test
    @DisplayName("Valid emails should pass validation")
    void testValidEmails() {
        assertTrue(ValidationUtils.isValidEmail("john@example.com"));
        assertTrue(ValidationUtils.isValidEmail("jane.doe@company.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("user+tag@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"john.example.com", "john@", "@example.com", "john@example", "john@.com"})
    @DisplayName("Invalid emails should fail validation")
    void testInvalidEmails(String invalidEmail) {
        assertFalse(ValidationUtils.isValidEmail(invalidEmail));
    }

    @Test
    @DisplayName("Valid dates should pass validation")
    void testValidDates() {
        assertTrue(ValidationUtils.isValidDate("2026-03-10"));
        assertTrue(ValidationUtils.isValidDate("2026-03-10 15:30"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"10-03-2026", "2026/03/10", "invalid", "2026-03-10 15:30:45"})
    @DisplayName("Invalid dates should fail validation")
    void testInvalidDates(String invalidDate) {
        assertFalse(ValidationUtils.isValidDate(invalidDate));
    }

    @Test
    @DisplayName("Normalize string should trim and collapse spaces")
    void testNormalizeString() {
        assertEquals("John Doe", ValidationUtils.normalizeString("  John   Doe  "));
        assertEquals("", ValidationUtils.normalizeString(null));
    }
}