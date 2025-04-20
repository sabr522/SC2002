package cli;

import Actors.Applicant;
import Actors.User;
import Project.Project;
import Services.EnquiryService;
import data.DataManager;

import java.util.ArrayList;
import java.util.Comparator;
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
    
    /**
     * Constructs a CLI handler for the given applicant.
     * @param applicant Logged-in applicant
     * @param scanner Input scanner
     * @param enquiryService Enquiry handling service
     * @param dataManager Data persistence utility
     * @param allProjectsMap All loaded projects
     * @param allUsersMap All loaded users
     */
    public ApplicantCLI(Applicant applicant, Scanner scanner, EnquiryService enquiryService, DataManager dataManager,
            Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        this.applicant = applicant;
        this.scanner = scanner;
        this.dataManager = dataManager;
        this.enquiryService = enquiryService;
        this.allProjectsMap = allProjectsMap;
        this.allUsersMap = allUsersMap;
    }

    /**
     * Displays the interactive applicant menu.
     */
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
            System.out.println("7. Set/Update Preferred Neighbourhood");
            System.out.println("0. Logout");
            System.out.println("Enter choice: ");

            choice = readIntInput();

            switch (choice) {
                case 1: handleViewProjectsWithPreference(); break;
                case 2: handleApplyForProject(); break;
                case 3: handleViewApplication(); break;
                case 4: handleBookFlat(); break;
                case 5: handleWithdrawApplication(); break;
                case 6: handleEnquiryActions(); break;
                case 7: handleSetPreferredNeighbourhood(); break;
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
     * Retrieves preferred neighbourhood of applicant
     * Either updates or set a new neighbourhood preference
     */
    private void handleSetPreferredNeighbourhood() {
        System.out.println("\n--- Set Preferred Neighbourhood ---");
        String currentPref = applicant.getPreferredNeighbourhood();
        if (currentPref != null) {
            System.out.println("Your current preference: " + currentPref);
        } else {
            System.out.println("You currently have no preferred neighbourhood set.");
        }
    
        System.out.print("Enter your preferred neighbourhood (or leave blank to clear): ");
        String input = scanner.nextLine();
        applicant.setPreferredNeighbourhood(input); 
    
        String newPref = applicant.getPreferredNeighbourhood();
        if (newPref != null) {
            System.out.println("Preferred neighbourhood updated to: " + newPref);
        } else {
            System.out.println("Preferred neighbourhood cleared.");
        }
         System.out.println("Change will be saved on logout.");
    }

    /**
     * Displays ALL visible projects the applicant MIGHT be eligible for.
     * Shows full details using Project.viewAllDetails.
     * Afterwards, offers to filter by preferred neighbourhood (if set) OR
     * allows filtering by any entered neighbourhood.
     */
    private void handleViewProjectsWithPreference() {
        // 1. Get ALL potentially visible projects
        List<Project> visibleProjects = applicant.getProjectsVisibleForEnquiry(allProjectsMap);

        System.out.println("\n--- Browse All Visible Projects ---");

        if (visibleProjects.isEmpty()) {
            System.out.println("There are currently no projects visible based on your basic eligibility.");
            return;
        }

        // 2. Display ALL visible projects first
        int displayIndex = 1;
        for (Project p : visibleProjects) {
            if (p == null) continue;
            System.out.println("\n====================================");
            System.out.println("Project #" + displayIndex++); // Keep index for reference, though not selecting here
            System.out.println("====================================");
            try {
                p.viewAllDetails(false); // Show full details
                // Display available units for this applicant
                String status = applicant.getMaritalStatus();
                int age = applicant.getAge();
                List<String> unitsAvailable = new ArrayList<>();
                if (status.equals("Single") && age >= 35) { if (p.getAvalNo2Room() > 0) unitsAvailable.add("2-Room: " + p.getAvalNo2Room()); }
                else if (status.equals("Married") && age >= 21) { if (p.getAvalNo2Room() > 0) unitsAvailable.add("2-Room: " + p.getAvalNo2Room()); if (p.getAvalNo3Room() > 0) unitsAvailable.add("3-Room: " + p.getAvalNo3Room());}
                if (!unitsAvailable.isEmpty()) System.out.println("-> Available Units You Can Apply For: [" + String.join(", ", unitsAvailable) + "]");
                else System.out.println("-> Available Units You Can Apply For: [None currently matching eligibility/stock]");
            } catch (Exception e) { /* Basic error handling */ System.err.println("Error displaying details for project: " + p.getName());}
        }
        System.out.println("===================================="); // Footer

        // 3. Handle Filtering Options
        String preference = applicant.getPreferredNeighbourhood();
        boolean wantsToFilter = false;
        String filterNeighbourhood = null;

        if (preference != null) {
            System.out.println("\nYour preferred neighbourhood is: " + preference);
            System.out.print("Do you want to see ONLY projects in this preferred neighbourhood? (yes/no): ");
            if (readYesNoInput()) {
                wantsToFilter = true;
                filterNeighbourhood = preference; // Use preference for filtering
            }
            // If they answer no, they might still want to filter by a *different* neighborhood below
        }

        // If they didn't have a preference OR they said 'no' to filtering by preference, ask if they want to filter now
        if (!wantsToFilter) { // Ask only if they haven't already opted to filter by preference
            System.out.print("\nDo you want to filter the list by a specific neighbourhood? (yes/no): ");
            if (readYesNoInput()) {
                System.out.print("Enter neighbourhood name to filter by: ");
                String inputNeighbourhood = scanner.nextLine().trim();
                if (!inputNeighbourhood.isEmpty()) {
                     // Validate if this neighbourhood actually exists in the visible list? Optional.
                     boolean exists = visibleProjects.stream()
                                       .anyMatch(p -> p!= null && inputNeighbourhood.equalsIgnoreCase(p.getNeighbourhood()));
                     if(exists){
                          wantsToFilter = true;
                          filterNeighbourhood = inputNeighbourhood; // Use user input for filtering
                     } else {
                          System.out.println("No visible projects found in the neighbourhood: " + inputNeighbourhood);
                     }
                } else {
                    System.out.println("Neighbourhood name cannot be empty. Not filtering.");
                }
            }
        }

        // 4. Display Filtered Results (if requested)
        if (wantsToFilter && filterNeighbourhood != null) {
            System.out.println("\n--- Showing Projects Filtered by Neighbourhood: " + filterNeighbourhood + " ---");
            boolean foundMatches = false;
            for (Project p : visibleProjects) { // Iterate the original visible list
                if (p != null && filterNeighbourhood.equalsIgnoreCase(p.getNeighbourhood())) {
                    System.out.println("\n====================================");
                    System.out.println("Project: " + p.getName()); // No index needed for filtered view
                    System.out.println("====================================");
                     try {
                          p.viewAllDetails(false);
                          // Display available units again
                           String status = applicant.getMaritalStatus(); int age = applicant.getAge(); List<String> unitsAvailable = new ArrayList<>();
                           if (status.equals("Single") && age >= 35) { if (p.getAvalNo2Room() > 0) unitsAvailable.add("2-Room: " + p.getAvalNo2Room()); }
                           else if (status.equals("Married") && age >= 21) { if (p.getAvalNo2Room() > 0) unitsAvailable.add("2-Room: " + p.getAvalNo2Room()); if (p.getAvalNo3Room() > 0) unitsAvailable.add("3-Room: " + p.getAvalNo3Room());}
                           if (!unitsAvailable.isEmpty()) System.out.println("-> Available Units You Can Apply For: [" + String.join(", ", unitsAvailable) + "]");
                           else System.out.println("-> Available Units You Can Apply For: [None]");
                           foundMatches = true;
                     } catch (Exception e) { /* Error handling */ System.err.println("Error displaying details for project: " + p.getName());}
                }
            }
            if (!foundMatches) {
                System.out.println("No currently visible projects match the filter: " + filterNeighbourhood);
            }
            System.out.println("====================================");
            System.out.println("--- End of Filtered Project List ---");
        }

        System.out.println("\nReturning to Applicant Menu...");
    }


    // Ensure readYesNoInput helper exists and uses this.scanner
    private boolean readYesNoInput() {
        String input = "";
        while (true) {
            input = this.scanner.nextLine().trim().toLowerCase(); // Use class scanner
            if (input.equals("true") || input.equals("yes") || input.equals("t") || input.equals("y")) {
                return true;
            } else if (input.equals("false") || input.equals("no") || input.equals("f") || input.equals("n")) {
                return false;
            } else {
                System.out.print("Invalid input. Please enter true/false or yes/no: ");
            }
        }
   }
    
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

        List<Project> displayableProjects = new ArrayList<>();
        int displayIndex = 1;
        String status = applicant.getMaritalStatus();
        int age = applicant.getAge();

        for (Project p : potentiallyEligibleProjects) {
            if (p == null) continue;

            boolean canApply2Room = p.getAvalNo2Room() > 0;
            boolean canApply3Room = p.getAvalNo3Room() > 0;
            boolean eligibleForAnyFlatInThisProject = false; 

            List<String> unitsAvailableToApplicant = new ArrayList<>();

            if (status.equals("Single") && age >= 35) {
                if (canApply2Room) {
                    unitsAvailableToApplicant.add("2-Room: " + p.getAvalNo2Room());
                    eligibleForAnyFlatInThisProject = true; 
                }
               
            } else if (status.equals("Married") && age >= 21) {
                if (canApply2Room) {
                    unitsAvailableToApplicant.add("2-Room: " + p.getAvalNo2Room());
                    eligibleForAnyFlatInThisProject = true;
                }
                if (canApply3Room) {
                    unitsAvailableToApplicant.add("3-Room: " + p.getAvalNo3Room());
                    eligibleForAnyFlatInThisProject = true; 
                }
            }

            if (eligibleForAnyFlatInThisProject) {
                displayableProjects.add(p); 

                System.out.println("\n====================================");
                System.out.println("Option #" + displayIndex++);
                System.out.println("====================================");
                try {
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
        List<Project> availableProjects = handleViewAvailableProjects(); 

        if (availableProjects.isEmpty()) {
            return; 
        }
        System.out.println("0. Cancel Application"); 

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
                selectedProject = availableProjects.get(projectChoice - 1); 
                if (selectedProject != null) {
                    break; 
                } else {
                    System.out.println("Invalid project entry selected. Please try again.");
                }
            } else {
                System.out.println("Invalid project number. Please enter a number between 0 and " + availableProjects.size() + ".");
            }
        }

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

        applicant.applyProject(availableProjects, selectedProject.getName(), chosenFlatType);
    }

    /**
     * Shows the applied project details and current application status.
     */
    private void handleViewApplication() {
        System.out.println(applicant.viewAppliedProject());
    }

    /**
     * Submits a request to book a flat.
     */
    private void handleBookFlat() {
        
        if (applicant.getProject() != null) {
            applicant.bookFlat();
        } else {
            System.out.println("You have not applied to any project yet.");
        }
    }
    
    /**
     * Initiates withdrawal request.
     */
    private void handleWithdrawApplication() {
        applicant.withdrawApp();
    }

    /**
     * Handles interactions related to enquiries for the applicant.
     * Allows selecting a project by index to manage enquiries for.
     */
    private void handleEnquiryActions() {
        System.out.println("\n--- Enquiry Management ---");

        List<Project> enquiryProjectOptions = new ArrayList<>();
        Map<Integer, String> optionMap = new HashMap<>();
        int currentIndex = 1;

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


        int choice = -1;
        String targetProjectName = null; 

        if (!optionMap.isEmpty()) { 
            while (true) {
                System.out.print("Project number to manage enquiries for (or 0 for general): ");
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
            choice = 0;
        }

        System.out.println("Launching Enquiry Menu" + (targetProjectName != null ? " for project " + targetProjectName : " (General)"));
        EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, applicant.getNric(), false, false,
                                                this.scanner, this.allUsersMap, allProjectsMap); 
        enquiryCLI.showEnquiryMenu(targetProjectName); 
    }

    /**
     * Reads and validates integer input from console.
     * @return A valid integer from user.
     */
    private int readIntInput() {
        int i = -1;        
         while (true) {   
             try {
                 String line = scanner.nextLine().trim(); 
                 if (line.isEmpty()) {
                      System.out.println("Input cannot be empty. Please enter a number.");
                      System.out.print("Enter choice: "); 
                      continue;
                 }
                 i = Integer.parseInt(line);
                 break;    
             } catch (InputMismatchException | NumberFormatException e) { 
                 System.out.println("Invalid input. Please enter a valid number.");
                 System.out.print("Enter choice: "); 
                 
             }
        }
         return i;
    }
}
