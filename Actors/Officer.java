package Actors;

import Project.Project;
import java.time.LocalDate; // Added for date check
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects; 
import java.util.Scanner;

/**
 * Officer subclass of Applicant, responsible for managing BTO project operations.
 * Can register for projects, view applicants, book flats, and generate receipts.
 */
public class Officer extends Applicant {

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
        if (projectName == null || projectName.trim().isEmpty()) {
            System.out.println("Project name cannot be empty.");
            return;
        }
        Project projectToRegister = allProjectsMap.get(projectName.trim());

        if (projectToRegister == null) {
            System.out.println("Project '" + projectName + "' not found.");
            return;
        }

        // 1. Check if already handling/pending another project
        if (this.handledProject != null) {
            System.out.println("You are already handling or pending approval for project '" + this.handledProject.getName() + "'. Cannot register for another.");
            return;
        }

        // 3. Check if applied as Applicant for THIS project
        // Access Applicant fields directly due to inheritance
        if (super.isApplied() && projectToRegister.equals(super.getProject())) {
             System.out.println("You cannot register as an Officer for a project you have applied to as an Applicant.");
             return;
        }

        // 4. Check if officer is registered (pending/approved) for another project within the same application period
        for(Project otherProject : allProjectsMap.values()){
            if (otherProject == null || otherProject.equals(projectToRegister)) continue; // Skip self and nulls

            // Check if the periods overlap
             if (projectToRegister.isClashing(otherProject.getAppOpeningDate(), otherProject.getAppClosingDate())) {
                 // Check if officer is pending or approved in the overlapping project
                  if ((otherProject.getPendingOfficerRegistrations() != null && otherProject.getPendingOfficerRegistrations().contains(this)) ||
                      (otherProject.getArrOfOfficers() != null && otherProject.getArrOfOfficers().contains(this)))
                  {
                      System.out.println("You are already registered (pending or approved) for project '" + otherProject.getName() + "' which has an overlapping application period.");
                      System.out.println("Cannot register for '" + projectName + "'.");
                      return;
                  }
             }
        }


        // All checks passed, add to pending list
        if (projectToRegister.updateArrOfPendingOfficers(this)) {
             // Link the project, but keep status as not approved
             this.setHandledProject(projectToRegister);
             this.setHandlingApproved(false);
             System.out.println("Successfully registered interest for project '" + projectToRegister.getName() + "'. Awaiting Manager approval.");
        } else {
             System.out.println("Failed to register interest for project '" + projectToRegister.getName() + "'. You might already be on the pending list.");
        }
    }

    /**
     * Displays the Officer's profile information.
     */
    public void showProfile() { 
        System.out.println("\n--- Officer Profile ---");
        System.out.println("Name: " + getName());
        System.out.println("NRIC: " + getNric());
        System.out.println("Role: " + getRole()); 
        if (isHandlingApproved && handledProject != null) {
            System.out.println("Status: Approved Officer");
            System.out.println("Handling Project: " + handledProject.getName());
        } else if (handledProject != null) { 
            System.out.println("Status: Pending Approval");
            System.out.println("Pending for Project: " + handledProject.getName());
        } else {
            System.out.println("Status: Not assigned to handle any project.");
        }
        System.out.println("-----------------------");
    }

    /**
     * Displays details of the project the officer is approved to handle.
     */
    public void viewProject() { 
        if (isHandlingApproved && handledProject != null) {
            System.out.println("\n--- Details for Handled Project ---");
            handledProject.viewAllDetails(true);
        } else if (handledProject != null) {
            System.out.println("Your registration for project '" + handledProject.getName() + "' is still pending approval.");
        } else {
            System.out.println("You are not currently approved to handle any project.");
        }
    }

    /**
     * Retrieves the list of applicants who are in 'Successful' state
     * for the project this officer is handling.
     * @return A List of bookable Applicant objects, or an empty list/null if none or not applicable.
     */
    public List<Applicant> getBookableApplicants() { 
        if (!this.isHandlingApproved || this.handledProject == null) {
            return new ArrayList<>(); // Return empty list
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
     * Attempts to book a flat for the specified applicant in the handled project.
     * Performs final availability checks and updates states if successful.
     * @param applicantToBook The Applicant object selected for booking.
     * @return true if booking was successful, false otherwise.
     */
    public boolean bookFlatForApplicant(Applicant applicantToBook) {
        if (!this.isHandlingApproved || this.handledProject == null) {
            System.out.println("Error: Officer not approved or not handling a project.");
            return false;
        }
        if (applicantToBook == null || !applicantToBook.getAppStatus().equals("Successful")) {
            System.out.println("Error: Invalid applicant or applicant status is not 'Successful'.");
            return false;
        }
        // Ensure the applicant belongs to the project the officer is handling
        if (!this.handledProject.equals(applicantToBook.getProject())) {
            System.out.println("Error: Applicant does not belong to the project you are handling.");
            return false;
        }

        String flatTypeToBook = applicantToBook.getTypeFlat();

        // Check final availability in the project
        boolean roomAvailable = false;
        if ("2-Room".equalsIgnoreCase(flatTypeToBook) && this.handledProject.getAvalNo2Room() > 0) {
            roomAvailable = true;
        } else if ("3-Room".equalsIgnoreCase(flatTypeToBook) && this.handledProject.getAvalNo3Room() > 0) {
            roomAvailable = true;
        }

        if (roomAvailable) {
            // Update applicant state
            applicantToBook.setAppStatus("Booked");

            // Delegate project state update (moving lists, decrementing count)
            boolean bookingUpdateSuccess = this.handledProject.updateBookedApplicants(applicantToBook);

            if (bookingUpdateSuccess) {
                System.out.println("Successfully booked a " + flatTypeToBook + " flat for " + applicantToBook.getName() + ".");
                System.out.println("Remaining " + flatTypeToBook + " units: " + ("2-Room".equalsIgnoreCase(flatTypeToBook) ? this.handledProject.getAvalNo2Room() : this.handledProject.getAvalNo3Room()));
                return true; // Booking succeeded
            } else {
                System.out.println("Error updating project data during booking. Rolling back applicant status.");
                applicantToBook.setAppStatus("Successful"); // Rollback status
                return false; // Booking failed
            }
        } else {
            System.out.println("Booking failed. No available " + flatTypeToBook + " units remaining for project '" + this.handledProject.getName() + "'.");
            return false; // Booking failed
        }
    }

    /**
     * Generates a receipt for a booked applicant within the handled project.
     * @param nric NRIC of applicant
     */
    public void generateReceipt(String nric) {
        if (!this.isHandlingApproved || this.handledProject == null) {
            System.out.println("You must be approved and assigned to a project to generate receipts.");
            return;
        }
        if (nric == null || nric.trim().isEmpty()){
             System.out.println("Applicant NRIC cannot be empty.");
             return;
        }

        // Find applicant in the project's booked list
        List<Applicant> bookedList = this.handledProject.getBookedApplicants(); // Assumes getter exists
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
            System.out.println("Project: " + this.handledProject.getName());
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
        if (this.handledProject != null) {
             System.out.println("Error: As an Officer handling or pending approval for '" + this.handledProject.getName() + "', you cannot apply for other projects as an applicant.");
             return;
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
