package main; // Assuming your MainApp is in the 'main' package

import cli.LoginCLI;
// Keep necessary imports for MainApp's remaining responsibilities
import cli.ManagerCLI;
// import cli.OfficerCLI; // Keep commented for now
import cli.ApplicantCLI; 
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

                currentUser = LoginCLI.loginUser(scanner, allUsersMap);
                
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
                    boolean passwordChanged = LoginCLI.changePassword(scanner, currentUser);
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
                    if (user instanceof Manager) {
                        ApplicantCLI applicantCLI = new ApplicantCLI((Applicant) user, scanner, enquiryService, dataManager, allProjectsMap);
                        applicantCLI.showApplicantMenu();
                    } else { System.err.println("Error: Role/Type mismatch for Applicant."); }
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


}