package main; // Assuming your MainApp is in the 'main' package

// Keep necessary imports for MainApp's remaining responsibilities
import cli.ManagerCLI;
// import cli.OfficerCLI; // Keep commented for now
// import cli.ApplicantCLI; // Keep commented for now
import data.DataManager;
import Actors.User;
import Actors.Manager;
import Actors.Officer;
import Actors.Applicant;
import Project.Project;

import java.util.Map;
import java.util.Scanner;
import java.util.InputMismatchException;

/**
 * Main application class for the BTO Management System.
 * Handles application startup, data loading/saving, and overall workflow.
 * Delegates login and password change interactions to a dedicated Login component (e.g., LoginCLI).
 * Launches role-specific CLIs after successful login.
 */
public class MainApp {

    // Core application components and state
    private static Scanner scanner = new Scanner(System.in);
    private static DataManager dataManager = new DataManager();
    private static Map<String, User> allUsersMap = null;
    private static Map<String, Project> allProjectsMap = null;

    public static void main(String[] args) {
        System.out.println("===== Welcome to the BTO Management System =====");

        // 1. Load All Application Data
        if (!loadAllData()) {
            System.err.println("Critical error loading data. Exiting application.");
            scanner.close();
            return;
        }
        System.out.println("Data loaded successfully.");

        User currentUser = null; // Track the currently logged-in user

        // Main Application Loop
        while (true) {

            // If no user is logged in, initiate the login process via LoginCLI
            if (currentUser == null) {
                System.out.println("\n--- Initiating Login Process ---");

                // --- DELEGATION POINT: Login ---
                // TODO: Implement LoginCLI.loginUser(Scanner, Map<String, User>)
                // This static method should:
                // 1. Accept the shared Scanner and the loaded allUsersMap.
                // 2. Handle the loop for prompting NRIC and Password.
                // 3. Allow typing 'exit' to quit (should return null).
                // 4. Validate credentials against the passed allUsersMap.
                // 5. Handle default password logic if necessary.
                // 6. Return the authenticated User object upon successful login.
                // 7. Return null if the user chooses to exit the application during login.
                // Example Call (replace with actual implementation):
                // currentUser = LoginCLI.loginUser(scanner, allUsersMap);
                currentUser = handleLogin_Placeholder(); // Using placeholder until LoginCLI is ready

                if (currentUser == null) {
                    // User chose to exit the application from the login screen
                    System.out.println("Exiting BTO Management System as requested from login.");
                    saveAllData(); // Attempt saving before final exit
                    break; // Exit the main application loop
                }
            }

            // --- Post-Login Menu ---
            // This section runs only if 'currentUser' is not null (login was successful)
            System.out.println("\n--- Main Menu ---");
            System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
            System.out.println("1. Access Role Menu");
            System.out.println("2. Change Password");
            System.out.println("0. Logout & Save");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    // Launch the role-specific CLI (This logic remains in MainApp)
                    launchRoleCLI(currentUser);
                    // After returning from CLI, loop continues showing Post-Login Menu.
                    // User needs to explicitly choose Logout (0).
                    break;

                case 2:
                    // --- DELEGATION POINT: Change Password ---
                    // TODO: Implement LoginCLI.changePassword(Scanner, User)
                    // This static method should:
                    // 1. Accept the shared Scanner and the currently logged-in User object.
                    // 2. Handle prompting for the new password and confirmation.
                    // 3. Check if passwords match.
                    // 4. If they match, call currentUser.setPassword(newPass) to update the User object in memory.
                    // 5. Print appropriate success/failure messages.
                    // 6. Return true/false indicating if the password was changed in memory.
                    // Example Call (replace with actual implementation):
                    // boolean passwordChanged = LoginCLI.changePassword(scanner, currentUser);
                    handleChangePassword_Placeholder(currentUser); // Using placeholder
                    // Note: Actual saving happens on Logout.
                    break;

                case 0:
                    // Handle logout
                    System.out.println("Logging out...");
                    saveAllData(); // Save all data before clearing user
                    System.out.println("Data saved.");
                    currentUser = null; // Clear the current user
                    System.out.println("Logged out successfully. Returning to Login screen...");
                    // Loop will continue and trigger the login prompt again
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } // End Main Application Loop (while true)

        scanner.close(); // Close scanner only when application loop terminates
        System.out.println("Application closed.");
    }

    // --- Data Loading and Saving ---

    /**
     * Loads all necessary data using the DataManager.
     * Populates allUsersMap and allProjectsMap.
     * @return true if loading was successful, false otherwise.
     */
    private static boolean loadAllData() {
        // (Implementation remains the same as provided previously)
        try {
            System.out.println("Loading users...");
            allUsersMap = dataManager.loadUsers();
            System.out.println("Loading core projects...");
            allProjectsMap = dataManager.loadProjectsCore();
            System.out.println("Loading project flats...");
            dataManager.loadProjectFlats(allProjectsMap);
            System.out.println("Loading project officers...");
            dataManager.loadProjectOfficers(allProjectsMap, allUsersMap);
            System.out.println("Loading applications...");
            dataManager.loadApplications(allProjectsMap, allUsersMap);
            // Load Enquiries if needed
            if (allUsersMap == null || allProjectsMap == null) {
                 System.err.println("Error: Data maps are null after loading attempt.");
                 return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Fatal error during data loading: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves all application data (Users and Projects) using the DataManager.
     */
    private static void saveAllData() {
        // (Implementation remains the same as provided previously)
         if (allUsersMap == null || allProjectsMap == null) {
             System.err.println("Warning: Data maps are not initialized. Skipping save operation.");
             return;
         }
        try {
            System.out.println("Saving users...");
            dataManager.saveUsers(allUsersMap);
            System.out.println("Saving all project data...");
            dataManager.saveAllProjectData(allProjectsMap);
            // Save Enquiries if needed
        } catch (Exception e) {
            System.err.println("Error encountered during data saving: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Role Launching Logic ---

    /**
     * Launches the appropriate Command Line Interface (CLI) based on the user's role.
     * @param user The currently logged-in User object.
     */
    private static void launchRoleCLI(User user) {
        // (Implementation remains the same as provided previously)
        // This part correctly stays in MainApp as it orchestrates which UI to show.
        String role = user.getRole().toLowerCase();
        System.out.println("\nLaunching " + user.getRole() + " Menu...");
        try {
            switch (role) {
                case "manager":
                    if (user instanceof Manager) {
                        ManagerCLI managerCLI = new ManagerCLI((Manager) user, scanner, dataManager, allProjectsMap, allUsersMap);
                        managerCLI.showManagerMenu();
                    } else { System.err.println("Error: Role/Type mismatch for Manager."); }
                    break;
                case "officer":
                    // TODO: !!implement officer based UI!!
                    System.out.println("Officer role access is currently disabled in MainApp."); // Keep commented out
                    break;
                case "applicant":
                    // TODO: implement applicant based UI!!
                     System.out.println("Applicant role access is currently disabled in MainApp."); // Keep commented out
                    break;
                default:
                    System.err.println("Error: Unknown user role: " + user.getRole());
            }
        } catch (ClassCastException e) {
             System.err.println("Critical Error: Failed to cast User object. Role: " + role); e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred within the " + role + " CLI: " + e.getMessage()); e.printStackTrace();
        }
        System.out.println("\nReturning from " + user.getRole() + " menu...");
    }


    // --- Input Helper ---

    /**
     * Helper method to read an integer input from the console robustly.
     * @param prompt The message to display before reading input.
     * @return The valid integer entered by the user.
     */
    private static int readIntInput(String prompt) {
        // (Implementation remains the same as provided previously)
         int input = -1;
        while (true) {
            try {
                System.out.print(prompt);
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) { System.out.println("Input cannot be empty."); continue; }
                input = Integer.parseInt(line);
                break;
            } catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a whole number."); }
        }
        return input;
    }


    // --- PLACEHOLDER METHODS (Remove these once LoginCLI is implemented) ---

    /**
     * Placeholder for LoginCLI.loginUser(). Simulates successful login for testing.
     * REMOVE THIS and use the actual LoginCLI call.
     * @return A User object or null.
     */
    private static User handleLogin_Placeholder() {
         System.out.println("<<<< Placeholder Login Activated >>>>");
         System.out.print("Enter NRIC to simulate login (or 'exit'): ");
         String nric = scanner.nextLine().trim();
         if (nric.equalsIgnoreCase("exit") || !allUsersMap.containsKey(nric)) {
              if (!nric.equalsIgnoreCase("exit")) System.out.println("Placeholder: User not found.");
              return null;
         }
         System.out.println("Placeholder: Simulating successful login for " + nric);
         return allUsersMap.get(nric); // Return found user directly without password check
    }

     /**
      * Placeholder for LoginCLI.changePassword(). Simulates password change.
      * REMOVE THIS and use the actual LoginCLI call.
      * @param currentUser The user object.
      */
    private static void handleChangePassword_Placeholder(User currentUser) {
         System.out.println("<<<< Placeholder Change Password Activated >>>>");
         System.out.print("Enter new simulated password: ");
         String newPass = scanner.nextLine();
         if (!newPass.isEmpty()) {
              currentUser.setPassword(newPass);
              System.out.println("Placeholder: Password updated in memory for " + currentUser.getName());
         } else {
             System.out.println("Placeholder: Password cannot be empty.");
         }
    }
}