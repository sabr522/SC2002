package Actors;


import Project.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Applicant extends User implements ApplicantRole {
	
    private Project project;
    private String typeFlat;
    private String appStatus;
    private boolean applied = false;
    private boolean withdrawStatus = false;

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
    }
    /**
     * Public constructor specifically for creating Applicant instances.
     * Calls the protected constructor with the role "Applicant".
     */
    public Applicant(String name, String nric, String password, String maritalStatus, int age) {
        this(name, nric, age, maritalStatus, password, "Applicant"); 
    }
    
    public String getTypeFlat() {
    	return typeFlat;
    }
    
    public Project getProject() {
    	return project;
    }
    
    public String getAppStatus() {
    	return appStatus;
    }

    public boolean getWithdrawalStatus() {
        return withdrawStatus;
    }
    
    public boolean isApplied() { 
    	return applied; 
    }
    
    public String checkApplicationStatus() {
        return appStatus;
    }
    
    public void setApplied(boolean applied) { 
    	this.applied = applied; 
    }
    
    public void setProject(Project project) { 
    	if(project != null)
    	   this.project = project; 
    }
    
    public void setWithdrawalStatus(boolean withdrawStatus) {
    	this.withdrawStatus = withdrawStatus;
    }
    

    public void setTypeFlat(String typeFlat) {
        if (!typeFlat.equals("2-Room") && !typeFlat.equals("3-Room")) 
            throw new IllegalArgumentException("Flat type must be either '2-Room' or '3-Room'");
        
        this.typeFlat = typeFlat;
    }
    
   
    public void setAppStatus(String appStatus) {
        List<String> validStatuses = Arrays.asList("Pending", "Unsuccessful", "Successful", "Booked");
        if (!validStatuses.contains(appStatus)) {
            throw new IllegalArgumentException("Invalid status: " + appStatus);
        }
        this.appStatus = appStatus;
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


    public void bookFlat() {
        if (this.appStatus.equals("Successful")) {
            project.updateSuccessfulApplicants(this);
            System.out.println(this.getName() + " wants to book a flat. Awaiting officer's approval.");
        }
        else {
        	System.out.println(this.getName() + " cannot book a flat. Application status is not 'Successful'.");
        }
        
    }

    
    public void withdrawApp() {
        if (this.applied && (this.appStatus.equals("Successful") || this.appStatus.equals("Booked"))) {
            if (this.withdrawStatus) {
                System.out.println(this.getName() + " has already requested a withdrawal.");
                return;
            }
            

            if (project !=null)           	
                project.updateWithdrawRequests(this);
            
            System.out.println(this.getName() + " has requested to withdraw from the project. Awaiting manager's approval.");
        } 
        else {
            System.out.println("You can only request withdrawal if your application is Successful or Booked.");
        }
    }

}
