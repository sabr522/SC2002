package cli;

import Actors.Applicant;
import Actors.Manager;
import Actors.Officer;
import Actors.User; // Assuming a User class exists for login info
import Project.Project;
import cli.EnquiryCLI;
// Assuming DataManager is in data package
import data.DataManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList; // Added for list handling

public class ManagerCLI {
    private Manager manager;
    private User currentUser; // To know who is logged in
    private Scanner scanner;
    private DataManager dataManager; // To fetch data lists
    private EnquiryCLI enquiryCLI;    // To handle enquiry UI

    // Constructor
    public ManagerCLI(Manager manager, User currentUser, Scanner scanner, DataManager dataManager) {
        this.manager = manager;
        this.currentUser = currentUser;
        this.scanner = scanner;
        this.dataManager = dataManager; // Initialize DataManager
        this.enquiryCLI = new EnquiryCLI(scanner); // Initialize EnquiryCLI (assuming constructor takes Scanner)
    }

    // Main menu loop for the manager
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

            switch (choice) {
                case 1:
                    handleCreateProject();
                    break;
                case 2:
                    handleViewEditDeleteProjects();
                    break;
                case 3:
                    handleToggleProjectVisibility();
                    break;
                case 4:
                    handleUpdateOfficerReg();
                    break;
                case 5:
                    handleViewApprovedOfficers();
                    break;
                case 6:
                    handleUpdateApplicant();
                    break;
                case 7:
                    handleUpdateWithdrawal();
                    break;
                case 8:
                    handleGenerateReport();
                    break;
                case 9:
                    // Delegate to the EnquiryCLI's menu or specific methods
                    enquiryCLI.showEnquiryMenuForStaff(currentUser); // Pass user context
                    break;
                case 0:
                    System.out.println("Logging out manager " + manager.getName() + "...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }

    // --- Handler Methods for Menu Options ---

    private void handleCreateProject() {
        System.out.println("\n--- Create New Project ---");
        System.out.print("Enter Project Name: ");
        String name = scanner.nextLine();
        System.out.print("Set initial visibility (true/false): ");
        boolean visibility = readBooleanInput();
        System.out.print("Enter Neighbourhood: ");
        String neighbourhood = scanner.nextLine();
        LocalDate openingDate = readDateInput("Enter Application Opening Date (YYYY-MM-DD): ");
        LocalDate closingDate = readDateInput("Enter Application Closing Date (YYYY-MM-DD): ");

        // Validate dates
        if (openingDate != null && closingDate != null && closingDate.isBefore(openingDate)) {
            System.err.println("Error: Closing date cannot be before opening date.");
            return;
        }

        System.out.print("Enter Number of 2-Room Flats: ");
        int num2Rooms = readIntInput();
        System.out.print("Enter Number of 3-Room Flats: ");
        int num3Rooms = readIntInput();

        Project newProject = manager.createProject(name, visibility, neighbourhood, openingDate, closingDate, num2Rooms, num3Rooms);

        if (newProject != null) {
            System.out.println("Project '" + newProject.getName() + "' created successfully!");
            // **Important:** Need to save the updated projects list via DataManager
            // dataManager.saveAllProjects(manager.getManagedProjects()); // Or however projects are saved
        } else {
            System.err.println("Failed to create project (check console for specific error like date clash).");
        }
    }

    private void handleViewEditDeleteProjects() {
        List<Project> projects = manager.getAllProjectsManagedByThisManager();
        Project selectedProject = selectProject(projects, "Select a project to View/Edit/Delete:");

        if (selectedProject == null) {
            return; // User cancelled or no projects
        }

        System.out.println("\n--- Project: " + selectedProject.getName() + " ---");
        System.out.println("1. View Details");
        System.out.println("2. Edit Project");
        System.out.println("3. Delete Project");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        int choice = readIntInput();

        switch (choice) {
            case 1:
                System.out.println("\n--- Project Details ---");
                System.out.println(manager.getProjectDetails(selectedProject));
                break;
            case 2:
                handleEditProject(selectedProject);
                break;
            case 3:
                handleDeleteProject(selectedProject);
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void handleEditProject(Project projectToEdit) {
        System.out.println("\n--- Editing Project: " + projectToEdit.getName() + " ---");
        System.out.println("(Leave blank or enter invalid value to keep existing)");

        System.out.print("Enter new Place Name (Current: " + projectToEdit.getPlaceName() + "): ");
        String placeName = readOptionalStringInput();

        System.out.print("Enter new Neighbourhood (Current: " + projectToEdit.getNeighbourhood() + "): ");
        String neighbourhood = readOptionalStringInput();

        LocalDate openingDate = readOptionalDateInput("Enter new Application Opening Date (YYYY-MM-DD) (Current: " + projectToEdit.getAppOpeningDate() + "): ");
        LocalDate closingDate = readOptionalDateInput("Enter new Application Closing Date (YYYY-MM-DD) (Current: " + projectToEdit.getAppClosingDate() + "): ");

        // Basic date validation if both are provided
        LocalDate finalOpening = (openingDate != null) ? openingDate : projectToEdit.getAppOpeningDate();
        LocalDate finalClosing = (closingDate != null) ? closingDate : projectToEdit.getAppClosingDate();
        if (finalOpening != null && finalClosing != null && finalClosing.isBefore(finalOpening)) {
             System.err.println("Error: Closing date cannot be before opening date. Edit cancelled.");
             return;
        }

        System.out.print("Enter new Number of 2-Room Flats (Current: " + projectToEdit.getNum2RoomUnits() + ", Enter non-number to skip): ");
        Integer num2Rooms = readOptionalIntInput(); // Reads Integer wrapper

        System.out.print("Enter new Number of 3-Room Flats (Current: " + projectToEdit.getNum3RoomUnits() + ", Enter non-number to skip): ");
        Integer num3Rooms = readOptionalIntInput(); // Reads Integer wrapper


        boolean success = manager.editProject(projectToEdit, placeName, neighbourhood, openingDate, closingDate, num2Rooms, num3Rooms);

        if (success) {
            System.out.println("Project updated successfully!");
            // **Important:** Need to save changes via DataManager
            // dataManager.saveAllProjects(...);
        } else {
            System.err.println("Failed to update project (check console for errors like date clash).");
        }
    }

     private void handleDeleteProject(Project projectToDelete) {
        System.out.print("Are you sure you want to delete project '" + projectToDelete.getName() + "'? (yes/no): ");
        String confirmation = scanner.nextLine();
        if (confirmation.equalsIgnoreCase("yes")) {
            boolean success = manager.delProject(projectToDelete);
            if (success) {
                System.out.println("Project deleted successfully!");
                // **Important:** Need to save changes via DataManager
                // dataManager.saveAllProjects(...);
            } else {
                System.err.println("Failed to delete project.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }


    private void handleToggleProjectVisibility() {
         List<Project> projects = manager.getAllProjectsManagedByThisManager();
         Project selectedProject = selectProject(projects, "Select a project to toggle visibility:");

         if (selectedProject == null) {
             return; // Cancelled or no projects
         }

         boolean currentVisibility = selectedProject.getVisibility(); // Assumes getter exists
         System.out.println("Project '" + selectedProject.getName() + "' is currently " + (currentVisibility ? "Visible" : "Hidden"));
         System.out.print("Toggle visibility? (yes/no): ");
         String confirmation = scanner.nextLine();

         if (confirmation.equalsIgnoreCase("yes")) {
             boolean success = manager.toggleProject(selectedProject);
             if (success) {
                 System.out.println("Project visibility toggled successfully to " + (!currentVisibility ? "Visible" : "Hidden") + "!");
                 // **Important:** Need to save changes via DataManager
                 // dataManager.saveAllProjects(...);
             } else {
                 System.err.println("Failed to toggle project visibility.");
             }
         } else {
             System.out.println("Toggle cancelled.");
         }
    }


    private void handleUpdateOfficerReg() {
        System.out.println("\n--- Update Officer Registration ---");
        // **Highlight:** How do we get the list of officers pending *for this manager*?
        // Option A: Iterate through all managed projects and collect pending officers (less efficient)
        // Option B: DataManager provides a dedicated method (preferred)
        List<Officer> pendingOfficers = dataManager.getAllPendingOfficersForManager(manager.getName()); // Assumed DataManager method

        if (pendingOfficers == null || pendingOfficers.isEmpty()) {
            System.out.println("No pending officer registrations found for projects managed by you.");
            return;
        }

        Officer selectedOfficer = selectOfficer(pendingOfficers, "Select an officer to Approve/Reject:");
        if (selectedOfficer == null) {
            return; // Cancelled
        }

        System.out.print("Approve this officer registration? (yes/no): ");
        boolean approve = readYesNoInput();

        boolean success = manager.updateRegOfficer(selectedOfficer, approve);

        if (success) {
            System.out.println("Officer registration status updated successfully.");
             // **Important:** Need to save changes (Projects and Officers lists) via DataManager
             // dataManager.saveAllProjects(...);
             // dataManager.saveAllOfficers(...);
        } else {
            System.err.println("Failed to update officer registration status (Officer might not be pending in your projects anymore).");
        }
    }

    private void handleViewApprovedOfficers() {
        System.out.println("\n--- View Approved Officers ---");
         List<Project> projects = manager.getAllProjectsManagedByThisManager();
         Project selectedProject = selectProject(projects, "Select a project to view its approved officers:");

         if (selectedProject == null) {
             return; // Cancelled or no projects
         }

         List<Officer> approvedOfficers = manager.getApprovedOfficers(selectedProject);

         if (approvedOfficers.isEmpty()) {
             System.out.println("No approved officers found for project '" + selectedProject.getName() + "'.");
         } else {
             System.out.println("Approved Officers for Project '" + selectedProject.getName() + "':");
             for (Officer officer : approvedOfficers) {
                 // Assuming Officer has a meaningful toString() or specific getters
                 System.out.println("- " + officer.getName()); // Example
             }
         }
    }

    private void handleUpdateApplicant() {
        System.out.println("\n--- Accept/Reject Applicant Application ---");
        // **Highlight:** Similar to officers, need a way to get relevant pending applicants.
        // Assuming DataManager can provide this list.
        List<Applicant> pendingApplicants = dataManager.getAllPendingApplicantsForManager(manager.getName()); // Assumed DataManager method

        if (pendingApplicants == null || pendingApplicants.isEmpty()) {
            System.out.println("No pending applicant applications found for projects managed by you.");
            return;
        }

        Applicant selectedApplicant = selectApplicant(pendingApplicants, "Select an applicant to Accept/Reject:");
        if (selectedApplicant == null) {
            return; // Cancelled
        }

        System.out.print("Accept this applicant? (yes/no): ");
        boolean accept = readYesNoInput();

        boolean success = manager.updateApp(selectedApplicant, accept);

         if (success) {
             System.out.println("Applicant status updated successfully.");
             // **Important:** Need to save changes (Projects and Applicants lists) via DataManager
             // dataManager.saveAllProjects(...); // If room counts changed
             // dataManager.saveAllApplicants(...);
         } else {
             System.err.println("Failed to update applicant status (check console for errors like 'no room').");
         }
    }

     private void handleUpdateWithdrawal() {
        System.out.println("\n--- Accept/Reject Applicant Withdrawal ---");
         // **Highlight:** Need a list of applicants requesting withdrawal for projects managed by this manager.
         List<Applicant> withdrawalApplicants = dataManager.getAllWithdrawalApplicantsForManager(manager.getName()); // Assumed DataManager method

         if (withdrawalApplicants == null || withdrawalApplicants.isEmpty()) {
             System.out.println("No pending applicant withdrawals found for projects managed by you.");
             return;
         }

         Applicant selectedApplicant = selectApplicant(withdrawalApplicants, "Select an applicant withdrawal request to Accept/Reject:");
         if (selectedApplicant == null) {
             return; // Cancelled
         }

         System.out.print("Accept this withdrawal? (yes/no): ");
         boolean accept = readYesNoInput();

         boolean success = manager.updateWithdrawal(selectedApplicant, accept);

          if (success) {
              System.out.println("Applicant withdrawal processed successfully.");
              // **Important:** Need to save changes (Projects and Applicants lists) via DataManager
              // dataManager.saveAllProjects(...); // If room counts changed
              // dataManager.saveAllApplicants(...);
          } else {
              System.err.println("Failed to process applicant withdrawal.");
          }
     }

    private void handleGenerateReport() {
        System.out.println("\n--- Generate Applicant Report ---");
        List<Project> projects = manager.getAllProjectsManagedByThisManager();
        Project selectedProject = selectProject(projects, "Select a project for the report:");

        if (selectedProject == null) {
             return; // Cancelled or no projects
        }

        System.out.println("Available Filters: all, married, unmarried, flat2room, flat3room, married_flat2room");
        System.out.print("Enter filter key: ");
        String filterKey = scanner.nextLine().trim();

        List<String> reportEntries = manager.generateApplicantReport(selectedProject, filterKey);

        if (reportEntries.isEmpty()) {
            System.out.println("Report generated, but no applicants matched the filter criteria for project '" + selectedProject.getName() + "'.");
        } else {
            System.out.println("\n--- Applicant Report for Project: " + selectedProject.getName() + " (Filter: " + filterKey + ") ---");
            for (String entry : reportEntries) {
                System.out.println(entry);
            }
            System.out.println("--- End of Report ---");
            // **Highlight:** Consider saving the report to a file?
            // E.g., saveReportToFile(reportEntries, selectedProject.getName() + "_" + filterKey + "_report.txt");
        }
    }


    // --- Helper Methods for Input and Selection ---

    private int readIntInput() {
        int input = -1;
        try {
            input = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Invalid input. Please enter a number.");
        } finally {
            scanner.nextLine(); // Consume newline
        }
        return input;
    }

    private Integer readOptionalIntInput() { // Returns Integer wrapper
        Integer input = null;
        try {
            String line = scanner.nextLine();
            if (!line.trim().isEmpty()) {
                input = Integer.parseInt(line);
            }
        } catch (NumberFormatException e) {
             // Input was not a valid integer, return null (meaning skip update)
             System.out.println("(Input was not a number, keeping existing value)");
        }
        return input;
    }


    private boolean readBooleanInput() {
        String input = scanner.nextLine().trim();
        return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes");
    }

     private boolean readYesNoInput() {
         String input = scanner.nextLine().trim();
         return input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y");
     }


    private LocalDate readDateInput(String prompt) {
        LocalDate date = null;
        while (date == null) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                date = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE); // Expects YYYY-MM-DD
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        return date;
    }

     private LocalDate readOptionalDateInput(String prompt) {
         System.out.print(prompt);
         String input = scanner.nextLine().trim();
         if (input.isEmpty()) {
             return null; // User skipped
         }
         try {
             return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
         } catch (DateTimeParseException e) {
              System.err.println("Invalid date format. Keeping existing value.");
             return null; // Treat invalid format as skipping
         }
     }

     private String readOptionalStringInput() {
         String input = scanner.nextLine().trim();
         return input.isEmpty() ? null : input; // Return null if empty, otherwise return input
     }

    // Generic selection method for Projects
    private Project selectProject(List<Project> projects, String prompt) {
        if (projects == null || projects.isEmpty()) {
            System.out.println("No projects available to select.");
            return null;
        }
        System.out.println("\n" + prompt);
        for (int i = 0; i < projects.size(); i++) {
            // Assuming Project has getName()
            System.out.println((i + 1) + ". " + projects.get(i).getName());
        }
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");
        int choice = readIntInput();

        if (choice > 0 && choice <= projects.size()) {
            return projects.get(choice - 1);
        } else {
            System.out.println("Selection cancelled or invalid.");
            return null;
        }
    }

    // Generic selection method for Officers
    private Officer selectOfficer(List<Officer> officers, String prompt) {
         if (officers == null || officers.isEmpty()) {
             System.out.println("No officers available to select.");
             return null;
         }
         System.out.println("\n" + prompt);
         for (int i = 0; i < officers.size(); i++) {
              // Assuming Officer has getName() or similar identifier
             System.out.println((i + 1) + ". " + officers.get(i).getName()); // Example
         }
         System.out.println("0. Cancel");
         System.out.print("Enter choice: ");
         int choice = readIntInput();

         if (choice > 0 && choice <= officers.size()) {
             return officers.get(choice - 1);
         } else {
             System.out.println("Selection cancelled or invalid.");
             return null;
         }
     }

     // Generic selection method for Applicants
     private Applicant selectApplicant(List<Applicant> applicants, String prompt) {
         if (applicants == null || applicants.isEmpty()) {
             System.out.println("No applicants available to select.");
             return null;
         }
         System.out.println("\n" + prompt);
         for (int i = 0; i < applicants.size(); i++) {
             System.out.println((i + 1) + ". " + applicants.get(i).getName() + " (Project: " + applicants.get(i).getProject().getName() + ")"); // Example
         }
         System.out.println("0. Cancel");
         System.out.print("Enter choice: ");
         int choice = readIntInput();

         if (choice > 0 && choice <= applicants.size()) {
             return applicants.get(choice - 1);
         } else {
             System.out.println("Selection cancelled or invalid.");
             return null;
         }
     }
}