package main; // Assuming your MainApp is in the 'main' package

import cli.LoginCLI;
// Keep necessary imports for MainApp's remaining responsibilities
import cli.ManagerCLI;
import cli.OfficerCLI;
// import cli.OfficerCLI; // Keep commented for now
import cli.ApplicantCLI; 
import data.DataManager;
import Actors.User;
import Actors.Manager;
import Actors.Officer;
import Actors.Applicant;
import Project.Project;
import Services.EnquiryService;

import java.util.Map;
import java.util.Scanner;

/**
 * Main application class for the BTO Management System.
 * Handles application startup, data loading/saving, and overall workflow.
 * Delegates login and password change interactions to a dedicated Login component (e.g., LoginCLI).
 * Launches role-specific CLIs after successful login.
 */
public class MainApp {

    private static Scanner scanner = new Scanner(System.in);
    private static DataManager dataManager = new DataManager();
    private static EnquiryService enquiryService = new EnquiryService();
    private static Map<String, User> allUsersMap = null;
    private static Map<String, Project> allProjectsMap = null;

    /**
     * Application startup and main control loop.
     * Loads data, handles login, and routes to user role menus.
     * @param args Standard command line arguments
     */
    public static void main(String[] args) {
        System.out.println("===== Welcome to the BTO Management System =====");

        if (!loadAllData()) {
            System.err.println("Critical error loading data. Exiting application.");
            scanner.close();
            return;
        }
        System.out.println("Data loaded successfully.");

        User currentUser = null; 

        while (true) {

            if (currentUser == null) {
                System.out.println("\n--- Initiating Login Process ---");

                currentUser = LoginCLI.loginUser(scanner, allUsersMap);
                
                if (currentUser == null) {
                    System.out.println("Exiting BTO Management System as requested from login.");
                    saveAllData(); 
                    break; 
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
                    launchRoleCLI(currentUser);
                    break;

                case 2:   
                    boolean passwordChanged = LoginCLI.changePassword(scanner, currentUser);
                    if (passwordChanged) {
                        currentUser = null;
                        System.out.println("Saving updated user data...");
                        saveAllData();
                        System.out.println("User data saved.");
                    }
                    break;

                case 0:
                    System.out.println("Logging out...");
                    saveAllData(); 
                    System.out.println("Data saved.");
                    currentUser = null; 
                    System.out.println("Logged out successfully. Returning to Login screen...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } 

        scanner.close();
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
            dataManager.loadProjectFlats(allProjectsMap);
            System.out.println("Loading project officers...");
            dataManager.loadProjectOfficers(allProjectsMap, allUsersMap);
            System.out.println("Loading applications...");
            dataManager.loadApplications(allProjectsMap, allUsersMap);
            System.out.println("Loading enquiries and replies...");
            dataManager.loadEnquiries(enquiryService); 
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
         if (allUsersMap == null || allProjectsMap == null) {
             System.err.println("Warning: Data maps are not initialized. Skipping save operation.");
             return;
         }
        try {
            System.out.println("Saving users...");
            dataManager.saveUsers(allUsersMap);
            System.out.println("Saving all project data...");
            dataManager.saveAllProjectData(allProjectsMap, allUsersMap);
            System.out.println("Saving enquiries and replies...");
            dataManager.saveEnquiries(enquiryService); 
        } catch (Exception e) {
            System.err.println("Error encountered during data saving: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Launches the appropriate Command Line Interface (CLI) based on the user's role.
     * @param user The currently logged-in User object.
     */
    private static void launchRoleCLI(User user) {
        String role = user.getRole().toLowerCase();
        System.out.println("\nLaunching " + user.getRole() + " Menu...");
        try {
            switch (role) {
                case "manager":
                    if (user instanceof Manager) {
                        ManagerCLI managerCLI = new ManagerCLI((Manager) user,
                                                    scanner,
                                                    dataManager,
                                                    enquiryService, 
                                                    allProjectsMap, 
                                                    allUsersMap);
                        managerCLI.showManagerMenu();
                    } else { 
                        System.err.println("Error: Role/Type mismatch for Manager."); 
                    }
                    break;
                case "officer":
                	if (user instanceof Officer) {
                        OfficerCLI officerCLI = new OfficerCLI((Officer) user,
                                                    scanner,
                                                    dataManager,
                                                    enquiryService, 
                                                    allProjectsMap, 
                                                    allUsersMap);
                        officerCLI.showOfficerMenu();
                    } else {
                        System.err.println("Error: Role/Type mismatch for Officer.");
                    }
                    break;
                case "applicant":
                    if (user instanceof Applicant) {
                        ApplicantCLI applicantCLI = new ApplicantCLI((Applicant) user, 
                                                        scanner, 
                                                        enquiryService, 
                                                        dataManager, 
                                                        allProjectsMap,
                                                        allUsersMap);
                        applicantCLI.showApplicantMenu();
                    } else { 
                        System.err.println("Error: Role/Type mismatch for Applicant."); 
                    }
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


    /**
     * Helper method to read an integer input from the console robustly.
     * @param prompt The message to display before reading input.
     * @return The valid integer entered by the user.
     */
    private static int readIntInput(String prompt) {
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
