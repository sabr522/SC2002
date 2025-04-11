package Actors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; 

public class Manager {
    private String name;
    private List<Project> managedProjects;
    private List<Officer> pendingOfficerRegistrations = new ArrayList<>();

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
            // getName or getManagerName?
            if (p != null && this.name.equals(p.getManagerName())) {
                ownProjects.add(p);
            }
        }
        return ownProjects;
    }


    public List<Officer> getPendingOfficerRegistrations() {
        // Return an unmodifiable list or a copy
        return new ArrayList<>(pendingOfficerRegistrations);
    }

    // --- Project Management Methods (expecting UI to handle selection) ---

    /**
     * Creates a new project and adds it to the managed list.
     * Checks for application period clashes.
     * @param placeName Project's place name
     * @param neighbourhood Project's neighbourhood
     * @param appPeriod Project's application period (used for clash check)
     * @param num2Rooms Number of 2-room flats
     * @param num3Rooms Number of 3-room flats
     * @return The created Project object, or null if creation failed (e.g., period clash).
     */
    public Project createProject(String placeName, String neighbourhood, String appPeriod, int num2Rooms, int num3Rooms) {
        // Check for clashes within this manager's projects
        for (Project existingProject : this.managedProjects) {
            if (existingProject != null && existingProject.getAppPeriod().equals(appPeriod)) {
                System.err.println("Error: Application period clashes with an existing project managed by " + this.name);
                return null; 
            }
        }

        // Assume Project constructor exists: Project(name, neigh, period, r2, r3, managerName)
        Project newProject = new Project(placeName, neighbourhood, appPeriod, num2Rooms, num3Rooms, this.name);
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
    public boolean editProject(Project projectToEdit, String placeName, String neighbourhood, String appPeriod, int num2Rooms, int num3Rooms) {
        if (projectToEdit != null && this.managedProjects.contains(projectToEdit) && this.name.equals(projectToEdit.getManagerName())) {
            // Assume Project has a setDetails method
            projectToEdit.setDetails(placeName, neighbourhood, appPeriod, num2Rooms, num3Rooms);
            return true;
        }
        return false; // Project not found or not managed by this manager
    }

    /**
     * Deletes a project managed by this manager.
     * @param projectToDelete The Project object to delete.
     * @return true if successful, false otherwise.
     */
    public boolean delProject(Project projectToDelete) {
        if (projectToDelete != null && this.name.equals(projectToDelete.getManagerName())) {
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
        if (projectToToggle != null && this.managedProjects.contains(projectToToggle) && this.name.equals(projectToToggle.getManagerName())) {
            // Assume Project has isVisible() and setVisibility()
            projectToToggle.setVisibility(!projectToToggle.isVisible());
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
     * Adds an officer to the pending list.
     * @param officer The officer awaiting registration approval.
     */
    public void addPendingOfficer(Officer officer) {
        if (officer != null && !this.pendingOfficerRegistrations.contains(officer)) {
            this.pendingOfficerRegistrations.add(officer);
        }
    }


    /**
     * Updates the status of an officer's registration request.
     * If approved, the officer is removed from pending and potentially added to relevant projects.
     * @param officerToUpdate The Officer whose registration is being processed.
     * @param approve True to approve, false to reject.
     * @return true if the update was processed, false otherwise (e.g., officer not found in pending).
     */
    public boolean updateRegOfficer(Officer officerToUpdate, boolean approve) {
        if (officerToUpdate != null && this.pendingOfficerRegistrations.contains(officerToUpdate)) {
            if (approve) {
                this.pendingOfficerRegistrations.remove(officerToUpdate);
                // Add officer to the projects managed by *this* manager
                // Assuming Project has an addOfficer method or similar
                for (Project project : this.managedProjects) {
                    if (project != null && this.name.equals(project.getManagerName())) {
                         // Example: project.addOfficer(officerToUpdate);
                         // Or use the existing method if it handles duplicates etc.
                        project.updateArrOfOfficers(officerToUpdate); // Using your existing method name
                    }
                }
                 System.out.println("Officer " + officerToUpdate.getName() + " approved and added to relevant projects.");

            } else {
                // Just remove from pending if rejected
                this.pendingOfficerRegistrations.remove(officerToUpdate);
                 System.out.println("Officer " + officerToUpdate.getName() + " registration rejected.");
            }
            // Assuming Officer needs status update, e.g., officerToUpdate.setStatus(approve ? "Approved" : "Rejected");
            return true;
        }
        return false; // Officer not found in pending list
    }

    /**
     * Retrieves a list of approved officers for a specific project managed by this manager.
     * @param project The project whose officers are requested.
     * @return A List of approved Officer objects, or an empty list if none/project invalid.
     */
    public List<Officer> getApprovedOfficers(Project project) {
        if (project != null && this.managedProjects.contains(project) && this.name.equals(project.getManagerName())) {
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
            // Assume Project has hasRoom(type) and updateApplicantAccepted(applicant)
            if (project.hasRoom(applicant.getTypeFlat())) {
                project.updateApplicantAccepted(applicant); // This method should set status to "Successful"
                 System.out.println("Applicant " + applicant.getName() + " accepted.");
                return true;
            } else {
                System.err.println("Error: Not enough room available for applicant " + applicant.getName());
                return false;
            }
        } else {
             // Assume Applicant has setAppStatus
            applicant.setAppStatus("Unsuccessful");
             System.out.println("Applicant " + applicant.getName() + " rejected.");
            return true;
        }
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
        // Check with Divisisha : Remove from project's withdrawal request list
        // applicant.getProject().removeWithdrawalRequest(applicant);
        return true;
    }

    // --- Private Helper Methods for Withdrawal Logic ---

    private void handleAcceptWithdraw(Applicant applicant) {
        Objects.requireNonNull(applicant, "Applicant cannot be null");
         // Assume getAppStatus returns String, getProject exists, removeSuccessful exists, incrementRoom exists
        if ("Successful".equals(applicant.getAppStatus())) {
            Project project = applicant.getProject();
            if (project != null) {
                project.removeSuccessful(applicant); // Remove from allocated list
                project.incrementRoom(applicant.getTypeFlat()); // Free up the room
            }
        }
        applicant.setAppStatus("Withdrawn"); // More descriptive status
        applicant.setWithdrawalStatus(true); // Mark withdrawal processed
    }

    private void handleRejectWithdraw(Applicant applicant) {
         Objects.requireNonNull(applicant, "Applicant cannot be null");
        // Only action is to update the flag
        applicant.setWithdrawalStatus(false); // Mark withdrawal request as not approved
    }


    // --- Reporting ---

    /**
     * Triggers report generation for all projects managed by this manager.
     * (Assumes Project's generateReport handles the actual report creation/output).
     */
    public void generateReport() {
        System.out.println("Generating reports for manager: " + this.name);
        for (Project project : this.managedProjects) {
            if (project != null) {
                System.out.println("Generating report for project: " + project.getName());
                project.generateReport(); // Assume this method does the work
            }
        }
        System.out.println("Report generation complete for manager: " + this.name);
    }

    // toString method for basic Manager info
    @Override
    public String toString() {
        return "Manager [name=" + name + ", managing " + managedProjects.size() + " projects, "
                + pendingOfficerRegistrations.size() + " pending registrations]";
    }
}