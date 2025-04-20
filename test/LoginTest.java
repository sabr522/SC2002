package Login; // Match package of class being tested

import static org.junit.jupiter.api.Assertions.*; // Import JUnit assertions
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import Actors.User; // Import necessary classes
import Actors.Applicant;
import Actors.Manager;

// Test class for the Login component
class LoginTest {

    private Map<String, User> testUsersMap;

    // This method runs BEFORE each test method
    @BeforeEach
    void setUp() {
        // Create a fresh map with mock user data for each test
        testUsersMap = new HashMap<>();
        // Use plain "password" here because User constructor will hash it
        testUsersMap.put("S1234567A", new Applicant("Test Applicant", "S1234567A", 30, "Married", "password"));
        testUsersMap.put("T9876543Z", new Manager("Test Manager", "T9876543Z", 45, "Single", "password"));
        // Add an entry with a DIFFERENT password for testing failure cases
        testUsersMap.put("S1111111B", new Applicant("Wrong Pass", "S1111111B", 25, "Single", "otherpass"));
    }

    @Test
    @DisplayName("Test Valid Applicant Login")
    void testValidApplicantLogin() {
        User user = Login.authenticate("S1234567A", "password", testUsersMap);
        assertNotNull(user, "User should not be null for valid login"); // Check if user object is returned
        assertEquals("Applicant", user.getRole(), "User role should be Applicant"); // Check if correct role
        assertEquals("Test Applicant", user.getName(), "User name should match");
    }

    @Test
    @DisplayName("Test Valid Manager Login")
    void testValidManagerLogin() {
        User user = Login.authenticate("T9876543Z", "password", testUsersMap);
        assertNotNull(user, "User should not be null for valid login");
        assertEquals("Manager", user.getRole(), "User role should be Manager");
    }

    @Test
    @DisplayName("Test Incorrect Password")
    void testIncorrectPassword() {
        User user = Login.authenticate("S1234567A", "wrongpassword", testUsersMap);
        assertNull(user, "User should be null for incorrect password"); // Expect null return
    }

    @Test
    @DisplayName("Test Non-Existent User")
    void testNonExistentUser() {
        User user = Login.authenticate("S0000000X", "password", testUsersMap);
        assertNull(user, "User should be null for non-existent NRIC"); // Expect null return
    }

    @Test
    @DisplayName("Test Invalid NRIC Format - Too Short")
    void testInvalidNricFormatTooShort() {
        User user = Login.authenticate("S123456A", "password", testUsersMap);
        assertNull(user, "User should be null for NRIC format too short"); // Expect null due to format check
    }

    @Test
    @DisplayName("Test Invalid NRIC Format - Wrong Start Char")
    void testInvalidNricFormatWrongStart() {
         User user = Login.authenticate("A1234567Z", "password", testUsersMap);
         assertNull(user, "User should be null for NRIC format wrong start char"); // Expect null
    }

     @Test
     @DisplayName("Test Invalid NRIC Format - Wrong End Char")
     void testInvalidNricFormatWrongEnd() {
          User user = Login.authenticate("S12345678", "password", testUsersMap);
          assertNull(user, "User should be null for NRIC format wrong end char"); // Expect null
     }

     @Test
     @DisplayName("Test Login Case Insensitivity for NRIC (if applicable)")
     void testLoginNricCaseInsensitive() {
          User user = Login.authenticate("s1234567a", "password", testUsersMap);
          assertNotNull(user, "Login should work with lowercase NRIC if lookup handles case");
          if (user != null) {
             assertEquals("S1234567A", user.getNric(), "Stored NRIC should be consistent case"); // Optional check
          }
     }

     // Add a simple test for updatePassword (optional, might be better in UserTest)
     @Test
     @DisplayName("Test Password Update Logic Success")
      void testUpdatePasswordSuccess() {
           User userToUpdate = testUsersMap.get("S1234567A");
           boolean updated = Login.updatePassword(userToUpdate, "newPass123", "newPass123");
           assertTrue(updated, "Password update should return true for matching passwords");
           // Verify the hash actually changed (more involved test, needs access to hash/verify)
           assertTrue(userToUpdate.verifyPassword("newPass123"), "User should be able to verify with the new password");
           assertFalse(userToUpdate.verifyPassword("password"), "User should NOT be able to verify with the old password");
      }

     @Test
     @DisplayName("Test Password Update Logic Failure Mismatch")
      void testUpdatePasswordMismatch() {
           User userToUpdate = testUsersMap.get("S1234567A");
           boolean updated = Login.updatePassword(userToUpdate, "newPass123", "differentPass");
           assertFalse(updated, "Password update should return false for non-matching passwords");
           assertTrue(userToUpdate.verifyPassword("password"), "Password should remain unchanged after failed update");
      }

}