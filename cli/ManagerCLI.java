package cli;

import Actors.Manager;
import Actors.User;       // Needed for type casting or methods accepting User
import Actors.Applicant;
import Actors.Officer;
import Project.Project;
// import cli.EnquiryCLI;    // Assumes EnquiryCLI is in this package
import data.DataManager; 

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;     // Needed to accept the main data maps
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;

/**
 * Provides a Command Line Interface (CLI) for users logged in as a Manager.
 * Handles user input, calls methods on the Manager logic object, and displays results.
 * Interacts with DataManager to fetch lists for selection and EnquiryCLI for enquiry handling.
 * Does NOT handle saving data directly; expects the calling class (e.g., MainApp)
 * to save data after the manager session ends (e.g., on logout).
 */
public class ManagerCLI {
    private final Manager manager;        // The specific Manager logic object for this session
    private final Scanner scanner;
    private final DataManager dataManager; // Instance to interact with data layer
    private final EnquiryCLI enquiryCLI;    // Instance for handling enquiry UI flows

    // References to the main application data maps, loaded at startup
    private final Map<String, Project> allProjectsMap;
    private final Map<String, User> allUsersMap;

    /**
     * Constructor for ManagerCLI.
     *
     * @param manager        The logged-in Manager object (contains user info and logic).
     * @param scanner        The Scanner instance for reading user input.
     * @param dataManager    The DataManager instance for fetching data.
     * @param allProjectsMap A reference to the Map holding all loaded Project objects.
     * @param allUsersMap    A reference to the Map holding all loaded User objects.
     */
    public ManagerCLI(Manager manager, Scanner scanner, DataManager dataManager,
                      Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        this.manager = manager;
        this.scanner = scanner;
        this.dataManager = dataManager;
        // Pass the scanner to EnquiryCLI if its constructor needs it
        // this.enquiryCLI = new EnquiryCLI(scanner);
        // Store references to the maps containing the application's current state
        this.allProjectsMap = allProjectsMap;
        this.allUsersMap = allUsersMap;
    }

    /**
     * Displays the main menu for the Manager and handles user choices.
     * Continues looping until the user chooses to logout (enters 0).
     * Calls handler methods for each action.
     * Note: Saving data is handled externally after this method returns.
     */
    public void showManagerMenu() {
        int choice;
        do {
            System.out.println("\n--- Manager Menu (" + manager.getName() + ") ---"); 
            System.out.println("1. Create New Project");
            System.out.println("2. View/Edit/Delete My Projects");
            System.out.println("3. Toggle Project Visibility");
            System.out.println("4. Approve/Reject Officer Registration");
            System.out.println("5. View Approved Officers (Per Project)");
            System.out.println("6. Accept/Reject Applicant Application");
            System.out.println("7. Accept/Reject Applicant Withdrawal");
            System.out.println("8. Generate Applicant Report");
            System.out.println("9. Handle Enquiries");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            choice = readIntInput();

            try {
                switch (choice) {
                    case 1: handleCreateProject(); break;
                    case 2: handleViewEditDeleteProjects(); break;
                    case 3: handleToggleProjectVisibility(); break;
                    case 4: handleUpdateOfficerReg(); break;
                    case 5: handleViewApprovedOfficers(); break;
                    case 6: handleUpdateApplicant(); break;
                    case 7: handleUpdateWithdrawal(); break;
                    case 8: handleGenerateReport(); break;
                    case 9:
                        // Pass the Manager object (which is a User) to the EnquiryCLI
                        enquiryCLI.showEnquiryMenuForStaff(this.manager);
                        break;
                    case 0:
                        System.out.println("Preparing to logout manager " + manager.getName() + "...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            } catch (Exception e) {
                 System.err.println("An unexpected error occurred during the action: " + e.getMessage());
                 // log the stack trace for debugging: e.printStackTrace();
            }
        } while (choice != 0);
    }

    // --- Handler Methods for Menu Options ---

    /**
     * Handles the creation of a new project. Prompts for details, calls the Manager logic,
     * and updates the main project map if successful.
     */
    private void handleCreateProject() {
        System.out.println("\n--- Create New Project ---");
        System.out.print("Enter Project Name: "); String name = scanner.nextLine();
        // Check if project name already exists in the main map
        if (allProjectsMap.containsKey(name)) {
             System.err.println("Error: Project name '" + name + "' already exists.");
             return;
        }
        System.out.print("Set initial visibility (true/false): "); boolean visibility = readBooleanInput();
        System.out.print("Enter Neighbourhood: "); String neighbourhood = scanner.nextLine();
        LocalDate openingDate = readDateInput("Enter Application Opening Date (YYYY-MM-DD): ");
        LocalDate closingDate = readDateInput("Enter Application Closing Date (YYYY-MM-DD): ");
        if (openingDate != null && closingDate != null && closingDate.isBefore(openingDate)) { System.err.println("Error: Closing date cannot be before opening date."); return; }
        System.out.print("Enter Number of 2-Room Flats: "); int num2Rooms = readIntInput();
        System.out.print("Enter Number of 3-Room Flats: "); int num3Rooms = readIntInput();

        // Call Manager logic
        Project newProject = manager.createProject(name, visibility, neighbourhood, openingDate, closingDate, num2Rooms, num3Rooms);

        if (newProject != null) {
            System.out.println("Project '" + newProject.getName() + "' created successfully IN MEMORY.");
            // Add the newly created project to the main map being tracked by the application
            allProjectsMap.put(newProject.getName(), newProject);
        } else {
            System.err.println("Failed to create project (check console for specific error like date clash).");
        }
    }

    /**
     * Handles viewing, editing, or deleting projects managed by the current manager.
     */
    private void handleViewEditDeleteProjects() {
        // Get projects specific to this manager instance's list
        List<Project> projects = manager.getAllProjectsManagedByThisManager();
        Project selectedProjectStub = selectProject(projects, "Select a project to View/Edit/Delete:"); // This is from the manager's list
        if (selectedProjectStub == null) return; // User cancelled or no projects

        // IMPORTANT: Get the actual project object from the main map to ensure edits affect the shared state
        Project projectToModify = allProjectsMap.get(selectedProjectStub.getName());
        if (projectToModify == null) {
             System.err.println("Error: Selected project ('"+selectedProjectStub.getName()+"') not found in main application data. Data might be inconsistent.");
             return;
        }

        System.out.println("\n--- Project: " + projectToModify.getName() + " ---");
        System.out.println("1. View Details");
        System.out.println("2. Edit Project");
        System.out.println("3. Delete Project");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        int choice = readIntInput();

        switch (choice) {
            case 1:
                System.out.println("\n--- Project Details ---");
                // Call getProjectDetails with the object from the main map
                System.out.println(manager.getProjectDetails(projectToModify));
                break;
            case 2:
                // Pass the object from the main map to be edited
                handleEditProject(projectToModify);
                break;
            case 3:
                 // Pass the object from the main map to be deleted
                handleDeleteProject(projectToModify);
                break;
            case 0: break;
            default: System.out.println("Invalid choice."); break;
        }
    }

    /**
     * Handles editing details of a specific project.
     * @param projectToEdit The actual Project object from the main map to modify.
     */
    private void handleEditProject(Project projectToEdit) {
        System.out.println("\n--- Editing Project: " + projectToEdit.getName() + " ---");
        System.out.println("(Leave blank or enter invalid value to keep existing)");

        // Get Optional Inputs using readOptional... helpers
        System.out.print("Enter new Place Name (Current: " + projectToEdit.getPlaceName() + "): "); String placeName = readOptionalStringInput();
        System.out.print("Enter new Neighbourhood (Current: " + projectToEdit.getNeighbourhood() + "): "); String neighbourhood = readOptionalStringInput();
        LocalDate openingDate = readOptionalDateInput("Enter new Application Opening Date (YYYY-MM-DD) (Current: " + projectToEdit.getAppOpeningDate() + "): ");
        LocalDate closingDate = readOptionalDateInput("Enter new Application Closing Date (YYYY-MM-DD) (Current: " + projectToEdit.getAppClosingDate() + "): ");
        System.out.print("Enter new Number of 2-Room Flats (Current: " + projectToEdit.getNum2RoomUnits() + ", Enter non-number to skip): "); Integer num2Rooms = readOptionalIntInput();
        System.out.print("Enter new Number of 3-Room Flats (Current: " + projectToEdit.getNum3RoomUnits() + ", Enter non-number to skip): "); Integer num3Rooms = readOptionalIntInput();

        // Validate dates if changed
        LocalDate finalOpening = (openingDate != null) ? openingDate : projectToEdit.getAppOpeningDate();
        LocalDate finalClosing = (closingDate != null) ? closingDate : projectToEdit.getAppClosingDate();
        if (finalOpening != null && finalClosing != null && finalClosing.isBefore(finalOpening)) { 
            System.err.println("Error: Closing date cannot be before opening date. Edit cancelled."); 
            return;
        }

        // Call Manager logic, operating on the object from the map
        boolean success = manager.editProject(projectToEdit, placeName, neighbourhood, openingDate, closingDate, num2Rooms, num3Rooms);

        if (success) {
            System.out.println("Project updated successfully IN MEMORY.");
            // NO SAVE TO FILE HERE
        } else {
            System.err.println("Failed to update project (check console for errors like date clash).");
        }
    }

     /**
      * Handles deleting a specific project.
      * @param projectToDelete The actual Project object from the main map to delete.
      */
     private void handleDeleteProject(Project projectToDelete) {
        System.out.print("Are you sure you want to delete project '" + projectToDelete.getName() + "'? (yes/no): ");
        if (readYesNoInput()) {
            // Call Manager logic (might also update manager's internal list if applicable)
            boolean success = manager.delProject(projectToDelete);
            if (success) {
                 // Also remove from the main map tracked by the application
                 allProjectsMap.remove(projectToDelete.getName());
                 System.out.println("Project deleted successfully IN MEMORY.");
                 // NO SAVE TO FILE HERE
            } else {
                 System.err.println("Failed to delete project (may not be managed by you or already removed).");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    /**
     * Handles toggling the visibility of a project managed by the current manager.
     */
    private void handleToggleProjectVisibility() {
        List<Project> projects = manager.getAllProjectsManagedByThisManager();
        Project selectedProjectStub = selectProject(projects, "Select a project to toggle visibility:");
        if (selectedProjectStub == null) return;

        // Get the actual object from the main map
        Project projectToToggle = allProjectsMap.get(selectedProjectStub.getName());
         if (projectToToggle == null) {
              System.err.println("Error: Selected project ('"+selectedProjectStub.getName()+"') not found in main application data.");
              return;
         }

        boolean currentVisibility = projectToToggle.getVisibility();
        System.out.println("Project '" + projectToToggle.getName() + "' is currently " + (currentVisibility ? "Visible" : "Hidden"));
        System.out.print("Toggle visibility? (yes/no): ");
        if (readYesNoInput()) {
            boolean success = manager.toggleProject(projectToToggle); // Operate on the map's object
            if (success) {
                 System.out.println("Project visibility toggled successfully IN MEMORY to " + (!currentVisibility ? "Visible" : "Hidden") + "!");
                 // NO SAVE TO FILE HERE
            } else {
                 System.err.println("Failed to toggle project visibility (maybe not managed by you?).");
            }
        } else {
            System.out.println("Toggle cancelled.");
        }
    }

    /**
     * Handles approving or rejecting pending officer registrations for projects
     * managed by the current manager.
     */
    private void handleUpdateOfficerReg() {
         System.out.println("\n--- Update Officer Registration ---");
         // Use DataManager helper, passing the main maps for context
         List<Officer> pendingOfficers = dataManager.getAllPendingOfficersForManager(manager.getName(), allProjectsMap, allUsersMap);

         if (pendingOfficers.isEmpty()) {
             System.out.println("No pending officer registrations found for your projects.");
             return;
         }

         Officer selectedOfficer = selectOfficer(pendingOfficers, "Select an officer to Approve/Reject:");
         if (selectedOfficer == null) return; // User cancelled

         // Get the actual Officer object from the main users map to ensure status updates are reflected globally
         Officer officerFromMap = (Officer) allUsersMap.get(selectedOfficer.getNric());
          if (officerFromMap == null) {
               System.err.println("Error: Selected officer not found in main application data.");
               return;
          }

         System.out.print("Approve this officer registration? (yes/no): ");
         boolean approve = readYesNoInput();

         // Call Manager logic, passing the globally tracked Officer object
         boolean success = manager.updateRegOfficer(officerFromMap, approve);

         if (success) {
             System.out.println("Officer registration status updated successfully IN MEMORY.");
             // NO SAVE TO FILE HERE
         } else {
             System.err.println("Failed to update officer registration status (Officer might not be pending in your projects anymore).");
         }
     }

    /**
     * Handles viewing the list of approved officers for a selected project.
     */
    private void handleViewApprovedOfficers() {
         System.out.println("\n--- View Approved Officers ---");
         List<Project> projects = manager.getAllProjectsManagedByThisManager();
         Project selectedProjectStub = selectProject(projects, "Select a project to view its approved officers:");
         if (selectedProjectStub == null) return;

         // Get actual project from map
         Project projectFromMap = allProjectsMap.get(selectedProjectStub.getName());
         if (projectFromMap == null) { System.err.println("Error: Project not found."); return; }

         // Get the list of approved officers (Manager method might return copies or originals)
         List<Officer> approvedOfficers = manager.getApprovedOfficers(projectFromMap);

         if (approvedOfficers.isEmpty()) {
             System.out.println("No approved officers found for project '" + projectFromMap.getName() + "'.");
         } else {
             System.out.println("Approved Officers for Project '" + projectFromMap.getName() + "':");
             // It's safer to get names from the main user map in case the list contains stubs
             for (Officer officerStub : approvedOfficers) {
                  User officerUser = allUsersMap.get(officerStub.getNric());
                  System.out.println("- " + (officerUser != null ? officerUser.getName() : "Unknown Officer NRIC: " + officerStub.getNric()));
             }
         }
     }

    /**
     * Handles accepting or rejecting pending applicant applications for projects
     * managed by the current manager.
     */
    private void handleUpdateApplicant() {
         System.out.println("\n--- Accept/Reject Applicant Application ---");
         // Use DataManager helper, passing maps
         List<Applicant> pendingApplicants = dataManager.getAllPendingApplicantsForManager(manager.getName(), allProjectsMap, allUsersMap);

         if (pendingApplicants.isEmpty()) {
             System.out.println("No pending applicant applications found for your projects.");
             return;
         }

         Applicant selectedApplicant = selectApplicant(pendingApplicants, "Select an applicant to Accept/Reject:");
         if (selectedApplicant == null) return;

         // Get the actual Applicant object from the main users map
         Applicant applicantFromMap = (Applicant) allUsersMap.get(selectedApplicant.getNric());
         if (applicantFromMap == null) { System.err.println("Error: Selected applicant not found."); return; }

         System.out.print("Accept this applicant? (yes/no): ");
         boolean accept = readYesNoInput();

         // Call Manager logic with the object from the map
         boolean success = manager.updateApp(applicantFromMap, accept);

         if (success) {
             System.out.println("Applicant status updated successfully IN MEMORY.");
             // NO SAVE TO FILE HERE
         } else {
             System.err.println("Failed to update applicant status (check console for errors like 'no room').");
         }
     }

    /**
     * Handles accepting or rejecting pending applicant withdrawal requests for projects
     * managed by the current manager.
     */
     private void handleUpdateWithdrawal() {
        System.out.println("\n--- Accept/Reject Applicant Withdrawal ---");
        // Use DataManager helper, passing maps
        List<Applicant> withdrawalApplicants = dataManager.getAllWithdrawalApplicantsForManager(manager.getName(), allProjectsMap, allUsersMap);

        if (withdrawalApplicants.isEmpty()) {
            System.out.println("No pending applicant withdrawals found for your projects.");
            return;
        }

        Applicant selectedApplicant = selectApplicant(withdrawalApplicants, "Select an applicant withdrawal request to Accept/Reject:");
        if (selectedApplicant == null) return;

        // Get the actual Applicant object from the main users map
        Applicant applicantFromMap = (Applicant) allUsersMap.get(selectedApplicant.getNric());
        if (applicantFromMap == null) { System.err.println("Error: Selected applicant not found."); return; }


        System.out.print("Accept this withdrawal? (yes/no): ");
        boolean accept = readYesNoInput();

        // Call Manager logic with object from map
        boolean success = manager.updateWithdrawal(applicantFromMap, accept);

         if (success) {
             System.out.println("Applicant withdrawal processed successfully IN MEMORY.");
             // NO SAVE TO FILE HERE
         } else {
             System.err.println("Failed to process applicant withdrawal.");
         }
     }

    /**
     * Handles generating an applicant report for a selected project based on a filter key.
     */
    private void handleGenerateReport() {
        System.out.println("\n--- Generate Applicant Report ---");
        List<Project> projects = manager.getAllProjectsManagedByThisManager();
        Project selectedProjectStub = selectProject(projects, "Select a project for the report:");
        if (selectedProjectStub == null) return;

        // Get actual project from map
        Project projectFromMap = allProjectsMap.get(selectedProjectStub.getName());
         if (projectFromMap == null) { System.err.println("Error: Project not found."); return; }

        System.out.println("Available Filters: all, married, unmarried, flat2room, flat3room, married_flat2room");
        System.out.print("Enter filter key: ");
        String filterKey = scanner.nextLine().trim();

        // Call Manager logic with object from map
        List<String> reportEntries = manager.generateApplicantReport(projectFromMap, filterKey);

        if (reportEntries.isEmpty()) {
            System.out.println("Report generated, but no successful applicants matched the filter criteria for project '" + projectFromMap.getName() + "'.");
        } else {
            System.out.println("\n--- Applicant Report for Project: " + projectFromMap.getName() + " (Filter: " + filterKey + ") ---");
            reportEntries.forEach(System.out::println);
            System.out.println("--- End of Report ---");
            // Consider option to save report to file here if desired
        }
    }


    // --- Helper Methods for Input and Selection (Keep implementations from previous response) ---

    private int readIntInput() { try { int i = scanner.nextInt(); scanner.nextLine(); return i; } catch (InputMismatchException e) { scanner.nextLine(); System.err.println("Invalid number."); return -1; } }
    private Integer readOptionalIntInput() { try { String line = scanner.nextLine(); return line.trim().isEmpty() ? null : Integer.parseInt(line); } catch (NumberFormatException e) { System.out.println("(Invalid number, keeping existing)"); return null; } }
    private boolean readBooleanInput() { String input = scanner.nextLine().trim(); return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes"); }
    private boolean readYesNoInput() { String input = scanner.nextLine().trim(); return input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y"); }
    private LocalDate readDateInput(String prompt) { LocalDate date = null; while (date == null) { System.out.print(prompt); try { date = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException e) { System.err.println("Invalid format (YYYY-MM-DD)."); } } return date; }
    private LocalDate readOptionalDateInput(String prompt) { System.out.print(prompt); String input = scanner.nextLine().trim(); if (input.isEmpty()) return null; try { return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException e) { System.err.println("Invalid format, keeping existing."); return null; } }
    private String readOptionalStringInput() { String input = scanner.nextLine().trim(); return input.isEmpty() ? null : input; }
    private Project selectProject(List<Project> projects, String prompt) { if (projects == null || projects.isEmpty()) { System.out.println("No projects available to select."); return null; } System.out.println("\n" + prompt); for (int i = 0; i < projects.size(); i++) System.out.println((i + 1) + ". " + projects.get(i).getName()); System.out.println("0. Cancel"); System.out.print("Enter choice: "); int choice = readIntInput(); if (choice <= 0 || choice > projects.size()) { System.out.println("Selection cancelled or invalid."); return null; } return projects.get(choice - 1); }
    private Officer selectOfficer(List<Officer> officers, String prompt) { if (officers == null || officers.isEmpty()) { System.out.println("No officers available to select."); return null; } System.out.println("\n" + prompt); for (int i = 0; i < officers.size(); i++) System.out.println((i + 1) + ". " + officers.get(i).getName()); System.out.println("0. Cancel"); System.out.print("Enter choice: "); int choice = readIntInput(); if (choice <= 0 || choice > officers.size()) { System.out.println("Selection cancelled or invalid."); return null; } return officers.get(choice - 1); }
    private Applicant selectApplicant(List<Applicant> applicants, String prompt) { if (applicants == null || applicants.isEmpty()) { System.out.println("No applicants available to select."); return null; } System.out.println("\n" + prompt); for (int i = 0; i < applicants.size(); i++) System.out.println((i + 1) + ". " + applicants.get(i).getName() + " (Project: " + (applicants.get(i).getProject() != null ? applicants.get(i).getProject().getName() : "N/A") + ")"); System.out.println("0. Cancel"); System.out.print("Enter choice: "); int choice = readIntInput(); if (choice <= 0 || choice > applicants.size()) { System.out.println("Selection cancelled or invalid."); return null; } return applicants.get(choice - 1); }

} 