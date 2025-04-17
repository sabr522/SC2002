package Actors;

import Actors.Officer; 
import Actors.Manager;
import Services.EnquiryService;
import Project.Project;


import java.util.ArrayList;
import java.util.List;

public class Applicant extends User implements ApplicantRole {
	
    private Project project;
    private String TypeFlat;
    private String appStatus;
    private boolean applied = false;
    private boolean WithdrawStatus = false;
    
    
    public Applicant(String name, String nric, String password, String maritalStatus, int age) {
        super(name, nric, age, maritalStatus, password, "Applicant");
        this.appStatus= "Null";
        this.TypeFlat= "Null";
        
    }


    public String getTypeFlat() {
    	return TypeFlat;
    }
    
    public Project getProject() {
    	return project;
    }
    
    public String getAppStatus(){
        return appStatus;
    }

    public boolean isApplied() { 
    	return applied; 
    }
    
    
    public void setName(String name) { 
    	this.name = name; 
    }
    
    public void setNRIC(String nric) { 
    	this.nric = nric; 
    }
    
    public void setAge(int age) { 
    	this.age = age; 
    }
    
    public void setMaritalStatus(String maritalStatus) { 
    	this.maritalStatus = maritalStatus; 
    }
    
   
    public void setApplied(boolean applied) { 
    	this.applied = applied; 
    }
    
   
    public void setProject(Project project) { 
    	this.project = project; 
    }
    

    public void setTypeFlat(String TypeFlat) {
        if (!TypeFlat.equals("2-Room") && !TypeFlat.equals("3-Room")) 
            throw new IllegalArgumentException("Flat type must be either '2-Room' or '3-Room'");
        
        this.TypeFlat = TypeFlat;
    }
    
    public void setAppStatus(String appStatus) { 
    	this.appStatus = appStatus;
    }

    public void setWithdrawalStatus(boolean WithdrawStatus) {
    	this.WithdrawStatus = WithdrawStatus;
    }

 
    public List<Project> viewAvailProjects() {
    	
        List<Project> allProjects = Project.getAllProjects();            // Fetches all projects
        List<Project> availableProjects = new ArrayList<>();            // Creates a list for avail projects
        
        if(this.applied) {
        	System.out.println("You have already applied for a project. No other projects available.");
        	return availableProjects;
        } 
        
        for (Project proj : allProjects) {
            if (!proj.getVisibility()) continue; // Skip if project is not visible

            if (this.getMaritalStatus().equals("Single") && this.getAge() >= 35) {
                if (proj.getAvalNo2Room() > 0) {
                    availableProjects.add(proj);
                }               
            } 
            
            else if (this.getMaritalStatus().equals("Married") && this.getAge() >= 21) {
                if (proj.getAvalNo2Room() > 0 || proj.getAvalNo3Room() > 0) {
                    availableProjects.add(proj);
                }
             }
         }
      
        return availableProjects;
    }

    public void applyProject(String Projectname, String chosenFlatType) {
    	
    	List<Project> availableProjects = viewAvailProjects();
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
        
        if (!(chosenFlatType.equals("2-Room") || chosenFlatType.equals("3-Room"))) {
            System.out.println("Invalid flat type entered.");
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

        // Assign project and flat type
        this.project = selectedProject;
        this.TypeFlat = chosenFlatType;
        this.appStatus = "Pending";
        selectedProject.updateArrOfApplicants(this);
        this.applied = true;

        System.out.println("You have successfully applied for the " + selectedProject.getName() + " project (" + this.TypeFlat + " flat).");
    }
        
    
    public String viewAppliedProject() {        
    	    if (applied && project != null) {
    	        return "Project: " + project.getName() + ", Application Status: " + this.checkApplicationStatus();
    	    } 
    	    else {
    	        return "No project applied";
    	    }
    }

    
    public String checkApplicationStatus() {
        return appStatus;
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
            if (this.WithdrawStatus) {
                System.out.println(this.getName() + " has already requested a withdrawal.");
                return;
            }
            
            this.WithdrawStatus = true; // flag request
            if (project !=null)           	
                project.updateWithdrawRequests(this);
            
            System.out.println(this.getName() + " has requested to withdraw from the project. Awaiting manager's approval.");
        } 
        else {
            System.out.println("You can only request withdrawal if your application is Successful or Booked.");
        }
    }




public void submitEnquiry(EnquiryService enquiryService, String content, String projectName) {
    
	List<Project> availableProjects = viewAvailProjects(); 
	boolean projectExists = availableProjects.stream()
        .anyMatch(p -> p.getName().equalsIgnoreCase(projectName));

    if (!projectExists) {
        System.out.println("Project '" + projectName + "' is not in your available list.");
        return;
    }

    enquiryService.submitEnquiry(this.getNric(), content, projectName);

    System.out.println("Enquiry submitted.");
}


public void viewEnquiries(EnquiryService enquiryService) {
    List<Enquiry> enquiries = enquiryService.getEnquiryByApplicantNRIC(this.getNric());

    if (enquiries.isEmpty()) {
        System.out.println("You have no enquiries.");
        return;
    }

    for (Enquiry enquiry : enquiries) {
        System.out.println("[" + enquiry.getProject() + "] Enquiry ID: " + enquiry.getId());
        System.out.println("  Content: " + enquiry.getContent());

        List<Reply> replies = enquiry.getReplies();
        if (replies.isEmpty()) {
            System.out.println("  No replies yet.");
        } else {
            for (Reply reply : replies) {
                System.out.println("  Reply: " + reply.getContent());
            }
        }
    }
}


public boolean editEnquiry(EnquiryService enquiryService, int enquiryId, String newContent) {
    return enquiryService.editEnquiry(enquiryId, this.getNric(), newContent);
}

public boolean deleteEnquiry(EnquiryService enquiryService, int enquiryId) {
    return enquiryService.deleteEnquiry(enquiryId, this.getNric());
}

}
