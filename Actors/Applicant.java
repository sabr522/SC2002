package Actors;


import Project.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents an applicant user who can apply for BTO projects, view and manage their application status,
 * book flats, and request withdrawals.
 */
public class Applicant extends User implements ApplicantRole {
	
    private Project project;
    private String typeFlat;
    private String appStatus;
    private boolean applied = false;
    private boolean withdrawStatus = false;
    private String preferredNeighbourhood;

    /**
     * Protected constructor for subclasses (like Officer) to pass the correct role up.
     */
    protected Applicant(String name, String nric, int age, String maritalStatus, String password, String role) {
        super(name, nric, age, maritalStatus, password, role); // Pass the role up
        // Initialize fields to default states
        this.project = null;
        this.appStatus = null; 
        this.typeFlat = null; 
        this.applied = false;
        this.withdrawStatus = false;
        this.preferredNeighbourhood = null;
    }
    /**
     * Public constructor specifically for creating Applicant instances.
     * Calls the protected constructor with the role "Applicant".
     * @param name Name of the applicant
     * @param nric NRIC of the applicant
     * @param password Password
     * @param maritalStatus Marital status
     * @param age Age of the applicant
     */
    public Applicant(String name, String nric, String password, String maritalStatus, int age) {
        this(name, nric, age, maritalStatus, password, "Applicant"); 
        this.preferredNeighbourhood = null;
    }
    
    /** Returns the flat type chosen by the applicant. 
     * @return The selected flat type (2-Room or 3-Room) */
    public String getTypeFlat() {
    	return typeFlat;
    }
    
    /** 
     * Returns the project the applicant is in.
     * @return The project that the applicant is in */
    public Project getProject() {
    	return project;
    }
    /**
     * @return The neighbourhood applicant prefers
     */
    public String getPreferredNeighbourhood() {
        return preferredNeighbourhood;
    }
    
    /** 
     * Returns the current application status.
     * @return The current application status */
    public String getAppStatus() {
    	return appStatus;
    }

    /** 
     * Returns whether a withdrawal has been requested.
     * @return true if a withdrawal has been requested */
    public boolean getWithdrawalStatus() {
        return withdrawStatus;
    }
    
    /** 
     * Checks whether the applicant has applied for a project.
     * @return true if applicant has applied to a project */
    public boolean isApplied() { 
    	return applied; 
    }
    
    /** 
     * Returns the current application status.
     * @return application status */
    public String checkApplicationStatus() {
        return appStatus;
    }
    
    /**
     * Updates the internal flag that tracks application state.
     * @param applied true if the applicant has applied
     */
    public void setApplied(boolean applied) { 
    	this.applied = applied; 
    }
    /**
     * Set applicant's preferred neighbourhood for application.
     * @param neighbourhood Applicant's desired neighbourhood name
     */
    public void setPreferredNeighbourhood(String neighbourhood) { 
        this.preferredNeighbourhood = (neighbourhood == null || neighbourhood.trim().isEmpty()) ? null : neighbourhood.trim();
    }

    /**
     * Sets the project that the applicant is in.
     * @param project The project object
     */
    public void setProject(Project project) { 
    	if(project != null)
    	   this.project = project; 
    }

    /**
     * Flags whether the applicant has requested withdrawal.
     * @param withdrawStatus true if withdrawal is requested
     */
    public void setWithdrawalStatus(boolean withdrawStatus) {
    	this.withdrawStatus = withdrawStatus;
    }
    

     /**
     * Sets the flat type that the applicant is applying for.
     * @param typeFlat Must be either "2-Room" or "3-Room"
     */
    public void setTypeFlat(String typeFlat) {
        if (!typeFlat.equals("2-Room") && !typeFlat.equals("3-Room")) 
            throw new IllegalArgumentException("Flat type must be either '2-Room' or '3-Room'");
        
        this.typeFlat = typeFlat;
    }
    
   
    /**
     * Sets the application status. Validates against allowed statuses.
     * If the status is set to "Withdrawn", resets other application-specific fields
     * on the Applicant object to allow re-application.
     * @param appStatus The new application status.
     */
    public void setAppStatus(String appStatus) {
        if (appStatus == null) {
            this.appStatus = null;
            setApplied(false); 
            setProject(null); 
            this.typeFlat = null;
            setWithdrawalStatus(false);
            return;
        }

        List<String> validStatuses = Arrays.asList("Pending", "Unsuccessful", "Successful", "Booked", "Withdrawn");

        if (!validStatuses.contains(appStatus)) {
            throw new IllegalArgumentException("Invalid application status provided: " + appStatus);
        }

        // Set the new status
        this.appStatus = appStatus;

        // If the status is set to Withdrawn, reset other relevant applicant fields
        if ("Withdrawn".equals(this.appStatus)) {
            this.setApplied(false);         // Allow applying again
            this.setProject(null);          // Disassociate from the withdrawn project object
            this.typeFlat = null;                   // Clear flat type
            this.setWithdrawalStatus(false); // Withdrawal process is complete
        }
    }
    
    /**
     * Gets a list of projects that are visible to this applicant based on
     * project visibility settings and basic applicant eligibility (age, marital status),
     * regardless of whether the applicant has already applied for a project.
     * This is primarily intended for contexts like selecting a project to enquire about.
     *
     * @param allProjectsMap Map of all projects in the system.
     * @return A List of Project objects the applicant can view.
     */
    public List<Project> getProjectsVisibleForEnquiry(Map<String, Project> allProjectsMap) {
        List<Project> visibleProjects = new ArrayList<>();
        if (allProjectsMap == null) return visibleProjects;

        for (Project proj : allProjectsMap.values()) {
            if (proj == null || !proj.getVisibility()) continue; 

            boolean canPotentiallyApply = false;
            if (this.getMaritalStatus().equals("Single") && this.getAge() >= 35) {
                canPotentiallyApply = true;
            } else if (this.getMaritalStatus().equals("Married") && this.getAge() >= 21) {
                canPotentiallyApply = true; 
            }

            if (canPotentiallyApply) { 
                visibleProjects.add(proj);
            }
        }
        return visibleProjects;
    }

    /**
     * Returns a list of projects this applicant is eligible to apply for.
     * @param allProjectsMap All projects in system
     * @return List of eligible and available projects
     */
    public List<Project> viewAvailProjects(Map<String, Project> allProjectsMap) {
        List<Project> availableProjects = new ArrayList<>();   

        if (this.applied) {
            System.out.println("You have already applied for a project. No other projects available.");
            return availableProjects;
        }

        for (Project proj : allProjectsMap.values()) {
            if (!proj.getVisibility()) continue;     

            if (this.getMaritalStatus().equals("Single") && this.getAge() >= 35) {
                if (proj.getAvalNo2Room() > 0) {
                    availableProjects.add(proj);
                }
            } else if (this.getMaritalStatus().equals("Married") && this.getAge() >= 21) {
                if (proj.getAvalNo2Room() > 0 || proj.getAvalNo3Room() > 0) {
                    availableProjects.add(proj);
                }
            }
        }

        return availableProjects;
    }

    /**
     * Applies to a selected project and flat type after validating eligibility and availability.
     * @param availableProjects List of available projects
     * @param Projectname Name of project to apply to
     * @param chosenFlatType Desired flat type
     */
    public void applyProject(List<Project> availableProjects, String Projectname, String chosenFlatType) {
    	
    	if (this.applied) {
    		System.out.println("You have already applied for a project.");
            return;
    }
        if (Projectname == null || chosenFlatType == null) {
            System.out.println("Project name or flat type not provided.");
            return;
        }
        
        
        Project selectedProject = null;     //find project by name from the list
        
        for (Project proj : availableProjects) {
            if (proj.getName().equalsIgnoreCase(Projectname)) {
                selectedProject = proj;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Project not found in the available list.");
            return;
        }
        
        
        // Check if project has the chosen flat type
        
        if (chosenFlatType.equals("2-Room") && selectedProject.getAvalNo2Room() == 0) {
            System.out.println("This project does not offer any 2-Room flats.");
            return;
        }

        if (chosenFlatType.equals("3-Room") && selectedProject.getAvalNo3Room() == 0) {
            System.out.println("This project does not offer any 3-Room flats.");
            return;
        }
        
        // Check eligibility 
        
        if (this.getMaritalStatus().equals("Single") && this.getAge() >= 35) {
            if (!chosenFlatType.equals("2-Room")) {
                System.out.println("Singles (35+) can only apply for 2-Room flats.");
                return;
            }
        } 
        else if (this.getMaritalStatus().equals("Married") && this.getAge()>= 21) {
            if (!chosenFlatType.equals("2-Room") && !chosenFlatType.equals("3-Room")) {
                System.out.println("Married applicants can apply for 2-Room or 3-Room flats.");
                return;
            }
        } 
        else {
            System.out.println(this.getName() + " is not eligible to apply for a project.");
            return;
        }

        // update
        
        setTypeFlat(chosenFlatType);  // sets & handles the validation
        setProject(selectedProject);  // Set the selected project
        setAppStatus("Pending");      // Set to default application status 
        setApplied(true);             // Mark the applicant as having applied

        selectedProject.updateArrOfApplicants(this); 
        
        System.out.println("You have successfully applied for the " + selectedProject.getName() + " project (" + this.typeFlat + " flat).");
    }
        
     /**
     * Displays and returns details of the project the applicant has applied to.
     * @return Status summary string
     */   
    public String viewAppliedProject() {
        if (applied && project != null) {
            System.out.println("\n--- Details of Your Applied Project ---");
            try {
                project.viewAllDetails(false); 
    
                return "-------------------------------------\nApplication Status: " + this.checkApplicationStatus(); // checkApplicationStatus() just returns appStatus field
    
            } catch (Exception e) {
                System.err.println("Error displaying applied project details: " + e.getMessage());
                // Still return status if available, but acknowledge error
                return "Error displaying project details. Status: " + this.checkApplicationStatus();
            }
        }
        else {
            return "You have not applied for any project.";
        }
    }

    /**
     * Initiates booking request if the applicant status is 'Successful'.
     */
    public void bookFlat() {
        if (this.appStatus.equals("Successful")) {
            project.updateSuccessfulApplicants(this);
            System.out.println(this.getName() + " wants to book a flat. Awaiting officer's approval.");
        }
        else {
        	System.out.println(this.getName() + " cannot book a flat. Application status is not 'Successful'.");
        }
        
    }

    /**
     * Submits a withdrawal request if status is 'Successful' or 'Booked'.
     */
    public void withdrawApp() {
        // Check if eligible to withdraw (Applied AND Successful/Booked)
        if (!this.applied || !("Successful".equals(this.appStatus) || "Booked".equals(this.appStatus))) {
            System.out.println("You can only request withdrawal if your application is Successful or Booked.");
            if (this.applied) System.out.println("Your current status: " + (appStatus != null ? appStatus : "N/A"));
            else System.out.println("You have not applied for a project.");
            return;
        }

        // Check if already requested
        if (this.withdrawStatus) {
            System.out.println("You have already submitted a withdrawal request for project '" + (project != null ? project.getName() : "Unknown") + "'. Awaiting manager's decision.");
            return;
        }

        // Check if project link exists
        if (project == null) {
            System.out.println("Error: Cannot request withdrawal - project link missing.");
            return;
        }

        // Update project's withdrawal request list
        if (project.updateWithdrawRequests(this)) {
            this.setWithdrawalStatus(true); 
            System.out.println("Withdrawal requested for project '" + project.getName() + "'. Awaiting manager's approval.");
        } else {
            // This might happen if updateWithdrawRequests fails (e.g., applicant not found in source lists)
            System.out.println("Failed to submit withdrawal request to the project. Please check your application status.");
        }
            }

}
