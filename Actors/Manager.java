package Actors;

import Project.Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; 

public class Manager {
    private String name;
    private List<Project> managedProjects;

    // --- Constructor taking initial lists ---
    public Manager(String name, List<Project> initialProjects) {
        this.name = name;
        this.managedProjects = (initialProjects != null) ? new ArrayList<>(initialProjects) : new ArrayList<>();
        System.out.println("Manager " + name + " logged in."); 
    }

    // --- Getters for name and lists ---
    public String getName() {
        return name;
    }

    public List<Project> getManagedProjects() {
        return new ArrayList<>(managedProjects);
    }

     public List<Project> getAllProjectsManagedByThisManager() {
        List<Project> ownProjects = new ArrayList<>();
        for (Project p : managedProjects) {
            if (p != null && this.name.equals(p.getCreatorName())) {
                ownProjects.add(p);
            }
        }
        return ownProjects;
    }


    public List<Officer> getPendingOfficerRegistrations(Project project) {
        // Return an unmodifiable list or a copy
        return new ArrayList<>(project.getPendingOfficerRegistrations());
    }

    // --- Project Management Methods (expecting UI to handle selection) ---

    /**
     * Creates a new project and adds it to the managed list.
     * Checks for application period clashes.
     * @param name Project's name
     * @param visibility Project's visibility
     * @param neighbourhood Project's neighbourhood
     * @param appOpeningDate Project's open application date
     * @param appClosingDate Project's closing application date
     * @param no2Rooms Number of 2-room flats
     * @param no3Rooms Number of 3-room flats
     * @return The created Project object, or null if creation failed (e.g., period clash).
     */
    public Project createProject(String name, Boolean visibility, String neighbourhood, LocalDate appOpeningDate, LocalDate appClosingDate, int no2Room, int no3Room) {
        // Check for clashes within this manager's projects
        for (Project existingProject : this.managedProjects) {
            if (existingProject != null && existingProject.isClashing(appOpeningDate, appClosingDate)) {
                System.err.println("Error: Application period clashes with an existing project managed by " + this.name);
                return null; 
            }
        }

        Project newProject = new Project(name, visibility, this.name, neighbourhood, appOpeningDate, appClosingDate, no2Rooms, no3Rooms);
        this.managedProjects.add(newProject);
        return newProject; // Return the created project
    }

    /**
     * Edits the details of an existing project managed by this manager.
     * @param projectToEdit The specific Project object to edit (must be managed by this manager).
     * @param placeName New place name
     * @param neighbourhood New neighbourhood
     * @param appPeriod New application period
     * @param num2Rooms New number of 2-room flats
     * @param num3Rooms New number of 3-room flats
     * @return true if successful, false otherwise (e.g., project not found or not managed by this manager).
     */
    public boolean editProject(Project projectToEdit, String placeName, String neighbourhood, LocalDate appOpeningDate, LocalDate appClosingDate, int num2Rooms, int num3Rooms) {
        if (projectToEdit != null && this.managedProjects.contains(projectToEdit) && this.name.equals(projectToEdit.getCreatorName())) {
            // Retrieve existing values if new ones are null
            String updatedPlaceName = (placeName != null) ? placeName : projectToEdit.getPlaceName();
            String updatedNeighbourhood = (neighbourhood != null) ? neighbourhood : projectToEdit.getNeighbourhood();
            LocalDate updatedOpening = (appOpeningDate != null) ? appOpeningDate : projectToEdit.getAppOpeningDate();
            LocalDate updatedClosing = (appClosingDate != null) ? appClosingDate : projectToEdit.getAppClosingDate();
            int updatedNum2Rooms = (num2Rooms != null) ? num2Rooms : projectToEdit.getNum2RoomUnits();
            int updatedNum3Rooms = (num3Rooms != null) ? num3Rooms : projectToEdit.getNum3RoomUnits();
            
            // date clash check before setting new values
            if (appOpeningDate != null || appClosingDate != null) {
                if (isAnyProjectClashing(updatedOpening, updatedClosing, projectToEdit.getName())) {
                    System.err.println("Error: Application period clashes with an existing project");
                    return false;
                }
            }
            projectToEdit.setDetails(updatedPlaceName, updatedNeighbourhood,
                                    updatedOpening, updatedClosing,
                                    updatedNum2Rooms, updatedNum3Rooms);
            return true;
    }
    return false;
    }

    // --- Private Helper Methods for date clashing Logic ---
    private boolean isAnyProjectClashing(LocalDate appOpeningDate, LocalDate appClosingDate, String skipProjectName) {
        for (Project existingProject : this.managedProjects) {
            // Skip null projects and the project that matches the skipProjectName (if provided)
            if (existingProject == null) {
                continue;
            }
            if (skipProjectName != null && existingProject.getName().equals(skipProjectName)) {
                continue;
            }
            // Assuming isClashing() compares the given dates with the project's own date range.
            if (existingProject.isClashing(appOpeningDate, appClosingDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a project managed by this manager.
     * @param projectToDelete The Project object to delete.
     * @return true if successful, false otherwise.
     */
    public boolean delProject(Project projectToDelete) {
        if (projectToDelete != null && this.name.equals(projectToDelete.getCreatorName())) {
            return this.managedProjects.remove(projectToDelete);
        }
        return false; // Not managed by this manager or null
    }

    /**
     * Toggles the visibility of a project managed by this manager.
     * @param projectToToggle The Project object to toggle.
     * @return true if successful, false otherwise.
     */
    public boolean toggleProject(Project projectToToggle) {
        if (projectToToggle != null && this.managedProjects.contains(projectToToggle) && this.name.equals(projectToToggle.getCreatorName())) {
            projectToToggle.setVisibility(!projectToToggle.getVisibility());
            return true;
        }
        return false;
    }

    /**
     * Retrieves details for a specific project. (Assumes Project has a meaningful method).
     * @param project The project to view.
     * @return String or structured data representing project details, or null if project is null.
     */
    public String getProjectDetails(Project project) {
         if (project != null) {

            // Assume project has a method returning details as a String
            return project.getAllDetailsAsString(); // Or similar method

         } else {
             return "No project selected or project is null.";
         }
    }


    // --- Officer Registration Methods ---

    /**
     * Processes an officer's registration request.
     * Searches all projects managed by this manager (by creator name) for the officer in the pending list.
     * If approved, removes the officer from pending and adds it to the approved officer list.
     *
     * @param officerToUpdate The Officer whose registration is being processed.
     * @param approve         True to approve, false to reject.
     * @return true if the update was processed; false if the officer was not found.
     */
    public boolean updateRegOfficer(Officer officerToUpdate, boolean approve) {
        if (officerToUpdate == null) {
            return false;
        }
        // Iterate through the projects managed by this manager (where manager is the creator)
        for (Project project : this.managedProjects) {
            if (project != null && this.name.equals(project.getCreatorName())) {
                // Check for the officer in the project's pending list
                if (project.getPendingOfficerRegistrations().contains(officerToUpdate)) {
                    // Remove the officer from the pending list
                    project.getPendingOfficerRegistrations().remove(officerToUpdate);
                    if (approve) {
                        // Add officer to the project's approved (successful) list.
                        project.updateArrOfOfficers(officerToUpdate);
                        System.out.println("Officer " + officerToUpdate.getName() + " approved and added to project " + project.getName() + ".");
                    } else {
                        System.out.println("Officer " + officerToUpdate.getName() + " registration rejected for project " + project.getName() + ".");
                    }
                    // Update officer’s status accordingly.
                    officerToUpdate.setStatus(approve);
                    return true;
                }
            }
        }
        // Officer not found in any pending list
        return false;
    }

    /**
     * Retrieves a list of approved officers for a specific project managed by this manager.
     * @param project The project whose officers are requested.
     * @return A List of approved Officer objects, or an empty list if none/project invalid.
     */
    public List<Officer> getApprovedOfficers(Project project) {
        if (project != null && this.managedProjects.contains(project) && this.name.equals(project.getCreatorName())) {
            // Assume Project has getArrOfOfficers() returning List<Officer>
            return project.getArrOfOfficers();
        }
        return new ArrayList<>(); // Return empty list if project not valid or not managed
    }


    // --- Applicant Processing Methods ---

    /**
     * Updates an applicant's status (accept/reject).
     * @param applicant The Applicant to update.
     * @param accept True to accept, false to reject.
     * @return true if successful, false if failed (e.g., no room, applicant invalid).
     */
    public boolean updateApp(Applicant applicant, boolean accept) {
        if (applicant == null || applicant.getProject() == null) {
            System.err.println("Error: Invalid applicant or applicant project.");
            return false;
        }

        Project project = applicant.getProject();

        if (accept) {
            if (hasRoom(project, applicant.getTypeFlat())) {
                project.updateSuccessfulApplicants(applicant); // Only updates the project's list
                applicant.setAppStatus("Successful");
                System.out.println("Applicant " + applicant.getName() + " accepted.");
                return true;
            } else {
                System.err.println("Error: Not enough room available for applicant " + applicant.getName());
                return false;
            }
        } else {
            applicant.setAppStatus("Unsuccessful");
            System.out.println("Applicant " + applicant.getName() + " rejected.");
            return true;
        }
    }
    // --- Private Helper Methods for applicant room check Logic ---
    private boolean hasRoom(Project projectApplied, String flatType) {
        if ("2Room".equals(flatType)) {
            return projectApplied.getNo2Room() > 0;
        } else if ("3Room".equals(flatType)) {
            return projectApplied.getNo3Room() > 0;
        }
        return false; // Return false if flatType is neither "2-Rooms" nor "3-Rooms"
    }

    /**
     * Processes an applicant's withdrawal request.
     * @param applicant The applicant requesting withdrawal.
     * @param accept True to accept withdrawal, false to reject.
     * @return true if the status was updated, false otherwise.
     */
    public boolean updateWithdrawal(Applicant applicant, boolean accept) {
        if (applicant == null) {
            return false;
        }

        if (accept) {
            handleAcceptWithdraw(applicant);
            System.out.println("Withdrawal accepted for " + applicant.getName());
        } else {
            handleRejectWithdraw(applicant);
            System.out.println("Withdrawal rejected for " + applicant.getName());
        }
        return true;
    }

    // --- Private Helper Methods for Withdrawal Logic ---

    private void handleAcceptWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null");
        Project project = applicant.getProject();
        if (project != null) {
            project.updateWithdrawRequests(applicant);
            project.incrementRoom(applicant.getTypeFlat()); // Free up the room
        }
        
        applicant.setAppStatus("Withdrawn");
        applicant.setWithdrawalStatus(true); // Mark withdrawal processed
    }

    private void handleRejectWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null");
        Project project = applicant.getProject();
        if (project != null) {
            project.updateWithdrawToUnsuccessful(applicant);
        }        
        applicant.setWithdrawalStatus(false); // Mark withdrawal request as not approved
    }


    // --- Reporting ---

    /**
     * Generates a report of applicants for the specified project based on a filter key.
     * Supported filter keys (case-insensitive):
     *   - "all": Show all applicants.
     *   - "married": Only show applicants with marital status "married".
     *   - "unmarried": Only show applicants who are not "married".
     *   - "flat2room": Only show applicants whose flat booking is of type "2room".
     *   - "flat3room": Only show applicants whose flat booking is of type "3room".
     *   - "married_flat2room": Only show married applicants whose flat booking is "2room".
     *   - (Additional keys can be added as needed.)
     *
     * @param project The project for which the report is generated.
     * @param filterKey The key that determines which filter to apply.
     * @return A list of formatted report entries.
     */
    public List<String> generateApplicantReport(Project project, String filterKey) {
        List<String> report = new ArrayList<>();
        for (Applicant applicant : project.getSuccessfulApplicants()) {
            boolean include = false;
            String applicantMaritalStatus = applicant.getMaritalStatus();
            String flatType = applicant.getTypeFlat();
            
            switch(filterKey.toLowerCase()) {
                case "all":
                    include = true;
                    break;
                case "married":
                    include = applicantMaritalStatus.equalsIgnoreCase("married");
                    break;
                case "unmarried":
                    include = !applicantMaritalStatus.equalsIgnoreCase("married");
                    break;
                case "flat2room":
                    include = flatType.equalsIgnoreCase("2room");
                    break;
                case "flat3room":
                    include = flatType.equalsIgnoreCase("3room");
                    break;
                case "married_flat2room":
                    include = applicantMaritalStatus.equalsIgnoreCase("married")
                            && flatType.equalsIgnoreCase("2room");
                    break;
                default:
                    // If filter key is unknown, default to showing all.
                    include = true;
            }
            
            if (include) {
                String reportEntry = "Project: " + project.getName() +  
                        ", Flat Type: " + flatType +
                        ", Age: " + applicant.getAge() +           
                        ", Marital Status: " + applicantMaritalStatus;
                report.add(reportEntry);
            }
        }
        return report;
    }

    // toString method for basic Manager info
    @Override
    public String toString() {
        return "Manager [name=" + name + ", managing " + managedProjects.size() + " projects.";
    }
}