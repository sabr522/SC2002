package Actors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Project.Project;

public class Officer extends Applicant {
    private Project officerApplication;
    private boolean status;
    private boolean booked = false;
    private List<Project> appliedProjects = new ArrayList<>();
    private List<Project> futureProjects = new ArrayList<>();

    public Officer(String name, String NRIC, String password, String maritalStatus, int age) {
    	super(name, NRIC, password, maritalStatus, age);
    	this.officerApplication = null;
    	this.status = false;
}


    public Project getOfficerApplication() {
        return officerApplication;
    }

    public void setOfficerApplication(Project officerApplication) {
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
    
    public void registerProject(String projectName, Map<String, Project> allProjectsMap) {
        if (!allProjectsMap.containsKey(projectName)) {
            System.out.println("Project not found.");
            return;
        }

        Project newProject = allProjectsMap.get(projectName);

        LocalDate now = LocalDate.now();
        if (now.isBefore(newProject.getAppOpeningDate()) || now.isAfter(newProject.getAppClosingDate())) {
            System.out.println("This project is not open for officer registration.");
            return;
        }

        if (appliedProjects.contains(newProject)) {
            System.out.println("You have already applied for this project.");
            return;
        }

        for (Project p : futureProjects) {
            if (p.isClashing(newProject.getAppOpeningDate(), newProject.getAppClosingDate())) {
                System.out.println("Cannot apply. This project's dates overlap with: " + p.getName());
                return;
            }
        }

        newProject.updateArrOfPendingOfficers(this);
        appliedProjects.add(newProject);
        System.out.println("Registered for project as officer (pending approval): " + newProject.getName());
    }
    
    public void showProfile() {
        if (status && officerApplication != null) {
        	System.out.println("Officer for Project: " + officerApplication.getName());
        } else {
            System.out.println("Officer registration not approved yet.");
        }
    }
    
    public void viewProject() {
        if (officerApplication == null) {
        	System.out.println("You are not handling any project.");
        }

        for (Project project : appliedProjects) {
        	if (project.equals(officerApplication)) {
                project.viewAllDetails();
                return;
            }
        }
        System.out.println("Project not found.");
    }
    
    
    public void successfulApplicants() {
        for (Project project : appliedProjects) {
        	if (project.equals(officerApplication)) {
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
                        applicant.setProject(project);
                        project.updateBookedApplicants(applicant);
                        System.out.println("Booked applicant: " + applicant.getName() + " (" + applicant.getNric() + ")");
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
        	if (!project.equals(officerApplication)) continue;;

            for (Applicant applicant : project.getBookedApplicants()) {
                if (applicant.getNric().equals(nric)) {
                    System.out.println("----- RECEIPT -----");
                    System.out.println("Name: " + applicant.getName());
                    System.out.println("NRIC: " + applicant.getNric());
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
    public void applyProject(List<Project> availableProjects, String projectName, String chosenFlatType) {
        if (projectName == null || chosenFlatType == null) {
            System.out.println("Invalid project or flat type.");
            return;
        }

        Project selectedProject = null;
        for (Project project : availableProjects) {
            if (project.getName().equalsIgnoreCase(projectName)) {
                selectedProject = project;
                break;
            }
        }

        if (selectedProject == null) {
            System.out.println("Project not found in available projects.");
            return;
        }

        if (selectedProject.getPendingOfficerRegistrations().contains(this)) {
            System.out.println("You are already registered as a pending officer for this project. Cannot apply.");
            return;
        }

        if (this.getOfficerApplication() != null && this.getOfficerApplication().equals(selectedProject)) {
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

        this.setProject(selectedProject);
        this.setTypeFlat(chosenFlatType);
        this.setAppStatus("Pending");
        this.setApplied(true);

        selectedProject.updateArrOfApplicants(this);
        System.out.println("You have successfully applied for the " + selectedProject.getName() + " project (" + this.getTypeFlat() + " flat).");
    }


}