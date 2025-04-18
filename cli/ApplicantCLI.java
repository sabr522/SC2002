package cli;

import Actors.Applicant;
import Actors.User;
import Project.Project;
import Services.EnquiryService;
import data.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

/**
 * Provides a Command Line Interface (CLI) for users logged in as an Applicant.
 * Handles user input, calls methods on the Applicant logic object, and displays results.
 */

public class ApplicantCLI {
    private Applicant applicant;
    private Scanner scanner;
    private DataManager dataManager;
    private final EnquiryService enquiryService;
    private final Map<String, Project> allProjectsMap;
    private final Map<String, User> allUsersMap;
    // --- Constructor for ApplicantCLI ---
    
    public ApplicantCLI(Applicant applicant, Scanner scanner, EnquiryService enquiryService, DataManager dataManager,
            Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        this.applicant = applicant;
        this.scanner = scanner;
        this.dataManager = dataManager;
        this.enquiryService = enquiryService;
        this.allProjectsMap = allProjectsMap;
        this.allUsersMap = allUsersMap;
    }

    public void showApplicantMenu() {
        int choice;
        do {
            System.out.println("\n--- Applicant Menu (" + applicant.getName() + ") ---");
            System.out.println("1. View Available Projects");
            System.out.println("2. Apply for Project");
            System.out.println("3. View My applied Project");
            System.out.println("4. Book Flat (if eligible)");
            System.out.println("5. Request to Withdraw Application");
            System.out.println("6. Submit or Handle Enquiries (View/Edit/Delete)");
            System.out.println("0. Logout");
            System.out.println("Enter choice: ");

            choice = readIntInput();

            switch (choice) {
                case 1: handleViewAvailableProjects(); break;
                case 2: handleApplyForProject(); break;
                case 3: handleViewApplication(); break;
                case 4: handleBookFlat(); break;
                case 5: handleWithdrawApplication(); break;
                case 6: handleEnquiryActions(); break;
                case 0:
                    System.out.println("Logging out applicant " + applicant.getName() + "...");
                    break;
                default:
                    System.out.print("Invalid choice. Try again.");
                    break;
            }
        } while (choice != 0);
    }

    
    // --- Handler Methods for Menu Options ---
    
    /**
     * Retrieves and displays only the projects the applicant is eligible to apply for,
     * showing detailed information and clearly indicating eligible flat types for each.
     * Uses the eligibility logic defined in Applicant.viewAvailProjects for initial filtering.
     * @return The list of available/eligible Project objects, or an empty list if none.
     */
    private List<Project> handleViewAvailableProjects() {
        List<Project> potentiallyEligibleProjects = applicant.viewAvailProjects(allProjectsMap);

        System.out.println("\n--- Available Projects You Are Eligible For ---");

        if (potentiallyEligibleProjects.isEmpty()) {
            if (!applicant.isApplied()) {
                System.out.println("There are currently no available projects matching your initial eligibility criteria.");
            }
            return potentiallyEligibleProjects;
        }

        // Prepare the *final* list to display and return, after more specific flat type check
        List<Project> displayableProjects = new ArrayList<>();
        int displayIndex = 1;
        String status = applicant.getMaritalStatus();
        int age = applicant.getAge();

        for (Project p : potentiallyEligibleProjects) {
            if (p == null) continue;

            boolean canApply2Room = p.getAvalNo2Room() > 0;
            boolean canApply3Room = p.getAvalNo3Room() > 0;
            boolean eligibleForAnyFlatInThisProject = false; // Flag to check if we should display this project

            List<String> unitsAvailableToApplicant = new ArrayList<>();

            // Determine specific eligibility for *this* project's available flats
            if (status.equals("Single") && age >= 35) {
                if (canApply2Room) {
                    unitsAvailableToApplicant.add("2-Room: " + p.getAvalNo2Room());
                    eligibleForAnyFlatInThisProject = true; // Eligible for this project
                }
                // Single cannot apply for 3-room, even if available
            } else if (status.equals("Married") && age >= 21) {
                if (canApply2Room) {
                    unitsAvailableToApplicant.add("2-Room: " + p.getAvalNo2Room());
                    eligibleForAnyFlatInThisProject = true; // Eligible for this project
                }
                if (canApply3Room) {
                    unitsAvailableToApplicant.add("3-Room: " + p.getAvalNo3Room());
                    eligibleForAnyFlatInThisProject = true; // Eligible for this project
                }
            }

            // 3. Display details ONLY if they are eligible for at least one flat type in this specific project
            if (eligibleForAnyFlatInThisProject) {
                displayableProjects.add(p); // Add to the list that will be returned for selection

                System.out.println("\n====================================");
                System.out.println("Option #" + displayIndex++);
                System.out.println("====================================");
                try {
                    // Call viewAllDetails for the eligible project
                    p.viewAllDetails(false);

                    System.out.println("-> Available Units You Can Apply For: [" + String.join(", ", unitsAvailableToApplicant) + "]");

                    if (status.equals("Single") && age >= 35 && canApply3Room) {
                        System.out.println("-> Note: 3-Room flats listed above are not available for Single applicants.");
                    }

                } catch (Exception e) {
                    System.err.println("Error displaying details for project: " + p.getName() + " - " + e.getMessage());
                    System.out.println("Project Name: " + p.getName());
                    System.out.println("(Error retrieving full details)");
                }
            }

        } 

        if (displayableProjects.isEmpty()) {
            System.out.println("Although some projects are visible, none currently have flat types you are eligible to apply for.");
        } else {
            System.out.println("====================================");
        }

        return displayableProjects; 
    }

    /**
     * Handles the process for an applicant applying for a project.
     * Displays available projects, prompts for selection and flat type,
     * then calls the applicant's application logic for final validation and state update.
     */
    private void handleApplyForProject() {
        // 1. Display available projects AND get the list for selection
        List<Project> availableProjects = handleViewAvailableProjects(); 

        // 2. Check if there are projects to apply for or if already applied
        if (availableProjects.isEmpty()) {
            return; // Exit if no projects or already applied
        }
        System.out.println("0. Cancel Application"); 

        // 3. Get user's project choice by number
        int projectChoice = -1;
        Project selectedProject = null;
        while (true) {
            System.out.print("Enter the number of the project you want to apply for: ");
            projectChoice = readIntInput(); 
            if (projectChoice == 0) {
                System.out.println("Application cancelled.");
                return;
            }
            if (projectChoice > 0 && projectChoice <= availableProjects.size()) {
                selectedProject = availableProjects.get(projectChoice - 1); // Adjust index
                if (selectedProject != null) {
                    break; // Valid project selected
                } else {
                    System.out.println("Invalid project entry selected. Please try again.");
                }
            } else {
                System.out.println("Invalid project number. Please enter a number between 0 and " + availableProjects.size() + ".");
            }
        }

        // 4. Get user's desired flat type (Let applyProject validate eligibility/availability)
        String chosenFlatType = null;
        while(chosenFlatType == null) {
            System.out.println("\nSelected Project: " + selectedProject.getName());
            System.out.print("Enter desired flat type (2-Room or 3-Room, or 0 to cancel): ");
            String inputType = scanner.nextLine().trim();

            if ("0".equals(inputType)) {
                System.out.println("Application cancelled.");
                return;
            } else if ("2-Room".equalsIgnoreCase(inputType) || "2".equals(inputType)) {
                chosenFlatType = "2-Room";
            } else if ("3-Room".equalsIgnoreCase(inputType) || "3".equals(inputType)) {
                chosenFlatType = "3-Room";
            } else {
                System.out.println("Invalid input. Please enter '2-Room', '3-Room', '2', '3', or '0'.");
            }
        }

        // 5. Final elgiibility check
        applicant.applyProject(availableProjects, selectedProject.getName(), chosenFlatType);
    }

    private void handleViewApplication() {
        System.out.println(applicant.viewAppliedProject());
    }


    private void handleBookFlat() {
        
        if (applicant.getProject() != null) {
            applicant.bookFlat();
        } else {
            System.out.println("You have not applied to any project yet.");
        }
    }
    private void handleWithdrawApplication() {
        applicant.withdrawApp();
    }

    /**
     * Handles interactions related to enquiries for the applicant.
     * Allows selecting a project by index to manage enquiries for.
     */
    private void handleEnquiryActions() {
        System.out.println("\n--- Enquiry Management ---");

        // 1. Consolidate list of projects applicant might enquire about
        List<Project> enquiryProjectOptions = new ArrayList<>();
        Map<Integer, String> optionMap = new HashMap<>();
        int currentIndex = 1;

        // Add available projects
        List<Project> available = applicant.getProjectsVisibleForEnquiry(allProjectsMap);
        if (!available.isEmpty()) {
            System.out.println("Available Projects:");
            for (Project p : available) {
                if (p != null) {
                    System.out.printf("%d. %s (%s)%n", currentIndex, p.getName(), p.getNeighbourhood());
                    optionMap.put(currentIndex, p.getName());
                    enquiryProjectOptions.add(p); 
                    currentIndex++;
                }
            }
        }

        // Add applied project (if different and exists)
        Project appliedProject = applicant.getProject();
        if (appliedProject != null && !enquiryProjectOptions.contains(appliedProject)) {
            System.out.println("\nProject You Applied For:");
            System.out.printf("%d. %s (%s)%n", currentIndex, appliedProject.getName(), appliedProject.getNeighbourhood());
            optionMap.put(currentIndex, appliedProject.getName());
            currentIndex++;
        }

        if (optionMap.isEmpty()) {
            System.out.println("No projects available or applied for to manage enquiries.");
            System.out.println("You can still view/manage your previously submitted enquiries.");
        }

        System.out.println("\n0. Manage/View My Enquiries (General)");
        if(optionMap.isEmpty()){
            System.out.println("   (Only option available)");
        }


        // 2. Get user choice by index
        int choice = -1;
        String targetProjectName = null; // Will be null for general enquiries

        if (!optionMap.isEmpty()) { // Only ask for prject selection if there are options
            while (true) {
                System.out.print("Project number to manage enquiries for (or 0 for general): ");
                choice = readIntInput();
                if (choice == 0) {
                    break; // User chose general
                }
                if (optionMap.containsKey(choice)) {
                    targetProjectName = optionMap.get(choice);
                    System.out.println("Selected project: " + targetProjectName);
                    break; // Valid project selected
                } else {
                    System.out.println("Invalid selection. Please enter a number from the list or 0.");
                }
            }
        } else {
            // Only option is 0 (General)
            choice = 0;
        }


        // 3. Instantiate EnquiryCLI and show menu
        System.out.println("Launching Enquiry Menu" + (targetProjectName != null ? " for project " + targetProjectName : " (General)"));
        EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, applicant.getNric(), false, false,
                                                this.scanner, this.allUsersMap, allProjectsMap); // Pass scanner and map
        enquiryCLI.showEnquiryMenu(targetProjectName); // Pass project name context (can be null)
    }

    private int readIntInput() {
        int i = -1;        // Default to an invalid value
         while (true) {    // Loop until valid input is received
             try {
                 String line = scanner.nextLine().trim(); // Read whole line
                 if (line.isEmpty()) {
                      System.out.println("Input cannot be empty. Please enter a number.");
                      System.out.print("Enter choice: "); // Re-prompt if needed by context
                      continue;
                 }
                 i = Integer.parseInt(line);
                 break;    // Exit loop if parsing is successful
             } catch (InputMismatchException | NumberFormatException e) { 
                 // scanner.nextLine(); // Consume the invalid input - already handled by reading line
                 System.out.println("Invalid input. Please enter a valid number.");
                 System.out.print("Enter choice: "); // Re-prompt if needed 
                 
             }
        }
         return i;
    }
}
