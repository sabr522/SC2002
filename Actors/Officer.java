package Actors;

import java.util.ArrayList;

public class Officer implements ApplicantRole {
    private String citizenApplication;
    private String officerApplication;
    private boolean status;
    private boolean booked = false;
    private List<String> enquiries = new ArrayList<>();
    private List<Project> appliedProjects = new ArrayList<>();

    public Officer(String citizenApplication, String officerApplication, boolean status) {
        this.citizenApplication = citizenApplication;
        this.officerApplication = officerApplication;
        this.status = status;
    }

    public String getCitizenApplication() {
        return citizenApplication;
    }

    public void setCitizenApplication(String citizenApplication) {
        this.citizenApplication = citizenApplication;
    }

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

    public void registerProject(String projectName) {
        for (Project project : appliedProjects) {
            if (project.getName().equals(projectName)) {
                if (project.hasApplicant(citizenApplication)) {
                    System.out.println("You have already applied as an applicant for this project.");
                    return;
                }

                if (this.officerApplication != null && !this.officerApplication.isEmpty()) {
                    System.out.println("Already registered as an officer for another project.");
                    return;
                }

                if (!project.isWithinApplicationPeriod()) {
                    System.out.println("This project is not open for registration.");
                    return;
                }

                this.officerApplication = projectName;
                project.setOfficer(this);
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
    
    public String viewProject() {
        if (officerApplication == null) {
            return "You are not handling any project.";
        }

        for (Project project : appliedProjects) {
            if (project.getName().equals(officerApplication)) {
                return project.toString();
            }
        }
        return "Project not found.";
    }
    
    public String[] projEnquiries(String projectName) {
        for (Project project : appliedProjects) {
            if (project.getName().equals(projectName)) {
                List<String> enquiries = project.getEnquiries();
                return enquiries.toArray(new String[0]);
            }
        }
        return new String[]{"No enquiries found."};
    }
    
    
    public void successfulApplicants() {
        for (Project project : appliedProjects) {
            if (project.getName().equals(officerApplication)) {
                for (Applicant applicant : project.getApplicants()) {
                    if (applicant.getStatus().equals("successful")) {
                        String type = applicant.getFlatType();
                        if (project.decrementFlat(type)) {
                            applicant.setStatus("booked");
                            applicant.setBooked(true);
                            System.out.println("Applicant: " + applicant.getName() + " (" + applicant.getNRIC() + ")");
                        } else {
                            System.out.println("No available flats of type: " + type);
                        }
                    }
                }
            }
        }
    }
    
    public void generateReceipt(String nric) {
        for (Project project : appliedProjects) {
            for (Applicant applicant : project.getApplicants()) {
                if (applicant.getNRIC().equals(nric) && applicant.getStatus().equals("booked")) {
                    System.out.println("----- RECEIPT -----");
                    System.out.println("Name: " + applicant.getName());
                    System.out.println("NRIC: " + applicant.getNRIC());
                    System.out.println("Age: " + applicant.getAge());
                    System.out.println("Marital Status: " + applicant.getMaritalStatus());
                    System.out.println("Flat Type: " + applicant.getFlatType());
                    System.out.println("Project: " + project.getName());
                    System.out.println("-------------------");
                    return;
                }
            }
        }
        System.out.println("No applicant found with NRIC: " + nric);
    }
}
