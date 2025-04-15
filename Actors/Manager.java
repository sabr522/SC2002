package Actors;

import Actors.User; // Base class
import Actors.Applicant;
import Actors.Officer;
import Project.Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Manager user, inheriting common attributes from User.
 * Manages projects, officers, and applicant processes.
 */
public class Manager extends User { // Extend the abstract User class

    // Manager-specific attribute 
    private List<Project> managedProjects; // List of Project objects this manager oversees

    /**
     * Constructor for Manager. Initializes inherited User fields and Manager-specific fields.
     *
     * @param name           Manager's name (for User).
     * @param nric           Manager's NRIC (for User).
     * @param age            Manager's age (for User).
     * @param maritalStatus  Manager's marital status (for User).
     * @param password       Manager's password (for User).
     * @param initialProjects List of projects initially managed by this manager.
     */
    public Manager(String name, String nric, int age, String maritalStatus, String password, List<Project> initialProjects) {
        // Call the User constructor FIRST to initialize common fields
        super(name, nric, age, maritalStatus, password, "Manager"); // Role is fixed as "Manager"

        // Initialize Manager-specific fields
        this.managedProjects = (initialProjects != null) ? new ArrayList<>(initialProjects) : new ArrayList<>();
        System.out.println("Manager " + this.name + " logged in."); // Use inherited name
    }

    // --- Getters ---

    /**
     * Returns a defensive copy of the list of projects managed by this manager.
     * Ensures the internal list cannot be modified externally.
     * @return A new list containing projects managed by this manager.
     */
    public List<Project> getManagedProjects() {
        return new ArrayList<>(managedProjects);
    }

    /**
     * Filters and returns only the projects created by this specific manager instance.
     * Uses the inherited 'name' field for comparison.
     * @return A new list containing projects created by this manager.
     */
    public List<Project> getAllProjectsManagedByThisManager() {
        List<Project> ownProjects = new ArrayList<>();
        for (Project p : this.managedProjects) { 
            if (p != null && this.name.equals(p.getCreatorName())) {
                ownProjects.add(p);
            }
        }
        return ownProjects;
    }


    // --- Project Management Methods --- (Kept as provided, using inherited this.name)

     /**
     * Creates a new project and adds it to the managed list.
     * Checks for application period clashes against other projects managed by this manager.
     * @param name Project's name.
     * @param visibility Project's initial visibility status.
     * @param neighbourhood Project's neighbourhood.
     * @param appOpeningDate Project's application opening date.
     * @param appClosingDate Project's application closing date.
     * @param num2Rooms Number of 2-room flats.
     * @param num3Rooms Number of 3-room flats.
     * @return The created Project object, or null if creation failed (e.g., period clash).
     */
    public Project createProject(String name, Boolean visibility, String neighbourhood, LocalDate appOpeningDate, LocalDate appClosingDate, int num2Rooms, int num3Rooms) {
        if (isAnyProjectClashing(appOpeningDate, appClosingDate, null)) {
             System.err.println("Error: Application period clashes with an existing project managed by " + this.name); // Use inherited name
             return null;
        }
        // Pass inherited name as creatorName
        Project newProject = new Project(name, visibility, this.name, neighbourhood, appOpeningDate, appClosingDate, num2Rooms, num3Rooms);
        this.managedProjects.add(newProject);
        return newProject;
    }

    /**
      * Edits the details of an existing project managed by this manager.
      * Allows partial updates; null parameters mean the corresponding field is not updated.
      * Checks for date clashes before applying changes.
      * @param projectToEdit The specific Project object to edit (must be managed by this manager).
      * @param placeName New place name (or null to keep existing).
      * @param neighbourhood New neighbourhood (or null to keep existing).
      * @param appOpeningDate New application opening date (or null to keep existing).
      * @param appClosingDate New application closing date (or null to keep existing).
      * @param num2Rooms New number of 2-room flats (or null to keep existing). Use Integer wrapper type.
      * @param num3Rooms New number of 3-room flats (or null to keep existing). Use Integer wrapper type.
      * @return true if successful, false otherwise (e.g., project not found, not managed, or date clash).
      */
    public boolean editProject(Project projectToEdit, String placeName, String neighbourhood, LocalDate appOpeningDate, LocalDate appClosingDate, Integer num2Rooms, Integer num3Rooms) {
        // Check ownership using inherited name
        if (projectToEdit != null && this.managedProjects.contains(projectToEdit) && this.name.equals(projectToEdit.getCreatorName())) {
            String updatedPlaceName = (placeName != null) ? placeName : projectToEdit.getPlaceName();
            String updatedNeighbourhood = (neighbourhood != null) ? neighbourhood : projectToEdit.getNeighbourhood();
            LocalDate updatedOpening = (appOpeningDate != null) ? appOpeningDate : projectToEdit.getAppOpeningDate();
            LocalDate updatedClosing = (appClosingDate != null) ? appClosingDate : projectToEdit.getAppClosingDate();
            int updatedNum2Rooms = (num2Rooms != null) ? num2Rooms : projectToEdit.getNum2RoomUnits();
            int updatedNum3Rooms = (num3Rooms != null) ? num3Rooms : projectToEdit.getNum3RoomUnits();

             if (appOpeningDate != null || appClosingDate != null) {
                if (isAnyProjectClashing(updatedOpening, updatedClosing, projectToEdit.getName())) {
                     System.err.println("Error: New application period clashes with an existing project.");
                     return false;
                 }
             }
            projectToEdit.setDetails(updatedPlaceName, updatedNeighbourhood, updatedOpening, updatedClosing, updatedNum2Rooms, updatedNum3Rooms);
            return true;
        }
        System.err.println("Error: Project not found or not managed by this manager.");
        return false;
    }

    /**
      * Checks if the given date range clashes with any existing project managed by this manager,
      * optionally skipping a specific project by name.
      * @param appOpeningDate The opening date to check.
      * @param appClosingDate The closing date to check.
      * @param skipProjectName The name of the project to ignore during the check (can be null).
      * @return true if a clash is found, false otherwise.
      */
    private boolean isAnyProjectClashing(LocalDate appOpeningDate, LocalDate appClosingDate, String skipProjectName) {
        for (Project existingProject : this.managedProjects) {
            if (existingProject == null) continue;
            if (skipProjectName != null && existingProject.getName().equals(skipProjectName)) continue;
            if (existingProject.isClashing(appOpeningDate, appClosingDate)) return true;
        }
        return false;
    }

    /**
     * Deletes a project managed by this manager.
     * @param projectToDelete The Project object to delete.
     * @return true if successful, false otherwise (project null, not found, or not created by this manager).
     */
    public boolean delProject(Project projectToDelete) {
        // Check ownership using inherited name
        if (projectToDelete != null && this.name.equals(projectToDelete.getCreatorName())) {
            boolean removed = this.managedProjects.remove(projectToDelete);
            if (!removed) {
                 System.err.println("Error: Project was not found in the managed list for deletion.");
                 return false;
            }
            return true;
        }
         System.err.println("Error: Cannot delete null project or project not created by this manager.");
        return false;
    }

     /**
      * Toggles the visibility of a project created by this manager.
      * @param projectToToggle The Project object to toggle.
      * @return true if successful, false otherwise.
      */
    public boolean toggleProject(Project projectToToggle) {
        // Check ownership using inherited name
        if (projectToToggle != null && this.managedProjects.contains(projectToToggle) && this.name.equals(projectToToggle.getCreatorName())) {
            projectToToggle.setVisibility(!projectToToggle.getVisibility());
            return true;
        }
         System.err.println("Error: Project not found or not created by this manager for toggling visibility.");
        return false;
    }

    /**
     * Retrieves details for a specific project.
     * @param project The project to view.
     * @return String representing project details, or an error message if project is null.
     */
    public String getProjectDetails(Project project) {
         if (project != null) {
             // Assumes Project.viewAllDetails() returns a String or prints details
             return project.viewAllDetails();
         } else {
              return "Error: Cannot get details for a null project.";
         }
    }

    // --- Officer Registration Methods --- (Kept as provided, using inherited this.name)

    /**
     * Processes an officer's registration request found within a project managed by this manager.
     * It finds the first project managed by this manager where the officer is pending,
     * removes the officer from that project's pending list, and optionally adds them
     * to the same project's approved list. Updates the officer's status.
     *
     * @param officerToUpdate The Officer whose registration is being processed.
     * @param approve         True to approve, false to reject.
     * @return true if the officer was found pending in a managed project and processed; false otherwise.
     */
    public boolean updateRegOfficer(Officer officerToUpdate, boolean approve) {
        if (officerToUpdate == null) {
            System.err.println("Error: Cannot update registration for a null officer.");
            return false;
        }
        for (Project project : this.managedProjects) {
            // Check ownership using inherited name
            if (project != null && this.name.equals(project.getCreatorName())) {
                List<Officer> pendingList = project.getPendingOfficerRegistrations();
                if (pendingList != null && pendingList.contains(officerToUpdate)) {
                    pendingList.remove(officerToUpdate);
                    if (approve) {
                        project.updateArrOfOfficers(officerToUpdate);
                        System.out.println("Officer " + officerToUpdate.getName() + " approved for project " + project.getName() + ".");
                    } else {
                        System.out.println("Officer " + officerToUpdate.getName() + " registration rejected for project " + project.getName() + ".");
                    }
                    officerToUpdate.setStatus(approve);
                    return true;
                }
            }
        }
        System.err.println("Error: Officer " + officerToUpdate.getName() + " not found pending in any project managed by " + this.name); // Use inherited name
        return false;
    }

    /**
     * Retrieves a list of approved officers for a specific project, ensuring the project is managed by this manager.
     * @param project The project whose officers are requested.
     * @return A defensive copy of the list of approved Officer objects, or an empty list if project is invalid or not managed.
     */
    public List<Officer> getApprovedOfficers(Project project) {
        // Check ownership using inherited name
        if (project != null && this.managedProjects.contains(project) && this.name.equals(project.getCreatorName())) {
            List<Officer> approvedOfficers = project.getArrOfOfficers();
            return (approvedOfficers != null) ? new ArrayList<>(approvedOfficers) : new ArrayList<>();
        }
        return new ArrayList<>();
    }


    // --- Applicant Processing Methods --- (Kept as provided)

     /**
     * Updates an applicant's status (accept/reject) for a project.
     * Checks for room availability before accepting.
     * @param applicant The Applicant to update.
     * @param accept True to accept, false to reject.
     * @return true if status update was successful, false if failed (e.g., no room, invalid input).
     */
    public boolean updateApp(Applicant applicant, boolean accept) {
        if (applicant == null || applicant.getProject() == null) {
            System.err.println("Error: Invalid applicant or applicant is not associated with a project.");
            return false;
        }
        Project project = applicant.getProject();

        // Optional check if manager must own the project applicant applied to
        if (!this.managedProjects.contains(project) || !this.name.equals(project.getCreatorName())) {
             System.err.println("Error: Cannot update application for a project not managed by " + this.name);
             return false;
        }

        if (accept) {
            if (hasRoom(project, applicant.getTypeFlat())) {
                project.updateSuccessfulApplicants(applicant);
                applicant.setAppStatus("Successful");
                System.out.println("Applicant " + applicant.getName() + " accepted for project " + project.getName() + ".");
                return true;
            } else {
                System.err.println("Error: Not enough room available in project " + project.getName() + " for applicant " + applicant.getName());
                return false;
            }
        } else {
            applicant.setAppStatus("Unsuccessful");
             // Optional: ensure removal from successful list if previously accepted
             // project.removeSuccessfulApplicant(applicant);
             System.out.println("Applicant " + applicant.getName() + " rejected for project " + project.getName() + ".");
            return true;
        }
    }

    /**
      * Helper method to check room availability within a project.
      * @param projectApplied The project being applied to.
      * @param flatType The type of flat ("2-Room" or "3-Room").
      * @return true if a room of the specified type is available, false otherwise.
      */
    private boolean hasRoom(Project projectApplied, String flatType) {
        if (projectApplied == null || flatType == null) return false;
        if ("2-Room".equalsIgnoreCase(flatType)) {
            return projectApplied.getAvalNo2Room() > 0;
        } else if ("3-Room".equalsIgnoreCase(flatType)) {
            return projectApplied.getAvalNo3Room() > 0;
        }
        System.err.println("Warning: Unknown flat type '" + flatType + "' for room check.");
        return false;
    }

    /**
     * Processes an applicant's withdrawal request.
     * @param applicant The applicant requesting withdrawal.
     * @param accept True to accept withdrawal, false to reject.
     * @return true if the status was updated, false if applicant is null.
     */
    public boolean updateWithdrawal(Applicant applicant, boolean accept) {
        if (applicant == null) {
             System.err.println("Error: Cannot process withdrawal for a null applicant.");
            return false;
        }
        // Optional check: Ensure project is managed by this manager
        Project project = applicant.getProject();
        if (project == null || !this.managedProjects.contains(project) || !this.name.equals(project.getCreatorName())) {
             System.err.println("Error: Cannot update withdrawal for a project not managed by " + this.name);
             return false;
        }

        if (accept) {
            handleAcceptWithdraw(applicant);
            System.out.println("Withdrawal accepted for " + applicant.getName() + ".");
        } else {
            handleRejectWithdraw(applicant);
            System.out.println("Withdrawal rejected for " + applicant.getName() + ".");
        }
        return true;
    }

    // --- Private Helper Methods for Withdrawal Logic --- (Kept as provided)

    private void handleAcceptWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null for withdrawal acceptance");
        Project project = applicant.getProject();
        if (project != null) {
            project.updateWithdrawRequests(applicant);
            // project.incrementRoom(applicant.getTypeFlat()); // Make sure Project handles this correctly
        } else {
             System.err.println("Warning: Applicant " + applicant.getName() + " has null project during withdrawal acceptance.");
        }
        applicant.setAppStatus("Withdrawn");
        applicant.setWithdrawalStatus(true);
    }

    private void handleRejectWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null for withdrawal rejection");
        Project project = applicant.getProject();
        if (project != null) {
            project.updateWithdrawToUnsuccessful(applicant);
        } else {
            System.err.println("Warning: Applicant " + applicant.getName() + " has null project during withdrawal rejection.");
        }
        applicant.setWithdrawalStatus(false);
    }

    // --- Reporting --- (Kept as provided, using inherited this.name)

     /**
      * Generates a report of successful applicants for the specified project based on a filter key.
      * Filters are case-insensitive.
      * @param project The project for which the report is generated. Ensures project is managed by this manager.
      * @param filterKey The key determining the filter ("all", "married", "unmarried", "flat2room", "flat3room", "married_flat2room").
      * @return A list of formatted report entries for matching applicants, or an empty list if project is invalid/unmanaged or no applicants match.
      */
    public List<String> generateApplicantReport(Project project, String filterKey) {
        List<String> report = new ArrayList<>();
        // Check ownership using inherited name
        if (project == null || !this.managedProjects.contains(project) || !this.name.equals(project.getCreatorName())) {
            System.err.println("Error: Cannot generate report for null, unmanaged, or non-owned project.");
            return report;
        }

        List<Applicant> applicantsToReport = project.getSuccessfulApplicants();
        if (applicantsToReport == null || applicantsToReport.isEmpty()){
            System.out.println("No successful applicants found for project " + project.getName() + " to report.");
            return report;
        }

        for (Applicant applicant : applicantsToReport) {
            if (applicant == null) continue;
            boolean include = false;
            String applicantMaritalStatus = Objects.toString(applicant.getMaritalStatus(), "").toLowerCase();
            String flatType = Objects.toString(applicant.getTypeFlat(), "").toLowerCase(); // Corrected comparison value
            int age = applicant.getAge();

            switch (filterKey.toLowerCase()) {
                case "all": include = true; break;
                case "married": include = applicantMaritalStatus.equals("married"); break;
                case "unmarried": include = !applicantMaritalStatus.equals("married"); break;
                case "flat2room": include = flatType.equals("2-room"); break; // Match format
                case "flat3room": include = flatType.equals("3-room"); break; // Match format
                case "married_flat2room": include = applicantMaritalStatus.equals("married") && flatType.equals("2-room"); break; // Match format
                default: include = true; System.out.println("Warning: Unknown filter key '" + filterKey + "'."); break;
            }

            if (include) {
                String reportEntry = String.format("Project: %s, Flat Type: %s, Age: %d, Marital Status: %s, Name: %s",
                                                  project.getName(), applicant.getTypeFlat(), age, applicant.getMaritalStatus(), applicant.getName());
                report.add(reportEntry);
            }
        }
        return report;
    }

    // Inherited toString() can be used, or override for manager-specific info
    @Override
    public String toString() {
        // Example override calling super and adding info
        return super.toString() + " [Managing " + managedProjects.size() + " projects]";
    }
}