package cli;

import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import Actors.User;
import Login.Login;

public class LoginCLI {

    /**
     * Prompts user to login with NRIC and password using the Scanner.
     * Returns the authenticated User object or null if user exits.
     */
    public static User loginUser(Scanner scanner, Map<String, User> usersMap) {
        
        String nric = null;
        User user = null;

        while (true) {
            System.out.print("Enter NRIC (Format: A#######Z, or type 'exit' to quit): ");
            nric = scanner.nextLine().trim();
            // exit
            if (nric.equalsIgnoreCase("exit")) return null; 
            
            // format check
            String nricRegex = "^[A-Za-z]\\d{7}[A-Za-z]$";
            if (Pattern.matches(nricRegex, nric)) {
                break;
            } else {
                System.out.println("Invalid NRIC format. Please try again (e.g., S1234567Z).");
            }
        }
 
        while (true) {
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            user = Login.authenticate(nric, password, usersMap);
            if (user != null) {
                System.out.println("Login successful. Welcome, " + user.getName() + "!");
                return user;
            } else {
                System.out.println("Invalid password. Please try again.");
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
            System.out.println("You will be logged out and need to log in again with the new password.");
            return true;
        } else {
            System.out.println("Passwords do not match. Try again.");
            return false;
        }
    }
}