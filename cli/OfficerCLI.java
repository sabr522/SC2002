package cli;

import Actors.Applicant;
import Actors.Officer;
import Actors.User; // Keep for map
import Project.Project;
import Services.EnquiryService; // Keep for enquiry
import data.DataManager; 
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.InputMismatchException; // Keep for helper
import java.util.List;

public class OfficerCLI {
    private final Officer officer; // The specific Officer object
    private final Scanner scanner; // Use scanner passed from MainApp
    private final DataManager dataManager; 
    private final EnquiryService enquiryService; // For managing enquiries
    private final Map<String, Project> allProjectsMap; // Needed for registration
    private final Map<String, User> allUsersMap; // Needed for registration checks

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

    public void showOfficerMenu() {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n--- Officer Menu (" + officer.getName() + ") ---");
            // Display options based on officer's current state
            if (officer.getHandledProject() == null) {
                System.out.println("1. Register for a Project to Handle");
            } else if (!officer.isHandlingApproved()) {
                System.out.println("1. (Registration Pending for: " + officer.getHandledProject().getName() + ")");
            } else { // Approved and handling a project
                System.out.println("1. (Handling Project: " + officer.getHandledProject().getName() + ")");
            }
            System.out.println("2. Show My Officer Profile");
            if (officer.isHandlingApproved() && officer.getHandledProject() != null) {
                 System.out.println("3. View Handled Project Details");
                 System.out.println("4. List/Book Successful Applicants for Handled Project");
                 System.out.println("5. Generate Booking Receipt for Handled Project");
                 System.out.println("6. Manage Enquiries for Handled Project");
            } else {
                 System.out.println("3. (View Project Details - N/A)");
                 System.out.println("4. (List/Book Applicants - N/A)");
                 System.out.println("5. (Generate Receipt - N/A)");
                 System.out.println("6. (Manage Enquiries - N/A)");
            }
            System.out.println("7. View/Apply for Projects (as Applicant)");

            System.out.println("0. Logout");
            System.out.print("Enter your choice: ");

            choice = readIntInput(); // Use helper

            switch (choice) {
                case 1:
                    if (officer.getHandledProject() == null) {
                         registerForProject(); // Allow registration attempt
                    } else {
                         System.out.println("You are already handling or pending approval for a project.");
                         showOfficerProfile(); // Show current status
                    }
                    break;
                case 2:
                    showOfficerProfile();
                    break;
                case 3:
                     if (officer.isHandlingApproved() && officer.getHandledProject() != null) {
                         viewProjectDetails(); // Call renamed method
                     } else { System.out.println("Option not available."); }
                    break;
                case 4:
                     if (officer.isHandlingApproved() && officer.getHandledProject() != null) {
                        handleFlatBooking();
                     } else { System.out.println("Option not available."); }
                    break;
                case 5:
                     if (officer.isHandlingApproved() && officer.getHandledProject() != null) {
                         generateReceipt(); // Call method
                     } else { System.out.println("Option not available."); }
                    break;
                case 6:
                    if (officer.isHandlingApproved() && officer.getHandledProject() != null) {
                         manageEnquiries(); // Call method
                    } else { System.out.println("Option not available."); }
                    break;
                case 7:
                     handleOfficerAsApplicantActions(); // New handler for applicant actions
                     break;
                case 0:
                    System.out.println("Logging out from Officer role...");
                    // Saving handled by MainApp
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
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
            System.out.println("- " + project.getName() + " (" + project.getNeighbourhood() + ")");
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
        // Calls the method in Officer class to view HANDLED project
        officer.viewProject();
    }

    /**
     * Handles listing successful applicants and booking a flat for one.
     * Retrieves bookable applicants from the Officer object, handles user selection,
     * and calls the Officer's booking logic.
     */
    private void handleFlatBooking() { // Renamed handler
        if (!officer.isHandlingApproved() || officer.getHandledProject() == null) {
            System.out.println("You must be an approved officer handling a project to list/book applicants.");
            return;
        }

        // 1. Get the list of applicants eligible for booking from the Officer
        List<Applicant> bookableApplicants = officer.getBookableApplicants();

        if (bookableApplicants.isEmpty()) {
            System.out.println("No applicants currently eligible for booking in project '" + officer.getHandledProject().getName() + "'.");
            return;
        }

        // 2. Display the list to the Officer
        System.out.println("\n--- Applicants Eligible for Booking ---");
        System.out.println("Project: " + officer.getHandledProject().getName());
        for (int i = 0; i < bookableApplicants.size(); i++) {
            Applicant app = bookableApplicants.get(i);
            // Use printf for better formatting
            System.out.printf("%d. %s (%s) - Applied for: %s%n",
                            (i + 1), app.getName(), app.getNric(), app.getTypeFlat());
        }
        System.out.println("0. Cancel Booking");

        // 3. Get Officer's choice
        int choice;
        Applicant applicantToBook = null;
        while (true) {
            // Use the CLI's own readIntInput method (ensure it exists and uses this.scanner)
            choice = readIntInput("Select applicant number to book flat for (0 to cancel): ");
            if (choice == 0) {
                System.out.println("Booking cancelled.");
                return;
            }
            if (choice > 0 && choice <= bookableApplicants.size()) {
                applicantToBook = bookableApplicants.get(choice - 1);
                if (applicantToBook != null) {
                    break; // Valid selection
                } else {
                    System.out.println("Invalid applicant entry in list. Please report this error."); // Should not happen
                }
            } else {
                System.out.println("Invalid selection. Please enter a number between 0 and " + bookableApplicants.size() + ".");
            }
        }

        // 4. Call the Officer's method to perform the booking action
        boolean success = officer.bookFlatForApplicant(applicantToBook);

        // Officer method already prints success/failure messages
        // No further action needed here unless you want to redisplay the menu etc.
    }

    private void generateReceipt() {
        if (!officer.isHandlingApproved() || officer.getHandledProject() == null) {
             System.out.println("You must be handling an approved project."); return;
        }
        System.out.print("Enter NRIC of booked applicant to generate receipt for: ");
        String nric = scanner.nextLine();
        // Calls the method in Officer class
        officer.generateReceipt(nric);
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
}