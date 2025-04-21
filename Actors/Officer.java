package Actors;

import Project.Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Officer subclass of Applicant, responsible for managing BTO project operations.
 * Can register for projects, view applicants, book flats, and generate receipts.
 */
public class Officer extends Applicant {

    private Map<Project, String> projectAssignments;
    private Project handledProject;
    private boolean isHandlingApproved;

    /**
     * Constructor for Officer.
     * Calls the protected Applicant constructor to set the role correctly.
     * @param name Officer name
     * @param nric NRIC
     * @param password Password
     * @param maritalStatus Marital status
     * @param age Officer age
     */
    public Officer(String name, String nric, String password, String maritalStatus, int age) {
        // Call protected Applicant constructor, passing the correct role
        super(name, nric, age, maritalStatus, password, "Officer");
        // Initialize Officer-specific fields
        this.handledProject = null;
        this.isHandlingApproved = false;
        this.projectAssignments = new HashMap<>();
    }

    // --- Getters and Setters for Officer state ---

    /** 
     * Gets the Project object this officer is handling or pending approval for. 
     * @return The project this officer is registered to handle
    */
    public Project getHandledProject() {
        return handledProject;
    }

    /** 
     * Gets the Project hashmap Project:Status String this officer is handling or pending approval for. 
     * @return hashmap Project:Status String Officer applied
    */
    public Map<Project, String> getProjectAssignments() {
        return new HashMap<>(this.projectAssignments);
    }

    /**
     * Gets status 
     * @param project project officer is working on
     * @return status in string format
     */
    public String getStatusForProject(Project project) {
        return this.projectAssignments.get(project);
    }

    /**
     * Gets list of projects officer approved
     * @return approved projects
     */
    public List<Project> getApprovedHandledProjects() {
        List<Project> approved = new ArrayList<>();
        for (Map.Entry<Project, String> entry : projectAssignments.entrySet()) {
            if ("Approved".equalsIgnoreCase(entry.getValue())) {
                approved.add(entry.getKey());
            }
        }
        return approved;
    }

    /**
     * Gets a list of projects the officer is *pending* approval for.
     * @return list of projects that is pending status
     */
    public List<Project> getPendingHandledProjects() {
        List<Project> pending = new ArrayList<>();
       for (Map.Entry<Project, String> entry : projectAssignments.entrySet()) {
           if ("Pending".equalsIgnoreCase(entry.getValue())) {
               pending.add(entry.getKey());
           }
       }
       return pending;
   }

   
    /**
     * Updates or adds an assignment for the officer.
     * Called by DataManager during load or Manager during approval/rejection.
     * @param project The project being assigned/updated.
     * @param status "Pending" or "Approved". If null, removes the assignment.
     */
    public void updateProjectAssignment(Project project, String status) {
        if (project == null) return;
        if (status == null || status.trim().isEmpty()) {
            this.projectAssignments.remove(project); // Remove assignment if status is null/empty
        } else if ("Pending".equalsIgnoreCase(status.trim()) || "Approved".equalsIgnoreCase(status.trim())) {
             // Allow setting Pending or Approved status
             this.projectAssignments.put(project, status.trim());
        } else {
             System.err.println("WARN: Invalid status '" + status + "' provided for officer assignment update.");
        }
    }


    /**
     * Sets the Project this officer will handle (called by Manager or DataManager).
     * @param project Project reference
     */
    public void setHandledProject(Project project) {
        this.handledProject = project;
    }

    /** 
     * Gets the approval status for handling the assigned project. 
     *  @return True if the officer has been approved
     */
    public boolean isHandlingApproved() {
        return isHandlingApproved;
    }

    /** 
     * Sets the approval status (called by Manager or DataManager). 
     	* @param approved Boolean flag
     */
    public void setHandlingApproved(boolean approved) {
        this.isHandlingApproved = approved;
    }

    // --- Officer Actions ---

    /**
     * Allows an Officer to register interest in handling a specific project.
     * Adds the officer to the project's pending list after eligibility checks.
     * Uses the corrected super constructor, so getRole() should be "Officer".
     * @param projectName Name of the project to register for
	 * @param allProjectsMap Map of all project names to project objects
	 * @param allUsersMap Map of all NRICs to user objects
     */
    public void registerProject(String projectName, Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        if (projectName == null || projectName.trim().isEmpty()) { System.out.println("Project name cannot be empty."); return; }
        Project projectToRegister = allProjectsMap.get(projectName.trim());
        if (projectToRegister == null) { System.out.println("Project '" + projectName + "' not found."); return; }

        // 2. Check if applied as Applicant for THIS project
        if (super.isApplied() && projectToRegister.equals(super.getProject())) {
             System.out.println("You cannot register as an Officer for a project you have applied to as an Applicant."); return;
        }
        java.time.format.DateTimeFormatter displayFormat = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String newProjectDates = "(Dates N/A)";
         if (projectToRegister.getAppOpeningDate() != null && projectToRegister.getAppClosingDate() != null) {
              newProjectDates = "(" + projectToRegister.getAppOpeningDate().format(displayFormat) + " to " + projectToRegister.getAppClosingDate().format(displayFormat) + ")";
         }

        // 3. Check for date clashes with *all* existing assignments (Pending or Approved)
        for (Map.Entry<Project, String> entry : this.projectAssignments.entrySet()) {
            Project assignedProject = entry.getKey();
            if (assignedProject != null && projectToRegister.isClashing(assignedProject.getAppOpeningDate(), assignedProject.getAppClosingDate())) {
                System.out.println("Error: Cannot register for '" + projectToRegister.getName() + "' because its application period overlaps with your existing assignment for '" + assignedProject.getName() + "' (Status: " + entry.getValue() + ").");
                return; // Disallow registration due to overlap
            }
        }

        // 4. Check if already pending/approved for THIS project (should be covered by #3 check if dates are same, but good explicit check)
        if(this.projectAssignments.containsKey(projectToRegister)){
             System.out.println("You are already registered ("+ this.projectAssignments.get(projectToRegister) +") for project '" + projectToRegister.getName() + "'.");
             return;
        }


        // All checks passed, add to project's pending list AND officer's assignment map
        if (projectToRegister.updateArrOfPendingOfficers(this)) {
            this.updateProjectAssignment(projectToRegister, "Pending"); // Add to officer's map as Pending
            System.out.println("Successfully registered interest for project '" + projectToRegister.getName() + "' " + newProjectDates + ". Awaiting Manager approval.");
            } else {
            System.out.println("Failed to register interest for project '" + projectToRegister.getName() + "'. You might already be on the project's pending list.");
            // Ensure officer's internal state doesn't have it if project add failed
            this.projectAssignments.remove(projectToRegister);
        }
    }

    /**
     * Displays the Officer's profile information, including all project assignments
     * (pending or approved) with their application periods.
     */
    public void showProfile() {
        System.out.println("\n--- Officer Profile ---");
        System.out.println("Name: " + getName());
        System.out.println("NRIC: " + getNric());
        System.out.println("Role: " + getRole());

        // Use the projectAssignments map now
        if (projectAssignments.isEmpty()) {
            System.out.println("Assignments: Not assigned to handle or pending for any project.");
        } else {
            System.out.println("Project Assignments:");
            for (Map.Entry<Project, String> entry : projectAssignments.entrySet()) {
                Project p = entry.getKey();
                String status = entry.getValue(); // "Pending" or "Approved"
                String projectName = (p != null) ? p.getName() : "Unknown Project";
                String dates = "(Dates N/A)";
                if (p != null && p.getAppOpeningDate() != null && p.getAppClosingDate() != null) {
                     // Define a standard date format for display
                     java.time.format.DateTimeFormatter displayFormat = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                     dates = "(" + p.getAppOpeningDate().format(displayFormat) + " to " + p.getAppClosingDate().format(displayFormat) + ")";
                }

                System.out.println(" - Project: " + projectName);
                System.out.println("   Status: " + status);
                System.out.println("   Period: " + dates);
            }
        }
        System.out.println("-----------------------");
    }

    /**
     * Displays details of the project the officer is approved to handle.
     * @param project the officer is holding
     */
    public void viewProject(Project projectToShow) {
        if (projectToShow == null || !this.projectAssignments.containsKey(projectToShow)) {
            System.out.println("You are not assigned to the specified project or project is invalid.");
            return;
        }
        String status = this.projectAssignments.get(projectToShow);
        System.out.println("\n--- Details for Project: " + projectToShow.getName() + " (Your Status: " + status + ") ---");
        // Officers (staff) see full details
        projectToShow.viewAllDetails(true);
    }

    /**
     * Retrieves the list of applicants who are in 'Successful' state
     * for the project this officer is handling.
     * @return A List of bookable Applicant objects, or an empty list/null if none or not applicable.
     */
    public List<Applicant> getBookableApplicants(Project projectToBookIn) { 
        if (projectToBookIn == null || !"Approved".equalsIgnoreCase(this.projectAssignments.get(projectToBookIn))) {
            System.out.println("You must be an approved officer for the specified project to book flats.");
            return new ArrayList<>();
         }
    
        List<Applicant> successfulList = this.handledProject.getSuccessfulApplicants(); // Assumes Project getter exists
    
        if (successfulList == null || successfulList.isEmpty()) {
            return new ArrayList<>();
        }
    
        // Filter for those *actually* still in Successful state (not yet Booked)
        List<Applicant> trulyBookable = new ArrayList<>();
        for (Applicant app : successfulList) {
            if (app != null && "Successful".equals(app.getAppStatus())) {
                trulyBookable.add(app);
            }
        }
        return trulyBookable;
    }
    /**
     * Attempts to book a flat for the specified applicant in the specified project.
     * Performs final availability checks and updates states if successful.
     * The calling context (OfficerCLI) must ensure the officer is approved for this project.
     *
     * @param applicantToBook The Applicant object selected for booking.
     * @param projectContext The Project in which the booking should occur.
     * @return true if booking was successful, false otherwise.
     */
    public boolean bookFlatForApplicant(Applicant applicantToBook, Project projectContext) { // Added projectContext parameter

        // 1. Validate Inputs
        if (projectContext == null) {
             System.out.println("Error: Project context cannot be null for booking.");
             return false;
        }
         // Check if officer is actually approved for this specific project context
         String officerStatusForProject = this.projectAssignments.get(projectContext);
         if (!"Approved".equalsIgnoreCase(officerStatusForProject)) {
              System.out.println("Error: You are not the approved Officer for project '" + projectContext.getName() + "'. Cannot book flat.");
              return false;
         }
        if (applicantToBook == null || !"Successful".equals(applicantToBook.getAppStatus())) {
            System.out.println("Error: Invalid applicant or applicant status is not 'Successful'.");
            return false;
        }
        // Ensure the applicant belongs to the provided project context
        if (!projectContext.equals(applicantToBook.getProject())) {
            System.out.println("Error: Applicant does not belong to the specified project '" + projectContext.getName() + "'.");
            return false;
        }

        // 2. Check final availability in the project context
        String flatTypeToBook = applicantToBook.getTypeFlat();
        boolean roomAvailable = false;
        int currentAvail = 0; // To show remaining units
        if (flatTypeToBook != null) { // Null check for flat type
            if ("2-Room".equalsIgnoreCase(flatTypeToBook) && projectContext.getAvalNo2Room() > 0) {
                roomAvailable = true;
                currentAvail = projectContext.getAvalNo2Room();
            } else if ("3-Room".equalsIgnoreCase(flatTypeToBook) && projectContext.getAvalNo3Room() > 0) {
                roomAvailable = true;
                 currentAvail = projectContext.getAvalNo3Room();
            }
        } else {
             System.out.println("Error: Applicant has no flat type selected. Cannot check availability.");
             return false; // Cannot proceed without flat type
        }


        // 3. Perform Booking if room available
        if (roomAvailable) {
            // Update applicant state
            applicantToBook.setAppStatus("Booked");

            // Delegate project state update (moving lists, decrementing count)
            boolean bookingUpdateSuccess = projectContext.updateBookedApplicants(applicantToBook); // Use projectContext

            if (bookingUpdateSuccess) {
                System.out.println("Successfully booked a " + flatTypeToBook + " flat for " + applicantToBook.getName() + " in project '" + projectContext.getName() + "'.");
                // Get updated count AFTER project update
                int remainingUnits = ("2-Room".equalsIgnoreCase(flatTypeToBook) ? projectContext.getAvalNo2Room() : projectContext.getAvalNo3Room());
                System.out.println("Remaining " + flatTypeToBook + " units in this project: " + remainingUnits);
                return true; // Booking succeeded
            } else {
                System.out.println("Error updating project data during booking. Rolling back applicant status.");
                applicantToBook.setAppStatus("Successful"); // Rollback status
                return false; // Booking failed
            }
        } else {
            System.out.println("Booking failed. No available " + flatTypeToBook + " units remaining for project '" + projectContext.getName() + "'.");
            return false; // Booking failed
        }
    }

    /**
     * Generates a receipt for a booked applicant within the handled project.
     * @param nric NRIC of applicant
     */
    public void generateReceipt(Project projectToGenerateIn, String nric) {
        if (projectToGenerateIn == null || !"Approved".equalsIgnoreCase(this.projectAssignments.get(projectToGenerateIn))) {
            System.out.println("You must be an approved officer for the specified project to generate receipts.");
            return;
         }
        if (nric == null || nric.trim().isEmpty()){
             System.out.println("Applicant NRIC cannot be empty.");
             return;
        }

        // Find applicant in the project's booked list
        List<Applicant> bookedList = projectToGenerateIn.getBookedApplicants(); 
        Applicant bookedApplicant = null;
        if (bookedList != null) {
            for (Applicant app : bookedList) {
                if (app != null && nric.trim().equals(app.getNric())) {
                    bookedApplicant = app;
                    break;
                }
            }
        }

        if (bookedApplicant != null) {
            System.out.println("\n----- Booking Receipt -----");
            System.out.println("Project: " + projectToGenerateIn.getName());
            System.out.println("Applicant Name: " + bookedApplicant.getName());
            System.out.println("Applicant NRIC: " + bookedApplicant.getNric());
            System.out.println("Age: " + bookedApplicant.getAge());
            System.out.println("Marital Status: " + bookedApplicant.getMaritalStatus());
            System.out.println("Booked Flat Type: " + bookedApplicant.getTypeFlat());
            System.out.println("-------------------------");
        } else {
            System.out.println("No booked applicant found with NRIC '" + nric + "' for project '" + this.handledProject.getName() + "'.");
        }
    }

    /**
     * Override applyProject inherited from Applicant.
     * Officers can apply for projects they are NOT handling, subject to applicant rules
     * AND officer rules (not handling overlapping project).
     */
    @Override
    public void applyProject(List<Project> availableProjects, String projectName, String chosenFlatType) {
        System.out.println("Officer attempting to apply as Applicant...");

        if (projectName == null || chosenFlatType == null || projectName.trim().isEmpty() || chosenFlatType.trim().isEmpty()) {
            System.out.println("Project name and flat type must be provided."); return;
        }

        Project selectedProject = null;
        for (Project proj : availableProjects) { // Search only available projects
            if (proj != null && proj.getName().equalsIgnoreCase(projectName.trim())) {
                selectedProject = proj; break;
            }
        }

        if (selectedProject == null) {
            System.out.println("Project '" + projectName + "' not found in your list of available projects."); return;
        }

        // --- OFFICER SPECIFIC CHECKS ---
        // 1. Cannot apply if currently handling/pending ANY project (even a different one)
        for (Map.Entry<Project, String> entry : this.projectAssignments.entrySet()) {
            Project assignedProject = entry.getKey();
            if (assignedProject != null && selectedProject.isClashing(assignedProject.getAppOpeningDate(), assignedProject.getAppClosingDate())) {
                 System.out.println("Error: As an Officer assigned/pending for '" + assignedProject.getName() + "', you cannot apply for project '" + selectedProject.getName() + "' due to overlapping application periods.");
                 return;
            }
       }

        // Check availability of chosen flat type
        if (chosenFlatType.equals("2-Room") && selectedProject.getAvalNo2Room() == 0) {
            System.out.println("This project does not have available 2-Room flats."); return;
        }
        if (chosenFlatType.equals("3-Room") && selectedProject.getAvalNo3Room() == 0) {
            System.out.println("This project does not have available 3-Room flats."); return;
        }

        // Check eligibility for chosen flat type
        boolean eligible = false;
        if (this.getMaritalStatus().equals("Single") && this.getAge() >= 35) {
            if (chosenFlatType.equals("2-Room")) eligible = true;
            else { System.out.println("Singles (35+) can only apply for 2-Room flats."); return; }
        } else if (this.getMaritalStatus().equals("Married") && this.getAge() >= 21) {
            if (chosenFlatType.equals("2-Room") || chosenFlatType.equals("3-Room")) eligible = true;
            else { System.out.println("Married applicants can apply for 2-Room or 3-Room flats only."); return; }
        } else {
            System.out.println(this.getName() + " (Officer) is not eligible to apply based on age/marital status."); return;
        }

        if (!eligible) {
             System.out.println("You are not eligible for the chosen flat type."); return;
        }


        // If all checks pass, proceed with updating the Applicant part of the Officer
        System.out.println("Applying as Applicant...");
        super.setTypeFlat(chosenFlatType);
        super.setProject(selectedProject);
        super.setAppStatus("Pending");
        super.setApplied(true);
        selectedProject.updateArrOfApplicants(this); // Add self to project's applicant list

        System.out.println("You (Officer " + getName() + ") have successfully applied for the " + selectedProject.getName() + " project (" + super.getTypeFlat() + " flat).");
    }
}
