package cli;

import Actors.Applicant;
import Actors.Officer;
import Actors.User; 
import Project.Project;
import Services.EnquiryService;
import data.DataManager; 
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CLI for the Officer role.
 * Provides functions for officers to register for projects, book flats, and manage enquiries.
 */
public class OfficerCLI {
    private final Officer officer; // The specific Officer object
    private final Scanner scanner; // Use scanner passed from MainApp
    private final DataManager dataManager; 
    private final EnquiryService enquiryService; // For managing enquiries
    private final Map<String, Project> allProjectsMap; // Needed for registration
    private final Map<String, User> allUsersMap; // Needed for registration checks

    /**
     * Constructs the OfficerCLI session.
     * @param officer The logged-in officer
     * @param scanner Input scanner
     * @param dataManager Data persistence utility
     * @param enquiryService Service for managing enquiries
     * @param allProjectsMap All loaded projects
     * @param allUsersMap All loaded users
     */
    public OfficerCLI(Officer officer, Scanner scanner, DataManager dataManager,
                      EnquiryService enquiryService,
                      Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        this.officer = officer;
        this.scanner = scanner;
        this.dataManager = dataManager;
        this.enquiryService = enquiryService;
        this.allProjectsMap = allProjectsMap;
        this.allUsersMap = allUsersMap;
    }

     /**
     * Shows the interactive menu for Officer role.
     */
    public void showOfficerMenu() {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n--- Officer Menu (" + officer.getName() + ") ---");
            // Show assignments first
            Map<Project, String> assignments = officer.getProjectAssignments();
            if (assignments.isEmpty()) {
                System.out.println("Status: Not assigned to handle any project.");
                System.out.println("1. Register for a Project to Handle");
            } else {
                System.out.println("Your Project Assignments:");
                assignments.forEach((proj, status) ->
                    System.out.println(" - Project: " + proj.getName() + " | Status: " + status)
                );
                 System.out.println("1. Register for Another Project (If eligible)"); // Keep register option
            }
    
            System.out.println("2. Show My Officer Profile");
            System.out.println("3. View Project Details (Requires Selection)");
            System.out.println("4. List/Book Successful Applicants (Requires Selection)");
            System.out.println("5. Generate Booking Receipt (Requires Selection)");
            System.out.println("6. Manage Enquiries (Requires Selection)");
            System.out.println("7. View/Apply for Projects (as Applicant)");
            System.out.println("0. Logout");
            System.out.print("Enter your choice: ");
            choice = readIntInput();
    
            switch (choice) {
                case 1: registerForProject(); break; 
                case 2: showOfficerProfile(); break; 
                case 3: viewProjectDetails(); break; 
                case 4: handleFlatBooking(); break; 
                case 5: generateReceipt(); break;   
                case 6: manageEnquiries(); break;   
                case 7: handleOfficerAsApplicantActions(); break;
                case 0: System.out.println("Logging out..."); break;
                default: System.out.println("Invalid choice..."); break;
            }
        }
    }

    // --- Handler Methods ---

    private void registerForProject() {
        System.out.println("\n--- Register for a Project to Handle ---");
        System.out.println("Available Projects (You cannot register for projects you applied to as Applicant):");
        // Filter out projects the officer might have applied to as Applicant
        Project applicantProject = officer.getProject(); // Inherited method
        int count = 0;

        for (Project project : allProjectsMap.values()) {
             // Don't list if they applied as applicant, or if it's the one they are already pending/handling
            if (project.equals(applicantProject) || project.equals(officer.getHandledProject())) {
                 continue;
            }
            java.time.format.DateTimeFormatter displayFormat = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            String newProjectDates = "(Dates N/A)";
             if (project.getAppOpeningDate() != null && project.getAppClosingDate() != null) {
                  newProjectDates = "(" + project.getAppOpeningDate().format(displayFormat) + " to " + project.getAppClosingDate().format(displayFormat) + ")";
             }
            System.out.println("- " + project.getName() + " (" + project.getNeighbourhood() + ") Period: " + newProjectDates);
            count++;
        }
        if (count == 0) {
             System.out.println("No projects currently available for registration.");
             return;
        }

        System.out.print("Enter Project Name to register for: ");
        String projectName = scanner.nextLine();
        // Call the officer's registration logic
        officer.registerProject(projectName, allProjectsMap, allUsersMap);
    }

    private void showOfficerProfile() {
        // Calls the method in Officer class
        officer.showProfile();
    }

    private void viewProjectDetails() {
        Project project = selectHandledProject("View Details");
        if (project != null) {
            officer.viewProject(project); // Call officer method with selected project
        }
    }

    /**
     * Handles listing successful applicants and booking a flat for one.
     * Retrieves bookable applicants from the Officer object, handles user selection,
     * and calls the Officer's booking logic.
     */
    private void handleFlatBooking() { 
         // 1. Check if Officer is approved and handling exactly one project
         Project handledProject = null;
         List<Project> approvedProjects = officer.getApprovedHandledProjects();
         
         if (approvedProjects.isEmpty()) {
            System.out.println("You must be approved and handling a project to list/book applicants.");
            // Check if they have pending projects
            if (!officer.getPendingHandledProjects().isEmpty()) {
                 System.out.println("Your project assignment(s) are still pending approval.");
            }
            return;
        } else {
            // Exactly one approved project
            handledProject = approvedProjects.get(0);
        }

        // Ensure we have a valid handled project context now
        if (handledProject == null) {
             System.out.println("Could not determine the project context. Cannot proceed.");
             return;
        }

        System.out.println("Operating on project: " + handledProject.getName());

        // 2. Get the list of applicants eligible for booking *from the specific handled project*
        List<Applicant> successfulList = handledProject.getSuccessfulApplicants();
        List<Applicant> bookableApplicants = new ArrayList<>();
        if (successfulList != null) {
             for (Applicant app : successfulList) {
                  if (app != null && "Successful".equals(app.getAppStatus())) {
                       bookableApplicants.add(app);
                  }
             }
        }

        if (bookableApplicants.isEmpty()) {
            System.out.println("No applicants currently eligible for booking in project '" + handledProject.getName() + "'.");
            return;
        }

        // 3. Display the list to the Officer
        System.out.println("\n--- Applicants Eligible for Booking ---");
        System.out.println("Project: " + handledProject.getName());
        for (int i = 0; i < bookableApplicants.size(); i++) {
            Applicant app = bookableApplicants.get(i);
            System.out.printf("%d. %s (%s) - Applied for: %s%n",
                              (i + 1), app.getName(), app.getNric(), app.getTypeFlat());
        }
        System.out.println("0. Cancel Booking");

        // 4. Get Officer's choice
        int choice;
        Applicant applicantToBook = null;
        while (true) {
            choice = readIntInput("Select applicant number to book flat for (0 to cancel): "); // Use CLI's helper
            if (choice == 0) { System.out.println("Booking cancelled."); return; }
            if (choice > 0 && choice <= bookableApplicants.size()) {
                applicantToBook = bookableApplicants.get(choice - 1);
                if (applicantToBook != null) break;
                else System.out.println("Invalid list entry selected."); // Error
            } else {
                System.out.println("Invalid selection. Please enter a number between 0 and " + bookableApplicants.size() + ".");
            }
        }
        boolean success = officer.bookFlatForApplicant(applicantToBook, handledProject);

    }

    /**
     * Handles generating a booking receipt. Lists booked applicants for the
     * handled project and prompts the officer to select one by index.
     */
    private void generateReceipt() {
        // 1. Check if officer is handling a project
        Project handledProject = selectHandledProject("Generate Receipt");

        // 2. Get list of booked applicants from the project
        List<Applicant> bookedList = handledProject.getBookedApplicants(); 
        if (bookedList == null || bookedList.isEmpty()) {
            System.out.println("No applicants have booked a flat in project '" + handledProject.getName() + "' yet.");
            return;
        }

        // 3. Display booked applicants with indices
        System.out.println("\n--- Booked Applicants in Project: " + handledProject.getName() + " ---");
        Map<Integer, String> optionMap = new HashMap<>(); // Map display index -> NRIC
        int displayIndex = 1;
        for (Applicant app : bookedList) {
            if (app != null) {
                System.out.printf("%d. %s (%s) - Booked: %s%n",
                                displayIndex, app.getName(), app.getNric(), app.getTypeFlat());
                optionMap.put(displayIndex, app.getNric());
                displayIndex++;
            }
        }
        System.out.println("0. Cancel Receipt Generation");

        // 4. Get officer's choice
        int choice = -1;
        String selectedNric = null;
        while(true) {
            choice = readIntInput("Enter number of applicant to generate receipt for: ");
            if (choice == 0) {
                System.out.println("Receipt generation cancelled.");
                return;
            }
            if (optionMap.containsKey(choice)) {
                selectedNric = optionMap.get(choice);
                break;
            } else {
                System.out.println("Invalid selection. Please enter a number from the list or 0.");
            }
        }

        // 5. Call the officer's logic method with the selected NRIC
        if (selectedNric != null) {
            officer.generateReceipt(handledProject, selectedNric); // Call the existing logic method
        }
    }

    private void manageEnquiries() {
         Project handled = officer.getHandledProject();
         if (handled == null || !officer.isHandlingApproved()) {
            System.out.println("You must be handling an approved project to manage enquiries.");
            return;
         }
         String projectName = handled.getName();
         System.out.println("\n--- Managing Enquiries for Project: " + projectName + " ---");
         EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, officer.getNric(), true, false, scanner, allUsersMap, allProjectsMap); // Officer is staff
         enquiryCLI.showEnquiryMenu(projectName);
    }

    /** Handles actions where the Officer acts as an Applicant */
    private void handleOfficerAsApplicantActions() {
         System.out.println("\n--- Acting as Applicant ---");
         // Present a limited Applicant menu for the Officer
         while(true) {
              System.out.println("\n-- Applicant Actions Menu (Officer: " + officer.getName() + ") --");
              System.out.println("1. View Available Projects (To Apply)");
              System.out.println("2. Apply for Project");
              System.out.println("3. View My Applied Project (As Applicant)");
              System.out.println("4. Request Withdrawal (As Applicant)");
              System.out.println("5. Manage Enquiries (As Applicant)");
              System.out.println("0. Back to Officer Menu");
              System.out.print("Select applicant action: ");
              int choice = readIntInput();

              switch(choice) {
                case 1:
                    // Display available projects by calling the logic method on officer
                    List<Project> available = officer.viewAvailProjects(allProjectsMap);
                    displayAvailableProjectsForOfficer(available);
                    break;
                case 2:
                    // Trigger the application process, calling the overridden applyProject
                    handleOfficerApplyForProject(); 
                    break;
                case 3:
                     // Call the logic method directly on officer
                     System.out.println("\n--- Your Application Status (as Applicant) ---");
                     System.out.println(officer.viewAppliedProject()); // Calls Applicant's method
                    break;
                case 4:
                     // Call the logic method directly on officer
                     officer.withdrawApp(); 
                    break;
                case 5:
                    // Manage enquiries submitted BY THIS NRIC (acting as applicant)
                    System.out.println("\n--- Managing Your Submitted Enquiries ---");
                    // Officer is NOT staff when submitting their own enquiry
                    EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, officer.getNric(), false, false, scanner, allUsersMap, allProjectsMap);
                    enquiryCLI.showEnquiryMenu(null); // Show general enquiry menu for self
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
           }
      }
 }

    // Helper to read integer input 
    private int readIntInput() {
        int input = -1;
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) { System.out.println("Input cannot be empty."); System.out.print("Enter choice: "); continue; }
                input = Integer.parseInt(line);
                break;
            } catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a whole number."); System.out.print("Enter choice: "); }
        }
        return input;
    }

    private int readIntInput(String prompt) {
    int input = -1;
    while (true) {
        try {
            System.out.print(prompt);
            String line = this.scanner.nextLine().trim(); 
            if (line.isEmpty()) { System.out.println("Input cannot be empty."); continue; }
            input = Integer.parseInt(line);
            break;
        } catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a whole number.");}
         catch (NoSuchElementException | IllegalStateException e) { 
              System.err.println("Input stream error. Exiting function."); return -1;
         }
    }
    return input;
}

     /**
     * Helper method specifically for displaying the list of projects an officer
     * might be able to apply for (acting as an applicant).
     * @param availableProjects The list of projects filtered by basic eligibility.
     */
    private void displayAvailableProjectsForOfficer(List<Project> availableProjects) {
         System.out.println("\n--- Available Projects You Could Apply For (As Applicant) ---");
         if (availableProjects.isEmpty()) {
             if (!officer.isApplied()) { // Check inherited applied status
                 System.out.println("There are currently no projects you are eligible to apply for.");
             } else {
                  System.out.println("You have already applied for project '" + officer.getProject().getName() + "' as an Applicant.");
             }
             return;
         }
         int displayIndex = 1;
         for (Project p : availableProjects) {
              if (p == null) continue;
              System.out.printf("%d. Project: %s (%s) - Flats Available: [%s%s]%n",
                   displayIndex++,
                   p.getName(),
                   p.getNeighbourhood(),
                   (p.getAvalNo2Room() > 0 ? "2-Room: " + p.getAvalNo2Room() + " " : ""),
                   (p.getAvalNo3Room() > 0 ? "3-Room: " + p.getAvalNo3Room() : "")
              );
         }
          System.out.println("----------------------------------------------");
    }


    /**
     * Helper method for handling the officer applying for a project as an applicant.
     * Gets project/flat choice and calls the overridden applyProject method.
     */
    private void handleOfficerApplyForProject() {
        // 1. Get available projects first
        List<Project> availableProjects = officer.viewAvailProjects(allProjectsMap);

         // 2. Display them using the helper
        displayAvailableProjectsForOfficer(availableProjects);

        if (availableProjects.isEmpty()) {
            return;
        }
        System.out.println("0. Cancel Application");

        // 3. Get project choice
        int projectChoice = -1;
        Project selectedProject = null;
        while (true) {
            System.out.print("Enter the number of the project you want to apply for: ");
            projectChoice = readIntInput();
            if (projectChoice == 0) { System.out.println("Application cancelled."); return; }
            if (projectChoice > 0 && projectChoice <= availableProjects.size()) {
                selectedProject = availableProjects.get(projectChoice - 1);
                if (selectedProject != null) break;
                else System.out.println("Invalid project entry selected.");
            } else System.out.println("Invalid project number.");
        }

        // 4. Get flat type choice
        String chosenFlatType = null;
        while (chosenFlatType == null) {
            System.out.println("\nSelected Project: " + selectedProject.getName());
            System.out.print("Enter desired flat type (2-Room or 3-Room, or 0 to cancel): ");
            String inputType = scanner.nextLine().trim();
            if ("0".equals(inputType)) { System.out.println("Application cancelled."); return; }
            else if ("2-Room".equalsIgnoreCase(inputType) || "2".equals(inputType)) chosenFlatType = "2-Room";
            else if ("3-Room".equalsIgnoreCase(inputType) || "3".equals(inputType)) chosenFlatType = "3-Room";
            else System.out.println("Invalid input.");
        }

        // 5. Call the *Officer's overridden* applyProject method
        officer.applyProject(availableProjects, selectedProject.getName(), chosenFlatType);
    }

    /** Prompts officer to select one of their handled/pending projects */
    private Project selectHandledProject(String promptAction) {
    Map<Project, String> assignments = officer.getProjectAssignments();
    List<Project> options = new ArrayList<>(assignments.keySet()); // Get list of projects

    if (options.isEmpty()) {
        System.out.println("You are not assigned to any projects to perform this action.");
        return null;/** Prompts officer to select one of their handled/pending projects */
    }

    if (options.size() == 1) {
         Project singleProject = options.get(0);
         // Check if approved if action requires it
         if(!"Approved".equalsIgnoreCase(assignments.get(singleProject)) &&
            (promptAction.contains("Book") || promptAction.contains("Receipt") || promptAction.contains("Manage Enquiries"))){
              System.out.println("Your assignment for project '" + singleProject.getName() + "' is still pending approval.");
              return null;
         }
         System.out.println("Acting on your only assigned project: " + singleProject.getName());
         return singleProject; // Auto-select if only one
    }

    System.out.println("\nSelect Project to " + promptAction + ":");
    Map<Integer, Project> choiceMap = new HashMap<>();
    int index = 1;
    for (Project p : options) {
         // Only list projects eligible for the action (e.g., must be approved for booking)
         boolean eligibleForAction = true;
         String status = assignments.get(p);
         if(!"Approved".equalsIgnoreCase(status) &&
             (promptAction.contains("Book") || promptAction.contains("Receipt") || promptAction.contains("Manage Enquiries")))
         {
              eligibleForAction = false; // Cannot perform these actions if pending
         }

         if(eligibleForAction) {
              System.out.printf("%d. %s (Status: %s)%n", index, p.getName(), status);
              choiceMap.put(index, p);
              index++;
         } else {
              System.out.printf("- %s (Status: %s - Action N/A)%n", p.getName(), status);
         }
    }
     if(choiceMap.isEmpty()){
          System.out.println("No projects currently eligible for this action.");
          return null;
     }
    System.out.println("0. Cancel");

    while (true) {
        int choice = readIntInput("Enter project number: ");
        if (choice == 0) return null;
        if (choiceMap.containsKey(choice)) {
            return choiceMap.get(choice);
        }
        System.out.println("Invalid selection.");
    }
    }
}
