package main;

import cli.ManagerCLI;
// Import other CLIs if/when they are created and needed
// import cli.OfficerCLI;
// import cli.ApplicantCLI;
import data.DataManager;
import Actors.User;
import Actors.Manager;
import Actors.Officer;   // Keep import even if CLI is commented, needed for User map / DataManager
import Actors.Applicant; // Keep import even if CLI is commented, needed for User map / DataManager
import Project.Project;

import java.util.Map;
import java.util.Scanner;
import java.util.InputMismatchException; // Keep for readIntInput helper

/**
 * Main application class for the BTO Management System.
 * Handles application startup, data loading, user login, role delegation (CLI launching),
 * password changes, and data saving on exit/logout.
 */
public class MainApp {

    private static Scanner scanner = new Scanner(System.in);
    private static DataManager dataManager = new DataManager();
    private static Map<String, User> allUsersMap = null;    // Initialize to null
    private static Map<String, Project> allProjectsMap = null; // Initialize to null

    public static void main(String[] args) {
        System.out.println("===== Welcome to the BTO Management System =====");

        // 1. Load Data
        if (!loadAllData()) {
            System.err.println("Critical error loading data. Exiting application.");
            scanner.close(); // Close scanner on exit
            return;
        }
        System.out.println("Data loaded successfully.");

        User currentUser = null; // No user logged in initially

        // Main Application Loop
        while (true) {

            // If no user is logged in, handle the login process
            if (currentUser == null) {
                currentUser = handleLogin();
                if (currentUser == null) {
                    // Login failed and user chose to exit application
                    System.out.println("Exiting BTO Management System.");
                    // Attempt to save any potential changes before exiting
                    saveAllData();
                    break; // Exit the main application loop
                }
            }

            // --- Post-Login Menu ---
            // This menu appears only when a user is successfully logged in
            System.out.println("\n--- Main Menu ---");
            System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
            System.out.println("1. Access Role Menu");
            System.out.println("2. Change Password");
            System.out.println("0. Logout & Save");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    // Launch the appropriate CLI based on the user's role
                    launchRoleCLI(currentUser);
                    // After returning from the role CLI, the user is effectively logged out
                    // from that specific role's context. We set currentUser to null
                    // to force re-login or exit via the main loop's logic.
                    // Note: Saving happens when they choose option 0 (Logout & Save) below.
                    System.out.println("\nReturned to Main Menu. Select an option or Logout.");
                    // Keep currentUser, loop will show post-login menu again
                    break;

                case 2:
                    // Handle password change for the currently logged-in user
                    handleChangePassword(currentUser);
                    // Password is changed in the currentUser object in memory.
                    // Changes will be persisted when saveAllData() is called.
                    break;

                case 0:
                    // Handle logout: Save data and reset currentUser
                    System.out.println("Logging out...");
                    saveAllData(); // Save changes before logging out
                    System.out.println("Data saved.");
                    currentUser = null; // Reset the current user
                    System.out.println("Logged out successfully. Returning to Login screen or exiting...");
                    // The loop will naturally go back to the login prompt if it continues
                    // Optionally, uncomment the next line to exit the app completely after logout
                    // break; // This would exit the while(true) loop
                    break; // Break the switch, loop continues (back to login check)

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

             // Optional: Check if user chose to exit the entire application after logging out.
             // If 'break;' was used inside case 0, this condition becomes relevant if needed.
             // if (currentUser == null && choice == 0) {
             //     System.out.println("\nExiting BTO Management System after logout.");
             //     break; // Exit the main application loop
             // }

        } // End Main Application Loop (while true)

        scanner.close(); // Close the scanner when the application loop ends
        System.out.println("Application closed.");
    }

    /**
     * Loads all necessary data using the DataManager.
     * Populates allUsersMap and allProjectsMap.
     * @return true if loading was successful, false otherwise.
     */
    private static boolean loadAllData() {
        try {
            System.out.println("Loading users...");
            allUsersMap = dataManager.loadUsers();
            System.out.println("Loading core projects...");
            allProjectsMap = dataManager.loadProjectsCore();
            System.out.println("Loading project flats...");
            dataManager.loadProjectFlats(allProjectsMap); // Pass the loaded projects map
            System.out.println("Loading project officers...");
            dataManager.loadProjectOfficers(allProjectsMap, allUsersMap); // Pass both maps
            System.out.println("Loading applications...");
            dataManager.loadApplications(allProjectsMap, allUsersMap); // Pass both maps

            // --- Uncomment if Enquiries are implemented ---
            // System.out.println("Loading enquiries...");
            // dataManager.loadEnquiries(allProjectsMap, allUsersMap); // Assuming method exists

            // Basic validation after loading
            if (allUsersMap == null || allProjectsMap == null) {
                 System.err.println("Error: Data maps are null after loading attempt.");
                 return false;
            }

            return true; // Loading successful

        } catch (Exception e) {
            System.err.println("Fatal error during data loading: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return false; // Loading failed
        }
    }

    /**
     * Saves all application data (Users and Projects) using the DataManager.
     * Should be called before exiting or logging out to persist changes.
     */
    private static void saveAllData() {
        // Prevent saving null maps if loading failed catastrophically
        if (allUsersMap == null || allProjectsMap == null) {
             System.err.println("Warning: Data maps are not initialized. Skipping save operation.");
             return;
        }
        try {
            System.out.println("Saving users...");
            dataManager.saveUsers(allUsersMap);
            System.out.println("Saving all project data (Core, Flats, Officers, Applications)...");
            dataManager.saveAllProjectData(allProjectsMap); // This one method handles saving all project-related CSVs

            // --- Uncomment if Enquiries are implemented ---
            // System.out.println("Saving enquiries...");
            // dataManager.saveEnquiries(...); // Pass necessary data, assuming method exists

        } catch (Exception e) {
            System.err.println("Error encountered during data saving: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    /**
     * Handles the user login process.
     * Prompts for NRIC and Password, validates against loaded data.
     * Allows the user to type 'exit' to quit the application.
     * @return The logged-in User object, or null if login failed or user chose to exit.
     */
    private static User handleLogin() {
        while (true) {
            System.out.println("\n--- Login ---");
            System.out.print("Enter NRIC (or type 'exit' to quit): ");
            String nric = scanner.nextLine().trim();

            if (nric.equalsIgnoreCase("exit")) {
                return null; // Signal to exit the application
            }
            if (nric.isEmpty()){
                System.out.println("NRIC cannot be empty. Please try again.");
                continue;
            }

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            User user = allUsersMap.get(nric); // Look up user by NRIC

            if (user != null) {
                String expectedPassword = user.getPassword();
                // Handle potential default password scenario based on requirements
                // Assuming "password" is the default if the loaded password field is null or empty
                if (expectedPassword == null || expectedPassword.isEmpty()) {
                    expectedPassword = "password"; // Default password check
                }

                // Validate password
                if (expectedPassword.equals(password)) {
                    System.out.println("Login successful!");
                    return user; // Return the authenticated user object
                } else {
                    System.out.println("Invalid NRIC or Password. Please try again.");
                }
            } else {
                // User NRIC not found in the map
                System.out.println("Invalid NRIC or Password. Please try again.");
            }
            // Loop continues if login fails
        }
    }

     /**
      * Handles the process for changing the current user's password.
      * Prompts for new password and confirmation. Updates the user object in memory.
      * @param currentUser The User object whose password needs changing.
      */
    private static void handleChangePassword(User currentUser) {
        if (currentUser == null) {
            System.out.println("Error: No user logged in to change password.");
            return;
        }
        System.out.println("\n--- Change Password ---");
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPass = scanner.nextLine();

        if (newPass.isEmpty()) {
             System.out.println("Password cannot be empty. Password not changed.");
             return;
        }

        if (newPass.equals(confirmPass)) {
            currentUser.setPassword(newPass); // Update password on the User object
            System.out.println("Password updated successfully in memory.");
            System.out.println("Changes will be saved permanently upon Logout & Save.");
        } else {
            System.out.println("Passwords do not match. Password not changed.");
        }
    }

    /**
     * Launches the appropriate Command Line Interface (CLI) based on the logged-in user's role.
     * Passes necessary data (Scanner, DataManager, Maps) to the CLI instance.
     * Handles potential ClassCastExceptions if user data is inconsistent.
     * @param user The currently logged-in User object.
     */
    private static void launchRoleCLI(User user) {
        String role = user.getRole().toLowerCase(); // Get role for switch statement

        System.out.println("\nLaunching " + user.getRole() + " Menu...");

        try {
            switch (role) {
                case "manager":
                    if (user instanceof Manager) {
                        ManagerCLI managerCLI = new ManagerCLI((Manager) user, scanner, dataManager, allProjectsMap, allUsersMap);
                        managerCLI.showManagerMenu(); // Enters the manager's menu loop
                    } else {
                         System.err.println("Error: Role is Manager, but user object type mismatch.");
                         // This indicates a potential issue in data loading or user creation logic
                    }
                    break;

                case "officer":
                    // --- Officer Section (Commented Out for now) ---
                    /*
                    if (user instanceof Officer) {
                        // Ensure OfficerCLI class exists and has a compatible constructor
                        OfficerCLI officerCLI = new OfficerCLI((Officer) user, scanner, dataManager, allProjectsMap, allUsersMap);
                        officerCLI.showOfficerMenu(); // Assuming method exists
                    } else {
                         System.err.println("Error: Role is Officer, but user object type mismatch.");
                    }
                    */
                    System.out.println("Officer role access is currently disabled in MainApp.");
                    break;

                case "applicant":
                    // --- Applicant Section (Commented Out for now) ---
                    /*
                    if (user instanceof Applicant) {
                        // Ensure ApplicantCLI class exists and has a compatible constructor
                        ApplicantCLI applicantCLI = new ApplicantCLI((Applicant) user, scanner, dataManager, allProjectsMap, allUsersMap);
                        applicantCLI.showApplicantMenu(); // Assuming method exists
                    } else {
                         System.err.println("Error: Role is Applicant, but user object type mismatch.");
                    }
                    */
                    System.out.println("Applicant role access is currently disabled in MainApp.");
                    break;

                default:
                    System.err.println("Error: Unknown user role encountered: " + user.getRole());
                    // This could happen if the users.csv contains an invalid role
            }
        } catch (ClassCastException e) {
             // Catch error if casting fails (e.g., user with role "Manager" isn't actually a Manager object)
             System.err.println("Critical Error: Failed to cast User object to its specified role type. Role: " + role);
             System.err.println("Please check data consistency in users.csv and DataManager loading logic.");
             e.printStackTrace();
        } catch (Exception e) {
            // Catch any other unexpected errors within the launched CLI
            System.err.println("An unexpected error occurred within the " + role + " CLI: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }

        // Message indicating return from the role-specific menu
        System.out.println("\nReturning from " + user.getRole() + " menu...");
        // Control flows back to the post-login menu loop.
    }

    /**
     * Helper method to read an integer input from the console robustly.
     * Handles NumberFormatException and prompts again on invalid input.
     * @param prompt The message to display before reading input.
     * @return The valid integer entered by the user.
     */
    private static int readIntInput(String prompt) {
        int input = -1; // Initialize with an invalid value
        while (true) {
            try {
                System.out.print(prompt);
                String line = scanner.nextLine().trim(); // Read the whole line
                if (line.isEmpty()) {
                    System.out.println("Input cannot be empty. Please enter a number.");
                    continue; // Ask again
                }
                input = Integer.parseInt(line); // Attempt to parse
                break; // Exit loop if parsing is successful
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
                // Loop continues to ask again
            }
        }
        return input;
    }

}