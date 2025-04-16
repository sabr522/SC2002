package Actors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Project.Project;

public class Officer extends Applicant {
//    private String citizenApplication;
    private String officerApplication;
    private boolean status;
    private boolean booked = false;
//    private List<String> enquiries = new ArrayList<>();
    private List<Project> appliedProjects = new ArrayList<>();

    public Officer(String name, String NRIC, String password, String maritalStatus, int age,
            String citizenApplication, String officerApplication, boolean status) {
	 super(name, NRIC, password, maritalStatus, age, "Null", "Null");
//	 this.citizenApplication = citizenApplication;
	 this.officerApplication = officerApplication;
	 this.status = status;
}

//    public String getCitizenApplication() {
//        return citizenApplication;
//    }
//
//    public void setCitizenApplication(String citizenApplication) {
//        this.citizenApplication = citizenApplication;
//    }

    public String getOfficerApplication() {
        return officerApplication;
    }

    public void setOfficerApplication(String officerApplication) {
        this.officerApplication = officerApplication;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
    
//	public List<String> getEnquiries() {
//		return enquiries;
//	}
//
//	public void setEnquiries(List<String> enquiries) {
//		this.enquiries = enquiries;
//	}
    
	public void registerProject(String projectName) {
	    for (Project project : Project.getAllProjects()) {
	        if (project.getName().equals(projectName)) {
	            if (this.officerApplication != null && !this.officerApplication.isEmpty()) {
	                System.out.println("Already registered as an officer for another project.");
	                return;
	            }

	            LocalDate now = LocalDate.now();
	            if (now.isBefore(project.getAppOpeningDate()) || now.isAfter(project.getAppClosingDate())) {
	                System.out.println("This project is not open for registration.");
	                return;
	            }

	            project.updateArrOfPendingOfficers(this);
	            this.appliedProjects.add(project);
	            System.out.println("Registered for project as officer (pending approval).");
	            return;
	        }
	    }
	    System.out.println("Project not found.");
	}
    
    public void showProfile() {
        if (status && officerApplication != null) {
            System.out.println("Officer for Project: " + officerApplication);
        } else {
            System.out.println("Officer registration not approved yet.");
        }
    }
    
    public void viewProject() {
        if (officerApplication == null) {
        	System.out.println("You are not handling any project.");
        }

        for (Project project : appliedProjects) {
            if (project.getName().equals(officerApplication)) {
                project.viewAllDetails();
                return;
            }
        }
        System.out.println("Project not found.");
    }
    
    
//    Need enquires
//    public String[] projEnquiries(String projectName) {
//        for (Project project : appliedProjects) {
//            if (project.getName().equals(projectName)) {
//                List<String> collectedEnquiries = new ArrayList<>();
//                for (Applicant applicant : project.getArrOfApplicants()) {
//                    collectedEnquiries.addAll(applicant.viewEnquiries());
//                }
//                return collectedEnquiries.isEmpty()
//                    ? new String[]{"No enquiries found."}
//                    : collectedEnquiries.toArray(new String[0]);
//            }
//        }
//        return new String[]{"Project not found."};
//    }
    
    
    public void successfulApplicants() {
        for (Project project : appliedProjects) {
            if (project.getName().equals(officerApplication)) {
                List<Applicant> successfulList = new ArrayList<>(project.getSuccessfulApplicants());

                for (Applicant applicant : successfulList) {
                    String flatType = applicant.getTypeFlat();
                    boolean booked = false;

                    if (flatType.equals("2-Room") && project.getAvalNo2Room() > 0) {
                    	project.setAvalNo2Room(project.getAvalNo2Room() - 1);
                        booked = true;
                    } else if (flatType.equals("3-Room") && project.getAvalNo3Room() > 0) {
                    	project.setAvalNo3Room(project.getAvalNo3Room() - 1);
                        booked = true;
                    }

                    if (booked) {
                        applicant.setAppStatus("Booked");
                        applicant.setApplied(true);
                        applicant.setProject(project.getName());
                        project.updateBookedApplicants(applicant);
                        System.out.println("Booked applicant: " + applicant.getName() + " (" + applicant.getNRIC() + ")");
                    } else {
                        System.out.println("No available flats of type: " + flatType + " for applicant " + applicant.getName());
                    }
                }
                return;
            }
        }
        System.out.println("You are not assigned to this project.");
    }
    
    public void generateReceipt(String nric) {
        for (Project project : appliedProjects) {
            if (!project.getName().equals(officerApplication)) continue;

            for (Applicant applicant : project.getBookedApplicants()) {
                if (applicant.getNRIC().equals(nric)) {
                    System.out.println("----- RECEIPT -----");
                    System.out.println("Name: " + applicant.getName());
                    System.out.println("NRIC: " + applicant.getNRIC());
                    System.out.println("Age: " + applicant.getAge());
                    System.out.println("Marital Status: " + applicant.getMaritalStatus());
                    System.out.println("Flat Type: " + applicant.getTypeFlat());
                    System.out.println("Project: " + project.getName());
                    System.out.println("-------------------");
                    return;
                }
            }
        }
        System.out.println("No booked applicant found with NRIC: " + nric);
    }
    
    @Override
    public void applyProject(Project project, String chosenFlatType) {
        if (project == null || chosenFlatType == null) {
            System.out.println("Invalid project or flat type.");
            return;
        }

        if (project.getPendingOfficerRegistrations().contains(this)) {
            System.out.println("You are already registered as a pending officer for this project. Cannot apply.");
            return;
        }

        if (this.getOfficerApplication() != null && this.getOfficerApplication().equals(project.getName())) {
            System.out.println("You are already the officer of this project. Cannot apply.");
            return;
        }

        if (this.isApplied()) {
            System.out.println("You have already applied for a project.");
            return;
        }

        if (this.getMaritalStatus().equals("Single") && this.getAge() >= 35) {
            if (!chosenFlatType.equals("2-Room")) {
                System.out.println("Singles (35+) can only apply for 2-Room flats.");
                return;
            }
        } else if (this.getMaritalStatus().equals("Married") && this.getAge() >= 21) {
            if (!chosenFlatType.equals("2-Room") && !chosenFlatType.equals("3-Room")) {
                System.out.println("Invalid flat type. Married applicants can apply for 2-Room or 3-Room flats.");
                return;
            }
        } else {
            System.out.println(this.getName() + " is not eligible to apply for a project.");
            return;
        }

        this.setProject(project.getName());
        this.setTypeFlat(chosenFlatType);
        this.setAppStatus("Pending");
        this.setApplied(true);

        project.updateArrOfApplicants(this);
        System.out.println("You have successfully applied for the " + project.getName() + " project (" + this.getTypeFlat() + " flat).");
    }


}
