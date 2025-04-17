package cli;

import java.util.Map;
import java.util.Scanner;
import Actors.User;
import Login.Login;

public class LoginCLI {

    /**
     * Prompts user to login with NRIC and password using the Scanner.
     * Returns the authenticated User object or null if user exits.
     */
    public static User loginUser(Scanner scanner, Map<String, User> usersMap) {
        while (true) {
            System.out.print("Enter NRIC (or type 'exit' to quit): ");
            String nric = scanner.nextLine().trim();

            if (nric.equalsIgnoreCase("exit")) return null;

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            User user = Login.authenticate(nric, password, usersMap);
            if (user != null) {
                System.out.println("Login successful. Welcome, " + user.getName() + "!");
                return user;
            } else {
                System.out.println("Invalid NRIC or password. Please try again.");
            }
        }
    }

    /**
     * Handles password change prompt for logged-in user.
     */
    public static boolean changePassword(Scanner scanner, User user) {
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (Login.updatePassword(user, newPassword, confirmPassword)) {
            System.out.println("Password successfully updated.");
            return true;
        } else {
            System.out.println("Passwords do not match. Try again.");
            return false;
        }
    }
}