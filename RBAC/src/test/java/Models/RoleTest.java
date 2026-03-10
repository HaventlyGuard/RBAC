package Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Permission readPermission;
    private Permission writePermission;
    private Permission deletePermission;

    @BeforeEach
    void setUp() {
        readPermission = new Permission("READ", "users", "Can read users");
        writePermission = new Permission("WRITE", "users", "Can write users");
        deletePermission = new Permission("DELETE", "users", "Can delete users");
    }

    @Nested
    @DisplayName("Role Creation Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role with generated ID")
        void shouldCreateRoleWithGeneratedId() {
            Role role = new Role("Administrator", "Full system access");

            assertAll("Role properties",
                    () -> assertNotNull(role.id()),
                    () -> assertTrue(role.id().startsWith("role_")),
                    () -> assertEquals("Administrator", role.name()),
                    () -> assertEquals("Full system access", role.description()),
                    () -> assertTrue(role.permissions().isEmpty())
            );
        }

        @Test
        @DisplayName("Should throw exception for empty name")
        void shouldThrowExceptionForEmptyName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Role("   ", "Description"));
        }
    }

    @Nested
    @DisplayName("Permission Management Tests")
    class PermissionManagementTests {

        private Role role;

        @BeforeEach
        void setUp() {
            role = new Role("Test Role", "Test Description");
        }

        @Test
        @DisplayName("Should add permission")
        void shouldAddPermission() {
            Role updatedRole = role.addPermission(readPermission);

            assertNotSame(role, updatedRole);
            assertTrue(updatedRole.hasPermission(readPermission));
            assertEquals(1, updatedRole.permissions().size());
        }

        @Test
        @DisplayName("Should remove permission")
        void shouldRemovePermission() {
            role = role.addPermission(readPermission)
                    .addPermission(writePermission);

            Role updatedRole = role.removePermission(readPermission);

            assertFalse(updatedRole.hasPermission(readPermission));
            assertTrue(updatedRole.hasPermission(writePermission));
            assertEquals(1, updatedRole.permissions().size());
        }

        @Test
        @DisplayName("Should check permission by name and resource")
        void shouldCheckPermissionByNameAndResource() {
            role = role.addPermission(readPermission);

            assertTrue(role.hasPermission("READ", "users"));
            assertFalse(role.hasPermission("WRITE", "users"));
        }



        @Nested
        @DisplayName("Equals and HashCode Tests")
        class EqualsAndHashCodeTests {

            @Test
            @DisplayName("Roles with same ID should be equal")
            void shouldBeEqual() {
                Role role1 = new Role("Admin", "Description");

                Role role2 = new Role(
                        role1.id(),
                        role1.name(),
                        role1.description(),
                        role1.permissions()
                );

                assertEquals(role1, role2);
                assertEquals(role1.hashCode(), role2.hashCode());
            }

            @Test
            @DisplayName("Roles with different IDs should not be equal")
            void shouldNotBeEqual() {
                Role role1 = new Role("Admin", "Description");
                Role role2 = new Role("Manager", "Description");

                assertNotEquals(role1, role2);
            }
        }

        @Nested
        @DisplayName("Format Method Tests")
        class FormatTests {

            @Test
            @DisplayName("Should format role correctly")
            void shouldFormatRole() {
                Role role = new Role("Admin", "Administrator role")
                        .addPermission(readPermission)
                        .addPermission(writePermission);

                String formatted = role.format();

                assertAll("Format contains",
                        () -> assertTrue(formatted.contains("Role: Admin")),
                        () -> assertTrue(formatted.contains("Permissions (2):")),
                        () -> assertTrue(formatted.contains("READ on users")),
                        () -> assertTrue(formatted.contains("WRITE on users"))
                );
            }
        }
    }
}