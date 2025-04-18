package Login;

import java.util.Map;
import java.util.regex.Pattern;

import Actors.User;

public class Login {

    /**
     * Validates user login based on NRIC and password.
     * Returns the User object if successful, or null if credentials are invalid.
     */
    public static User authenticate(String nric, String password, Map<String, User> usersMap) {
        
        // pattern matching for NRIC
        String nricRegex = "^[A-Za-z]\\d{7}[A-Za-z]$";
        if (nric == null || !Pattern.matches(nricRegex, nric)) return null; 

        // find user
        User user = usersMap.get(nric.toUpperCase());
        if (user == null) return null;

        return user.getPassword().equals(password) ? user : null;
    }

    /**
     * Updates the password of the given user.
     */
    public static boolean updatePassword(User user, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }
        user.setPassword(newPassword);
        return true;
    }
}