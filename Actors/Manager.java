package Actors;

import Project.Project; 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Added import for Map needed in updated updateRegOfficer
import java.util.Objects;
import java.util.stream.Collectors; // For report generation filtering

/**
 * Represents a Manager user, inheriting common attributes from User.
 * Manages projects (creation, editing, deletion, visibility) created by this manager,
 * and handles related processes like officer registration and applicant processing for those projects.
 * NOTE: This class no longer maintains its own internal list of managed projects.
 * Operations rely on checking the creatorName of Project objects passed to its methods
 * or operating on data filtered by the caller (e.g., ManagerCLI).
 */
public class Manager extends User { // Extend the abstract User class

    // No longer contains: private List<Project> managedProjects;

    /**
     * Constructor for Manager. Initializes inherited User fields.
     * Role is automatically set to "Manager".
     *
     * @param name          Manager's name (for User).
     * @param nric          Manager's NRIC (for User).
     * @param age           Manager's age (for User).
     * @param maritalStatus Manager's marital status (for User).
     * @param password      Manager's password (for User).
     */
    public Manager(String name, String nric, int age, String maritalStatus, String password) {
        // Call the User constructor FIRST to initialize common fields
        super(name, nric, age, maritalStatus, password, "Manager"); // Role is fixed as "Manager"

        // No longer initializes: this.managedProjects
    }

    // --- Getters ---
    // Inherits getName(), getNric(), getAge(), getMaritalStatus(), getPassword(), getRole() from User.

    // --- Project Management Methods ---

    /**
     * Creates a new project with this manager set as the creator.
     * necessary date clash checks against other projects before invoking this method.
     *
     * @param name             Project's name.
     * @param visibility       Project's initial visibility status.
     * @param neighbourhood    Project's neighbourhood.
     * @param appOpeningDate   Project's application opening date.
     * @param appClosingDate   Project's application closing date.
     * @param num2Rooms        Number of 2-room flats.
     * @param num3Rooms        Number of 3-room flats.
     * @param projectsToCheck  Projects Manager holding
     * @return The created Project object, or null if creation failed (e.g., invalid parameters, though clash check is external).
     */
    public Project createProject(String name, Boolean visibility, String neighbourhood,
                                 LocalDate appOpeningDate, LocalDate appClosingDate, int num2Rooms, int num3Rooms, List<Project> projectsToCheck) {
        // Perform clash check internally using the provided list
        if (isAnyProjectClashing(projectsToCheck, appOpeningDate, appClosingDate, null)) { // Pass null for skipProjectName
            System.err.println("Error: Application period clashes with an existing project managed by " + this.getName() + ".");
            return null; // Return null to indicate failure due to clash
        }

        // Pass inherited name (this.getName) as creatorName
        try {
            Project newProject = new Project(name, visibility, this.getName(), neighbourhood,
                                            appOpeningDate, appClosingDate, num2Rooms, num3Rooms);
            return newProject;
        } catch (Exception e) {
            System.err.println("Error during project object creation: " + e.getMessage());
            return null; // Indicate failure
        }
    }

    /**
     * Edits the details of an existing project.
     * if dates are being modified, before invoking this method.
     * Checks if the project being edited was actually created by this manager.
     * Allows partial updates; null parameters mean the corresponding field is not updated by this call,
     * the method will use the existing value from projectToEdit.
     *
     * @param projectToEdit    The specific Project object to edit (must exist and be managed by this manager).
     * @param placeName        New place name (or null to keep existing).
     * @param neighbourhood    New neighbourhood (or null to keep existing).
     * @param appOpeningDate   New application opening date (or null to keep existing).
     * @param appClosingDate   New application closing date (or null to keep existing).
     * @param num2Rooms        New number of 2-room flats (or null to keep existing). Use Integer wrapper type.
     * @param num3Rooms        New number of 3-room flats (or null to keep existing). Use Integer wrapper type.\
     * @param projectsToCheck  Projects Manager holding
     * @return true if successful, false otherwise (e.g., project not found, not managed by this manager).
     */
    public boolean editProject(Project projectToEdit, String placeName, String neighbourhood,
                               LocalDate appOpeningDate, LocalDate appClosingDate, Integer num2Rooms, Integer num3Rooms, List<Project> projectsToCheck) {

        // Check ownership using creatorName
        if (projectToEdit != null && this.getName().equals(projectToEdit.getCreatorName())) {

            // Determine final values, using existing if input is null
            String updatedPlaceName = (placeName != null) ? placeName : projectToEdit.getName();
            String updatedNeighbourhood = (neighbourhood != null) ? neighbourhood : projectToEdit.getNeighbourhood();
            LocalDate updatedOpening = (appOpeningDate != null) ? appOpeningDate : projectToEdit.getAppOpeningDate();
            LocalDate updatedClosing = (appClosingDate != null) ? appClosingDate : projectToEdit.getAppClosingDate();
            int updatedNum2Rooms = (num2Rooms != null) ? num2Rooms : projectToEdit.getNo2Room(); 
            int updatedNum3Rooms = (num3Rooms != null) ? num3Rooms : projectToEdit.getNo3Room(); 


            // Perform internal clash check *only if dates were actually changed*
            if (appOpeningDate != null || appClosingDate != null) {
                if (isAnyProjectClashing(projectsToCheck, updatedOpening, updatedClosing, projectToEdit.getName())) { // Pass project name to skip
                    System.err.println("Error: New application period clashes with another existing project managed by you. Edit cancelled.");
                    return false; // Edit failed due to clash
                }
            }

            // Call the setter method in Project 
             try {
                 projectToEdit.setName(this.name, updatedPlaceName);
                 projectToEdit.setNeighbourhood(this.name, updatedNeighbourhood);
                 projectToEdit.setAppOpeningDate(this.name, updatedOpening);
                 projectToEdit.setAppClosingDate(this.name, updatedClosing);
                 projectToEdit.setNo2Room(this.name, updatedNum2Rooms);
                 projectToEdit.setAvalNo2Room(updatedNum2Rooms); 
                 projectToEdit.setNo3Room(this.name, updatedNum3Rooms);
                 projectToEdit.setAvalNo3Room(updatedNum3Rooms); 

                 return true; // Edit successful
             } catch (Exception e) {
                  System.err.println("Error setting project details: " + e.getMessage());
                  return false; // Edit failed
             }

        } else {
            if (projectToEdit == null) {
                 System.err.println("Error: Cannot edit a null project.");
            } else {
                 System.err.println("Error: Project '" + projectToEdit.getName() + "' not managed by " + this.getName() + ".");
            }
            return false; // Project not found or not managed by this manager
        }
    }

    /**
     * Private helper method to check if the given date range clashes with any existing project
     * within the provided list, optionally skipping a specific project by name.
     *
     * @param projectsToCheck List of projects to check against (typically those managed by this manager).
     * @param appOpeningDate The opening date to check.
     * @param appClosingDate The closing date to check.
     * @param skipProjectName The name of the project to ignore during the check (can be null, e.g., for new projects).
     * @return true if a clash is found, false otherwise.
     */
    private boolean isAnyProjectClashing(List<Project> projectsToCheck, LocalDate appOpeningDate, LocalDate appClosingDate, String skipProjectName) {
        if (appOpeningDate == null || appClosingDate == null || projectsToCheck == null) {
            return false; // Cannot clash if dates are incomplete or list is null
        }
        for (Project existingProject : projectsToCheck) {
            if (existingProject == null) continue;

            // Skip the project if its name matches skipProjectName
            if (skipProjectName != null && skipProjectName.equals(existingProject.getName())) {
                continue;
            }

            if (existingProject.isClashing(appOpeningDate, appClosingDate)) {
                return true; // Found a clash
            }
        }
        return false; // No clashes found
    }
    /**
     * Deletes a project if it was created by this manager.
     * IMPORTANT: This method only verifies ownership. The actual removal from the
     * main application data map must be handled by the caller (e.g., ManagerCLI).
     *
     * @param projectToDelete The Project object to potentially delete.
     * @return true if the manager owns the project (deletion can proceed), false otherwise.
     */
    public boolean delProject(Project projectToDelete) {
        // Check ownership using creatorName
        if (projectToDelete != null && this.getName().equals(projectToDelete.getCreatorName())) {
            return true; // Indicates manager owns it, caller can proceed with removal
        } else {
             if (projectToDelete == null) {
                  System.err.println("Error: Cannot delete null project.");
             } else {
                  System.err.println("Error: Cannot delete project not created by " + this.getName() + ".");
             }
            return false; // Cannot delete
        }
    }

    /**
     * Toggles the visibility of a project if it was created by this manager.
     *
     * @param projectToToggle The Project object to toggle.
     * @return true if successful, false otherwise (project null or not managed).
     */
    public boolean toggleProject(Project projectToToggle) {
        // Check ownership using creatorName
        if (projectToToggle != null && this.getName().equals(projectToToggle.getCreatorName())) {
             try {
                 projectToToggle.setVisibility(this.name ,!projectToToggle.getVisibility()); 
                 return true;
             } catch (Exception e) {
                  System.err.println("Error toggling project visibility: " + e.getMessage());
                  return false;
             }
        } else {
             System.err.println("Error: Project not found or not created by this manager for toggling visibility.");
             return false;
        }
    }

    /**
     * Retrieves details for a specific project (typically by calling its display method).
     * Does not perform ownership check here
     *
     * @param project The project to view.
     * @return String representing project details, or an error message if project is null or view fails.
     */
    public void getProjectDetails(Project project) {
        if (project != null) {
            try {
                project.viewAllDetails(); 
            } catch (Exception e) {
                 System.err.println("Error retrieving project details: " + e.getMessage());
                 System.err.println("Error: Could not retrieve details for project " + project.getName());
            }
        } else {
            System.err.println("Error: Cannot get details for a null project.");
        }
    }

    // --- Officer Registration Methods ---

    /**
     * Processes an officer's registration request.
     * It finds the first project *managed by this manager* where the officer is pending,
     * updates the project's lists (removing from pending, potentially adding to approved),
     * and updates the officer's status.
     *
     * @param allProjectsMap    The map of all projects in the system.
     * @param officerToUpdate   The Officer whose registration is being processed.
     * @param approve           True to approve, false to reject.
     * @return true if the officer was found pending in a managed project and processed; false otherwise.
     */
    public boolean updateRegOfficer(Map<String, Project> allProjectsMap, Officer officerToUpdate, boolean approve) {
        if (officerToUpdate == null) {
            System.err.println("Error: Cannot update registration for a null officer.");
            return false;
        }
        if (allProjectsMap == null) {
             System.err.println("Error: Project map is null, cannot process officer registration.");
             return false;
        }

        boolean processed = false;
        Project targetProject = null;

        // Find the first project managed by this manager where the officer is pending
        for (Project project : allProjectsMap.values()) {
            // Check ownership using creatorName
            if (project != null && this.getName().equals(project.getCreatorName())) {
                 // Check if the officer is in the pending list for this project
                 List<Officer> pendingList = project.getPendingOfficerRegistrations(); 
                 if (pendingList != null && pendingList.contains(officerToUpdate)) {
                     try {

                        if (approve) {
                            // Add to approved list
                            project.updateArrOfOfficers(this.name, officerToUpdate);
                            System.out.println("Officer '" + officerToUpdate.getName() + "' approved for project '" + project.getName() + "'.");
                        } else {
                            System.out.println("Officer '" + officerToUpdate.getName() + "' registration rejected for project '" + project.getName() + "'.");
                        }

                        officerToUpdate.setStatus(approve); 
                        processed = true;
                        targetProject = project; // Store project for logging/confirmation
                        break; // Process only the first match found
                    } catch (Exception e) {
                         System.err.println("Error processing officer registration for " + officerToUpdate.getName() + " in project " + project.getName() + ": " + e.getMessage());
                         return false;
                    }
                 }
            }
        }

        if (!processed) {
            System.err.println("Error: Officer '" + officerToUpdate.getName() + "' not found pending registration in any project managed by " + this.getName() + ".");
        }

        return processed;
    }


    /**
     * Retrieves a list of approved officers for a specific project, ensuring the project is
     * managed by this manager.
     *
     * @param project The project whose officers are requested.
     * @return A defensive copy of the list of approved Officer objects, or an empty list if
     *         project is invalid, not managed, or has no approved officers.
     */
    public List<Officer> getApprovedOfficers(Project project) {
        // Check ownership using creatorName
        if (project != null && this.getName().equals(project.getCreatorName())) {
            try {
                List<Officer> approvedOfficers = project.getArrOfOfficers(); 
                // Return a defensive copy
                return (approvedOfficers != null) ? new ArrayList<>(approvedOfficers) : new ArrayList<>();
            } catch (Exception e) {
                 System.err.println("Error retrieving approved officers for project " + project.getName() + ": " + e.getMessage());
                 return new ArrayList<>(); // Return empty list on error
            }
        } else {
             // Project is null or not managed by this manager
            if (project != null) { // Only log if project wasn't null
                 System.err.println("Cannot get approved officers for project not managed by " + this.getName());
            }
            return new ArrayList<>(); // Return empty list
        }
    }

    // --- Applicant Processing Methods ---

    /**
     * Updates an applicant's status (accept/reject) for a project.
     * Checks for room availability before accepting.
     * Ensures the project the applicant applied to is managed by this manager.
     *
     * @param applicant The Applicant object to update (should contain project info).
     * @param accept    True to accept, false to reject.
     * @return true if status update was successful, false if failed (e.g., no room, invalid input, not managed).
     */
    public boolean updateApp(Applicant applicant, boolean accept) {
        if (applicant == null || applicant.getProject() == null) {
            System.err.println("Error: Invalid applicant or applicant is not associated with a project.");
            return false;
        }

        // Retrieve the project object from the applicant
         Project project = applicant.getProject();
         if (project == null) {
              System.err.println("Error: Applicant " + applicant.getName() + " is not linked to a valid Project object.");
              return false;
         }

        // Check if manager owns the project applicant applied to
        if (!this.getName().equals(project.getCreatorName())) {
            System.err.println("Error: Cannot update application for project '" + project.getName() + "' as it's not managed by " + this.getName());
            return false;
        }

        try {
            if (accept) {
                // Check room availability using the helper method
                if (hasRoom(project, applicant.getTypeFlat())) { 
                    // Update project lists 
                    project.updateSuccessfulApplicants(applicant);
                    applicant.setAppStatus("Successful"); 
                    System.out.println("Applicant '" + applicant.getName() + "' accepted for project '" + project.getName() + "'.");
                    return true;
                } else {
                    System.err.println("Error: Not enough room available in project '" + project.getName() + "' of type '" + applicant.getTypeFlat() + "' for applicant '" + applicant.getName() + "'.");
                    return false; // Acceptance failed due to no room
                }
            } else {
                // Rejecting the applicant
                applicant.setAppStatus("Unsuccessful");
                // Also update the project's list of unsuccessful applicants
                 project.updateUnsuccessfulApplicants(applicant); 
                System.out.println("Applicant '" + applicant.getName() + "' rejected for project '" + project.getName() + "'.");
                return true; // Rejection is considered a successful status update
            }
        } catch (Exception e) {
             System.err.println("Error updating applicant status for " + applicant.getName() + ": " + e.getMessage());
             return false;
        }
    }


    /**
     * Helper method to check room availability within a project.
     *
     * @param projectApplied The project being applied to.
     * @param flatType       The type of flat ("2-Room" or "3-Room").
     * @return true if a room of the specified type is available, false otherwise.
     */
    private boolean hasRoom(Project projectApplied, String flatType) {
        if (projectApplied == null || flatType == null) return false;
        try {
            if ("2-Room".equalsIgnoreCase(flatType)) {
                return projectApplied.getAvalNo2Room() > 0; 
            } else if ("3-Room".equalsIgnoreCase(flatType)) {
                return projectApplied.getAvalNo3Room() > 0;
            } else {
                System.err.println("Warning: Unknown flat type '" + flatType + "' requested for room check in project '" + projectApplied.getName() + "'.");
                return false; 
            }
        } catch (Exception e) {
             System.err.println("Error checking room availability: " + e.getMessage());
             return false;
        }
    }


    /**
     * Processes an applicant's withdrawal request.
     * Ensures the project is managed by this manager.
     * Calls helper methods to handle the logic based on acceptance/rejection.
     *
     * @param applicant The applicant requesting withdrawal (should contain project info).
     * @param accept    True to accept withdrawal, false to reject.
     * @return true if the status was updated successfully, false otherwise.
     */
    public boolean updateWithdrawal(Applicant applicant, boolean accept) {
        if (applicant == null) {
            System.err.println("Error: Cannot process withdrawal for a null applicant.");
            return false;
        }

        Project project = applicant.getProject();
        if (project == null) {
            System.err.println("Error: Applicant " + applicant.getName() + " is not linked to a valid Project object for withdrawal.");
            return false;
        }

        // Check if manager owns the project
        if (!this.getName().equals(project.getCreatorName())) {
            System.err.println("Error: Cannot update withdrawal for project '" + project.getName() + "' not managed by " + this.getName() + ".");
            return false;
        }

        try {
            if (accept) {
                handleAcceptWithdraw(applicant); // Use private helper
                System.out.println("Withdrawal accepted for '" + applicant.getName() + "'.");
            } else {
                handleRejectWithdraw(applicant); // Use private helper
                System.out.println("Withdrawal rejected for '" + applicant.getName() + "'.");
            }
            return true; // Status update was successful
        } catch (Exception e) {
             System.err.println("Error processing withdrawal for " + applicant.getName() + ": " + e.getMessage());
             return false;
        }
    }


    /**
     * Private helper to handle the logic when a withdrawal request is accepted.
     * Updates project lists and applicant status.
     *
     * @param applicant The applicant whose withdrawal is accepted.
     */
    private void handleAcceptWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null for withdrawal acceptance");
        Project project = applicant.getProject(); 

        if (project != null) {
             // Add applicant to the project's list of withdrawal requests 
             project.updateWithdrawRequests(applicant); 
             System.out.println("Note: Room increment logic upon withdrawal acceptance needs implementation in Project class.");
        } else {
            // This case should ideally be caught earlier, but log warning if it happens.
            System.err.println("Warning: Applicant '" + applicant.getName() + "' has null project during withdrawal acceptance processing.");
        }

        applicant.setAppStatus("Withdrawn"); // Set status
        applicant.setWithdrawalStatus(true);  // Indicate withdrawal was processed positively (accepted)
    }

    /**
     * Private helper to handle the logic when a withdrawal request is rejected.
     * Updates applicant status (remains Successful/Booked) and withdrawal status flag.
     *
     * @param applicant The applicant whose withdrawal is rejected.
     */
    private void handleRejectWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null for withdrawal rejection");
        Project project = applicant.getProject(); 

        if (project != null) {
            // Remove applicant from the project's list of withdrawal requests
             project.updateWithdrawToUnsuccessful(applicant); // Needs to remove from withdrawRequests list
        } else {
            System.err.println("Warning: Applicant '" + applicant.getName() + "' has null project during withdrawal rejection processing.");
        }

        // Applicant status likely remains "Successful" or "Booked"
        // Only update the withdrawal status flag to indicate rejection
        applicant.setWithdrawalStatus(false); // Indicate withdrawal request was processed negatively (rejected)
    }


    // --- Reporting ---

    /**
     * Generates a report of successful applicants for the specified project based on a filter key.
     * Ensures the project is managed by this manager.
     * Filters are case-insensitive.
     *
     * @param project   The project for which the report is generated.
     * @param filterKey The key determining the filter ("all", "married", "unmarried", "flat2room", etc.).
     * @return A list of formatted report entries for matching applicants, or an empty list if
     *         project is invalid/unmanaged or no applicants match.
     */
    public List<String> generateApplicantReport(Project project, String filterKey) {
        List<String> report = new ArrayList<>();

        // Check ownership using creatorName
        if (project == null || !this.getName().equals(project.getCreatorName())) {
            System.err.println("Error: Cannot generate report for null, unmanaged, or non-owned project.");
            return report; // Return empty report
        }

        // Get the list of successful applicants for this project
        List<Applicant> applicantsToReport = project.getSuccessfulApplicants(); 
        if (applicantsToReport == null || applicantsToReport.isEmpty()) {
            System.out.println("No successful applicants found for project '" + project.getName() + "' to generate a report.");
            return report; // Return empty report
        }

        String filterKeyLower = filterKey.toLowerCase().trim();

        for (Applicant applicant : applicantsToReport) {
            if (applicant == null) continue; // Skip null entries

            boolean include = false;
            try {
                // Prepare data for filtering (handle potential nulls gracefully)
                String applicantMaritalStatus = Objects.toString(applicant.getMaritalStatus(), "").toLowerCase();
                String flatType = Objects.toString(applicant.getTypeFlat(), "").toLowerCase(); 

                // Apply filters
                switch (filterKeyLower) {
                    case "all":
                        include = true;
                        break;
                    case "married":
                        include = applicantMaritalStatus.equals("married");
                        break;
                    case "unmarried":
                        include = !applicantMaritalStatus.equals("married") && !applicantMaritalStatus.isEmpty(); // Check not empty too
                        break;
                    case "flat2room":
                        include = flatType.equals("2-room"); // Match exact format used in project/applicant
                        break;
                    case "flat3room":
                        include = flatType.equals("3-room"); // Match exact format
                        break;
                    case "married_flat2room":
                        include = applicantMaritalStatus.equals("married") && flatType.equals("2-room");
                        break;
                    // Add more filters as needed
                    default:
                        System.out.println("Warning: Unknown filter key '" + filterKey + "'. Including all successful applicants.");
                        include = true; // Default to include if filter is unknown? Or exclude? Let's include.
                        break;
                }

                // If applicant matches filter, format and add to report
                if (include) {
                    String reportEntry = String.format("Project: %s, Flat Type: %s, Age: %d, Marital Status: %s, Name: %s, NRIC: %s",
                                                       project.getName(),
                                                       applicant.getTypeFlat(), // Original case for display
                                                       applicant.getAge(),
                                                       applicant.getMaritalStatus(), // Original case
                                                       applicant.getName(),
                                                       applicant.getNric()); // Added NRIC for completeness
                    report.add(reportEntry);
                }
            } catch (Exception e) {
                 System.err.println("Error processing applicant " + applicant.getName() + " for report: " + e.getMessage());
                 // Decide whether to skip this applicant or halt report generation
            }
        } // End loop through applicants

        return report;
    }


    // --- Standard Java Methods ---

    /**
     * Provides a string representation of the Manager object.
     * Calls the superclass toString() and adds manager-specific info (if any).
     * Since managedProjects list is removed, it might just call super.toString().
     */
    @Override
    public String toString() {
        // Example override calling super and adding info (if there was manager-specific data)
        // Since managedProjects is removed, just calling super might be sufficient.
        // return super.toString() + " [Role=Manager]"; // Example minimal addition
        return super.toString(); // Keep it simple for now
    }

}