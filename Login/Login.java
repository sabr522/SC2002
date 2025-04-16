package Login;

import java.io.IOException;
import java.util.Map;
import Actors.User; 
import data.DataManager; 

public class Login {

    /**
     * Checks if the NRIC exists in the user map.
     * @param NRIC The NRIC entered by the user.
     * @param users Map of users keyed by NRIC.
     * @return The User object if found, else null.
     */
    public static User checkNRIC(String NRIC, Map<String, User> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("Error: No user data loaded.");
            return null;
        }

        User user = users.get(NRIC);
        if (user != null) {
            System.out.println("Valid NRIC");
            return user;
        } else {
            System.out.println("Invalid NRIC. Please enter valid NRIC:");
            return null;
        }
    }

    /**
     * Checks if the password matches the given user's password.
     * @param password The input password.
     * @param user The User object to verify against.
     * @return true if match, false otherwise.
     */
    public static boolean checkPassword(String password, User user) {
        if (user == null) {
            System.out.println("Error: No user found.");
            return false;
        }

        if (password.equals(user.getPassword())) {
            System.out.println("You have logged in!");
            return true;
        } else {
            System.out.println("Invalid password. Please enter valid password:");
            return false;
        }
    }

    /**
     * Changes the password for the given user, saves to file.
     * @param user The user whose password is to be changed.
     * @param newPass The new password.
     * @param confirmPass Confirmation of the new password.
     * @param users The full user map.
     * @param filePath The path to the users.csv file.
     * @return true if success, false otherwise.
     */
    public static boolean changePassword(User user, String newPass, String confirmPass, Map<String, User> users, String filePath) {
        if (!newPass.equals(confirmPass)) {
            System.out.println("Passwords do not match.");
            return false;
        }

        user.setPassword(newPass);
        System.out.println("Password updated successfully.");

        try {
            DataManager dm = new DataManager();
            dm.saveUsers(users);
        } catch (IOException e) {
            System.out.println("Failed to save updated password to file.");
            e.printStackTrace();
            return false;
        }

        return true;
    }
}