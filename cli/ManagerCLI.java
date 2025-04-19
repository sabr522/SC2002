package cli;

import Actors.Manager;
import Actors.User; // Needed for type casting or methods accepting User
import Actors.Applicant;
import Actors.Officer;
import Project.Project;
import Services.EnquiryService;
import data.DataManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map; // Needed to accept the main data maps
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.stream.Collectors; // Added for cleaner filtering

/**
 * Provides a Command Line Interface (CLI) for users logged in as a Manager.
 * Handles user input, calls methods on the Manager logic object, and displays results.
 * Interacts with DataManager to fetch lists for selection and potentially EnquiryCLI for enquiry handling.
 * Modifies the main data maps (allProjectsMap, allUsersMap) directly for operations like creation/deletion.
 * Does NOT handle saving data directly; expects the calling class (e.g., MainApp)
 * to save data after the manager session ends (e.g., on logout).
 */
public class ManagerCLI {

    private final Manager manager; // The specific Manager logic object for this session
    private final Scanner scanner;
    private final DataManager dataManager; // Instance to interact with data layer helpers
    private final EnquiryService enquiryService; // Instance for handling enquiry UI flows

    // References to the main application data maps, loaded at startup
    private final Map<String, Project> allProjectsMap;
    private final Map<String, User> allUsersMap;

    /**
     * Constructor for ManagerCLI.
     *
     * @param manager        The logged-in Manager object (contains user info and logic).
     * @param scanner        The Scanner instance for reading user input.
     * @param dataManager    The DataManager instance for fetching data.
     * @param enquiryService Main logic for holding all enquires/replies
     * @param allProjectsMap A reference to the Map holding all loaded Project objects.
     * @param allUsersMap    A reference to the Map holding all loaded User objects.
     */
    public ManagerCLI(Manager manager, Scanner scanner, DataManager dataManager, EnquiryService enquiryService,
                      Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        this.manager = manager;
        this.scanner = scanner;
        this.dataManager = dataManager;
        this.enquiryService = enquiryService; 
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
            System.out.println("2. View/Edit/Delete MY Created Projects"); 
            System.out.println("3. View ALL Projects (Regardless of Creator/Visibility)");
            System.out.println("4. Toggle MY Project Visibility");
            System.out.println("5. Approve/Reject Officer Registration"); 
            System.out.println("6. View Approved Officers (Per MY Project)"); 
            System.out.println("7. Accept/Reject Applicant Application (For MY Projects)");
            System.out.println("8. Accept/Reject Applicant Withdrawal (For MY Projects)");
            System.out.println("9. Generate Applicant Report (For MY Project)");
            System.out.println("10. Manage Project Enquiries");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            choice = readIntInput();

            try {
                switch (choice) {
                    case 1: handleCreateProject(); break;
                    case 2: handleViewEditDeleteProjects(); break; 
                    case 3: handleViewAllProjects(); break;
                    case 4: handleToggleProjectVisibility(); break;
                    case 5: handleUpdateOfficerReg(); break;
                    case 6: handleViewApprovedOfficers(); break; 
                    case 7: handleUpdateApplicant(); break;
                    case 8: handleUpdateWithdrawal(); break;
                    case 9: handleGenerateReport(); break; 
                    case 10: manageAllEnquiries(); break;
                    case 0:
                        System.out.println("Preparing to logout manager " + manager.getName() + "...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred during the action: " + e.getMessage());
                 e.printStackTrace(); 
            }

        } while (choice != 0);
    }

    // --- Helper Method to Filter Projects by Current Manager ---
    private List<Project> getProjectsManagedByThisManager() {
        String currentManagerName = manager.getName(); // Get manager's name
        return allProjectsMap.values().stream() // Stream all projects from the main map
                .filter(p -> p != null && currentManagerName.equals(p.getCreatorName())) // Filter by creator name
                .collect(Collectors.toList()); // Collect results into a list
    }

    /**
     * Handles viewing/replying to enquiries, allowing manager to select a managed project by index or view all.
     */
    private void manageAllEnquiries() {
        System.out.println("\n--- Enquiry Management (Manager) ---");

        // 1. Get and display projects managed by this manager with indices
        List<Project> managedProjects = getProjectsManagedByThisManager();
        Map<Integer, String> optionMap = new HashMap<>(); 
        int currentIndex = 1;

        if (managedProjects.isEmpty()) {
            System.out.println("You are not currently managing any projects.");
        } else {
            System.out.println("Projects you manage:");
            for (Project p : managedProjects) {
                if (p != null) {
                    System.out.printf("%d. %s (%s)%n", currentIndex, p.getName(), p.getNeighbourhood());
                    optionMap.put(currentIndex, p.getName());
                    currentIndex++;
                }
            }
        }
        System.out.println("\n0. Manage/View All Enquiries (Unfiltered)");

        // 2. Get user choice by index
        int choice = -1;
        String targetProjectName = null; 

        if (!optionMap.isEmpty()) { 
            while (true) {
                System.out.println("Enter project number to filter enquiries (or 0 for All): ");
                choice = readIntInput();
                if (choice == 0) {
                    break;
                }
                if (optionMap.containsKey(choice)) {
                    targetProjectName = optionMap.get(choice);
                    System.out.println("Selected project: " + targetProjectName);
                    break; 
                } else {
                    System.out.println("Invalid selection. Please enter a number from the list or 0.");
                }
            }
        } else {
            System.out.println("Viewing options for all enquiries as no specific projects are managed.");
            choice = 0;
        }

        // 3. Instantiate EnquiryCLI and show menu
        System.out.println("Launching Enquiry Menu" + (targetProjectName != null ? " for project " + targetProjectName : " (All Projects View)"));
        EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, manager.getNric(), true, true, 
                                                this.scanner, this.allUsersMap, allProjectsMap);
        enquiryCLI.showEnquiryMenu(targetProjectName); 
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

        if (openingDate != null && closingDate != null && closingDate.isBefore(openingDate)) {
             System.err.println("Error: Closing date cannot be before opening date."); return;
        }

        System.out.print("Enter Number of 2-Room Flats: "); int num2Rooms = readIntInput();
        System.out.print("Enter Number of 3-Room Flats: "); int num3Rooms = readIntInput();
        List<Project> managedProjects = getProjectsManagedByThisManager();

        // Call Manager logic (Manager now uses its own name as creator)
        Project newProject = manager.createProject(name, visibility, neighbourhood,
                                                  openingDate, closingDate, num2Rooms, num3Rooms, managedProjects);

        if (newProject != null) {
            // Add the newly created project to the main map being tracked by the application
            // This makes it immediately visible in the current session.
            allProjectsMap.put(newProject.getName(), newProject);
            System.out.println("Project '" + newProject.getName() + "' created successfully IN MEMORY.");
            System.out.println("Changes will be saved on logout.");
        } else {
            // Error message (e.g., date clash) should have been printed by manager.createProject
            System.err.println("Failed to create project (check console for specific error like date clash).");
        }
    }

    /**
     * Handles viewing, editing, or deleting projects managed by the current manager.
     */
    private void handleViewEditDeleteProjects() {
        // Get projects specific to this manager instance's list by filtering the main map
        List<Project> projects = getProjectsManagedByThisManager();

        Project selectedProjectStub = selectProject(projects, "Select a project to View/Edit/Delete:"); // This is from the manager's list

        if (selectedProjectStub == null) return; // User cancelled or no projects

        // IMPORTANT: Get the actual project object from the main map to ensure edits/deletes affect the shared state
        Project projectToModify = allProjectsMap.get(selectedProjectStub.getName());

        if (projectToModify == null) {
            System.err.println("Error: Selected project ('" + selectedProjectStub.getName() + "') not found in main application data. Data might be inconsistent.");
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
                manager.getProjectDetails(projectToModify); 
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
        String originalName = projectToEdit.getName();
        System.out.println("\n--- Editing Project: " + originalName+ " ---");
        System.out.println("(Leave blank or enter invalid value to keep existing)");

        // Get Optional Inputs using readOptional... helpers
        System.out.print("Enter new Place Name (Current: " + originalName+ "): ");
        String placeName = readOptionalStringInput();

        System.out.print("Enter new Neighbourhood (Current: " + projectToEdit.getNeighbourhood() + "): ");
        String neighbourhood = readOptionalStringInput();

        LocalDate openingDate = readOptionalDateInput("Enter new Application Opening Date (YYYY-MM-DD) (Current: " + projectToEdit.getAppOpeningDate() + "): ");
        LocalDate closingDate = readOptionalDateInput("Enter new Application Closing Date (YYYY-MM-DD) (Current: " + projectToEdit.getAppClosingDate() + "): ");
        System.out.print("Enter new Number of 2-Room Flats (Current: " + projectToEdit.getNo2Room() + ", Enter non-number to skip): ");
        Integer num2Rooms = readOptionalIntInput();
        System.out.print("Enter new Number of 3-Room Flats (Current: " + projectToEdit.getNo3Room() + ", Enter non-number to skip): ");
        Integer num3Rooms = readOptionalIntInput();

        List<Project> managedProjects = getProjectsManagedByThisManager();

        // Call Manager logic, operating on the object from the map
        // Manager.editProject now primarily calls setters on the project object passed to it.
        boolean success = manager.editProject(projectToEdit, placeName, neighbourhood,
                                            openingDate, closingDate, num2Rooms, num3Rooms, managedProjects);

        if (success) {
            System.out.println("Project updated successfully IN MEMORY.");
            String newName = projectToEdit.getName(); 
            if (!originalName.equals(newName)) {
                 System.out.println("Project name changed from '" + originalName + "' to '" + newName + "'. Updating map reference.");
                 allProjectsMap.remove(originalName); // Remove entry with the old key
                 allProjectsMap.put(newName, projectToEdit); // Add entry with the new key
            System.out.println("Changes will be saved on logout.");
        } else {
            System.err.println("Failed to update project (check console for errors like date clash or project not managed).");
            }
        }
    }

    /**
     * Handles deleting a specific project.
     * @param projectToDelete The actual Project object from the main map to delete.
     */
    private void handleDeleteProject(Project projectToDelete) {
        System.out.print("Are you sure you want to delete project '" + projectToDelete.getName() + "'? (yes/no): ");
        if (readYesNoInput()) {
            // Call Manager logic (might also update manager's internal list if applicable - but we removed that list)
            // Manager.delProject should primarily check if the manager *can* delete it.
            boolean success = manager.delProject(projectToDelete);

            if (success) {
                // Also remove from the main map tracked by the application
                allProjectsMap.remove(projectToDelete.getName());
                System.out.println("Project deleted successfully IN MEMORY.");
                 System.out.println("Changes will be saved on logout.");
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
        List<Project> projects = getProjectsManagedByThisManager(); // Filter projects first

        Project selectedProjectStub = selectProject(projects, "Select a project to toggle visibility:");

        if (selectedProjectStub == null) return;

        // Get the actual object from the main map
        Project projectToToggle = allProjectsMap.get(selectedProjectStub.getName());

        if (projectToToggle == null) {
            System.err.println("Error: Selected project ('" + selectedProjectStub.getName() + "') not found in main application data.");
            return;
        }

        boolean currentVisibility = projectToToggle.getVisibility(); // Assuming getter exists
        System.out.println("Project '" + projectToToggle.getName() + "' is currently " + (currentVisibility ? "Visible" : "Hidden"));
        System.out.print("Toggle visibility? (yes/no): ");

        if (readYesNoInput()) {
             // Call manager logic - operates on the object from the map
            boolean success = manager.toggleProject(projectToToggle);

            if (success) {
                System.out.println("Project visibility toggled successfully IN MEMORY to " + (!currentVisibility ? "Visible" : "Hidden") + "!");
                 System.out.println("Changes will be saved on logout.");
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

        if (pendingOfficers == null || pendingOfficers.isEmpty()) { // Added null check
            System.out.println("No pending officer registrations found for your projects.");
            return;
        }

        Officer selectedOfficer = selectOfficer(pendingOfficers, "Select an officer to Approve/Reject:");

        if (selectedOfficer == null) return; // User cancelled

        // Get the actual Officer object from the main users map to ensure status updates are reflected globally
        // Note: selectedOfficer here might be a copy depending on DataManager implementation,
        // fetching from allUsersMap guarantees we modify the shared object.
        Officer officerFromMap = (Officer) allUsersMap.get(selectedOfficer.getNric()); // Cast needed

        if (officerFromMap == null) {
            System.err.println("Error: Selected officer not found in main application data.");
            return;
        }

        System.out.print("Approve this officer registration? (yes/no): ");
        boolean approve = readYesNoInput();

        // Call Manager logic, passing the globally tracked Officer object
        boolean success = manager.updateRegOfficer(allProjectsMap, officerFromMap, approve);

        if (success) {
            System.out.println("Officer registration status updated successfully IN MEMORY.");
            System.out.println("Changes will be saved on logout.");
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
        List<Project> projects = getProjectsManagedByThisManager(); // Filter projects manager can access

        Project selectedProjectStub = selectProject(projects, "Select a project to view its approved officers:");

        if (selectedProjectStub == null) return;

        // Get actual project from map
        Project projectFromMap = allProjectsMap.get(selectedProjectStub.getName());
        if (projectFromMap == null) {
            System.err.println("Error: Project not found."); return;
        }

        // Get the list of approved officers (Manager method might return copies or originals - defensive copy preferred)
        List<Officer> approvedOfficers = manager.getApprovedOfficers(projectFromMap);

        if (approvedOfficers == null || approvedOfficers.isEmpty()) { // Added null check
            System.out.println("No approved officers found for project '" + projectFromMap.getName() + "'.");
        } else {
            System.out.println("Approved Officers for Project '" + projectFromMap.getName() + "':");
            // It's safer to get names from the main user map in case the list contains stubs or incomplete objects
            for (Officer officerStub : approvedOfficers) {
                 if (officerStub == null || officerStub.getNric() == null) continue; // Safety check
                User officerUser = allUsersMap.get(officerStub.getNric()); // Get full user object
                if (officerUser != null) {
                    System.out.println("- " + officerUser.getName() + " (" + officerUser.getNric() + ")");
                } else {
                     // Should ideally not happen if data is consistent
                    System.out.println("- Unknown Officer (NRIC: " + officerStub.getNric() + ")");
                }
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

        if (pendingApplicants == null || pendingApplicants.isEmpty()) { // Added null check
            System.out.println("No pending applicant applications found for your projects.");
            return;
        }

        Applicant selectedApplicant = selectApplicant(pendingApplicants, "Select an applicant to Accept/Reject:");

        if (selectedApplicant == null) return;

        // Get the actual Applicant object from the main users map
        Applicant applicantFromMap = (Applicant) allUsersMap.get(selectedApplicant.getNric()); // Cast needed

        if (applicantFromMap == null) {
            System.err.println("Error: Selected applicant not found in main application data."); return;
        }

        System.out.print("Accept this applicant? (yes/no): ");
        boolean accept = readYesNoInput();

        // Call Manager logic with the object from the map
        boolean success = manager.updateApp(applicantFromMap, accept);

        if (success) {
            System.out.println("Applicant status updated successfully IN MEMORY.");
             System.out.println("Changes will be saved on logout.");
            // NO SAVE TO FILE HERE
        } else {
            // Specific error (like 'no room') should be printed by manager.updateApp
            System.err.println("Failed to update applicant status (check console for specific errors).");
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

        if (withdrawalApplicants == null || withdrawalApplicants.isEmpty()) { // Added null check
            System.out.println("No pending applicant withdrawals found for your projects.");
            return;
        }

        Applicant selectedApplicant = selectApplicant(withdrawalApplicants, "Select an applicant withdrawal request to Accept/Reject:");

        if (selectedApplicant == null) return;

        // Get the actual Applicant object from the main users map
        Applicant applicantFromMap = (Applicant) allUsersMap.get(selectedApplicant.getNric()); // Cast needed

        if (applicantFromMap == null) {
            System.err.println("Error: Selected applicant not found in main application data."); return;
        }

        System.out.print("Accept this withdrawal? (yes/no): ");
        boolean accept = readYesNoInput();

        // Call Manager logic with object from map
        boolean success = manager.updateWithdrawal(applicantFromMap, accept);

        if (success) {
            System.out.println("Applicant withdrawal processed successfully IN MEMORY.");
             System.out.println("Changes will be saved on logout.");
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
        List<Project> projects = getProjectsManagedByThisManager(); // Filter projects

        Project selectedProjectStub = selectProject(projects, "Select a project for the report:");

        if (selectedProjectStub == null) return;

        // Get actual project from map
        Project projectFromMap = allProjectsMap.get(selectedProjectStub.getName());
        if (projectFromMap == null) {
            System.err.println("Error: Project not found."); return;
        }

        System.out.println("Available Filters: all, married, unmarried, flat2room, flat3room, married_flat2room"); // Add more as needed
        System.out.print("Enter filter key: ");
        String filterKey = scanner.nextLine().trim();

        // Call Manager logic with object from map
        List<String> reportEntries = manager.generateApplicantReport(projectFromMap, filterKey); // Assumes manager method handles filtering

        if (reportEntries == null || reportEntries.isEmpty()) { // Added null check
            System.out.println("Report generated, but no successful applicants matched the filter criteria for project '" + projectFromMap.getName() + "'.");
        } else {
            System.out.println("\n--- Applicant Report for Project: " + projectFromMap.getName() + " (Filter: " + filterKey + ") ---");
            reportEntries.forEach(System.out::println);
            System.out.println("--- End of Report ---");
            // Consider option to save report to file here if desired (outside scope of memory changes)
        }
    }


    // --- Helper Methods for Input and Selection (Keep implementations mostly as provided) ---

    private int readIntInput() {
        int i = -1; // Default to an invalid value
         while (true) { // Loop until valid input is received
             try {
                 String line = scanner.nextLine().trim(); // Read whole line
                 if (line.isEmpty()) {
                      System.out.println("Input cannot be empty. Please enter a number.");
                      System.out.print("Enter choice: "); // Re-prompt if needed by context
                      continue;
                 }
                 i = Integer.parseInt(line);
                 break; // Exit loop if parsing is successful
             } catch (InputMismatchException | NumberFormatException e) { // Catch parsing error
                 // scanner.nextLine(); // Consume the invalid input - already handled by reading line
                 System.out.println("Invalid input. Please enter a valid number.");
                 System.out.print("Enter choice: "); // Re-prompt if needed by context
                 // return -1; // Return -1 immediately in original code - changing to loop
             }
        }
         return i;
    }


    private Integer readOptionalIntInput() {
        try {
            String line = scanner.nextLine().trim();
            return line.isEmpty() ? null : Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("(Invalid number, keeping existing)");
            return null; // Return null to indicate keeping existing value
        }
    }

     private boolean readBooleanInput() {
        String input = "";
         while (true) {
             input = scanner.nextLine().trim().toLowerCase();
             if (input.equals("true") || input.equals("yes") || input.equals("t") || input.equals("y")) {
                 return true;
             } else if (input.equals("false") || input.equals("no") || input.equals("f") || input.equals("n")) {
                 return false;
             } else {
                 System.out.print("Invalid input. Please enter true/false or yes/no: ");
             }
         }
    }

    private boolean readYesNoInput() {
        return readBooleanInput();
    }

    private LocalDate readDateInput(String prompt) {
        LocalDate date = null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
        while (date == null) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                date = LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Please use YYYY-MM-DD.");
            }
        }
        return date;
    }

    private LocalDate readOptionalDateInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null; // No change requested
        try {
             DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            return LocalDate.parse(input, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid format (YYYY-MM-DD), keeping existing.");
            return null; // Indicate keeping existing value
        }
    }

    private String readOptionalStringInput() {
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input; // Return null if empty, otherwise return input
    }


    // --- Selection Helpers ---

     private Project selectProject(List<Project> projects, String prompt) {
        if (projects == null || projects.isEmpty()) {
            System.out.println("No projects available to select.");
            return null;
        }
        System.out.println("\n" + prompt);
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            // Basic display - enhance if needed
            System.out.println((i + 1) + ". " + (p != null ? p.getName() : "Invalid Project Entry"));
        }
        System.out.println("0. Cancel");

        int choice;
        while (true) {
            System.out.print("Enter choice: ");
            choice = readIntInput(); // Use the robust readIntInput
            if (choice >= 0 && choice <= projects.size()) {
                break; // Valid choice
            } else {
                System.out.println("Invalid choice. Please enter a number between 0 and " + projects.size() + ".");
            }
        }


        if (choice == 0) {
            System.out.println("Selection cancelled.");
            return null;
        }
        // Check if selected project is null in the list before returning
        Project selected = projects.get(choice - 1);
         if (selected == null) {
             System.err.println("Error: Selected project entry is invalid.");
             return null;
         }
         return selected;
    }

    private Officer selectOfficer(List<Officer> officers, String prompt) {
        if (officers == null || officers.isEmpty()) {
            System.out.println("No officers available to select.");
            return null;
        }
        System.out.println("\n" + prompt);
        for (int i = 0; i < officers.size(); i++) {
             Officer o = officers.get(i);
             // Fetch full name from allUsersMap for better display
            User u = (o != null && o.getNric() != null) ? allUsersMap.get(o.getNric()) : null;
            String displayName = (u != null) ? u.getName() : (o != null ? "Officer NRIC: " + o.getNric() : "Invalid Officer Entry");
            System.out.println((i + 1) + ". " + displayName);
        }
        System.out.println("0. Cancel");

        int choice;
        while (true) {
            System.out.print("Enter choice: ");
            choice = readIntInput();
            if (choice >= 0 && choice <= officers.size()) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter a number between 0 and " + officers.size() + ".");
            }
        }

        if (choice == 0) {
            System.out.println("Selection cancelled.");
            return null;
        }
        Officer selected = officers.get(choice - 1);
        if (selected == null) {
            System.err.println("Error: Selected officer entry is invalid.");
            return null;
        }
        return selected;
    }


    private Applicant selectApplicant(List<Applicant> applicants, String prompt) {
        if (applicants == null || applicants.isEmpty()) {
            System.out.println("No applicants available to select.");
            return null;
        }
        System.out.println("\n" + prompt);
        for (int i = 0; i < applicants.size(); i++) {
            Applicant a = applicants.get(i);
            if (a == null) {
                 System.out.println((i + 1) + ". Invalid Applicant Entry");
                 continue;
            }
            // Fetch full name from allUsersMap for better display
            User u = (a.getNric() != null) ? allUsersMap.get(a.getNric()) : null;
            String displayName = (u != null) ? u.getName() : "Applicant NRIC: " + a.getNric();

            // Simpler display for now:
            System.out.println((i + 1) + ". " + displayName + " (NRIC: " + a.getNric() + ")"); // Added NRIC for clarity
        }
        System.out.println("0. Cancel");

        int choice;
        while (true) {
            System.out.print("Enter choice: ");
            choice = readIntInput();
            if (choice >= 0 && choice <= applicants.size()) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter a number between 0 and " + applicants.size() + ".");
            }
        }

        if (choice == 0) {
            System.out.println("Selection cancelled.");
            return null;
        }
        Applicant selected = applicants.get(choice - 1);
        if (selected == null) {
            System.err.println("Error: Selected applicant entry is invalid.");
            return null;
        }
        return selected;
    }
    /**
     * Displays details for ALL projects in the system, ignoring visibility
     * and creator, as per Manager requirements.
     */
    private void handleViewAllProjects() {
        System.out.println("\n--- Viewing ALL Projects ---");
        if (allProjectsMap == null || allProjectsMap.isEmpty()) {
            System.out.println("No projects found in the system.");
            return;
        }

        // Sort projects by name for consistent display (optional)
        List<Project> sortedProjects = new ArrayList<>(allProjectsMap.values());
        sortedProjects.sort(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER));

        for (Project p : sortedProjects) {
            if (p == null) continue; // Safety check

            System.out.println("\n====================================");
            System.out.println("Project: " + p.getName());
            System.out.println("====================================");
            try {
                p.viewAllDetails(true);

                System.out.println("-> Available Units: [2-Room: " + p.getAvalNo2Room() + ", 3-Room: " + p.getAvalNo3Room() + "]");

            } catch (Exception e) {
                System.err.println("Error displaying details for project: " + p.getName() + " - " + e.getMessage());
                System.out.println("(Error retrieving full details)");
            }
        }
        System.out.println("====================================");
        System.out.println("--- End of All Projects List ---");
    }

} 