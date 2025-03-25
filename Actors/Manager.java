package Actors;

import java.util.ArrayList;
import java.util.List;

public class Manager {
    private String name;
    private Project[] projects = new Project[10]; 
    private Officer[] officerRegistration;

    // Constructor
    public Manager(String name) {
        this.name = name;
        if (projects.length == 0 || projects[0] == null) {
            System.out.println("Hi " + name + ", you are handling 0 project.");
        } else {
            System.out.println("Hi " + name + ", you are handling " + projects[0].getName() + ".");
        }
    }

    // Private methods
    private void setName(String name) {
        this.name = name;
    }

    private Project viewAndSelectAll() {
        System.out.println("Available Projects:");
        for (int i = 0; i < projects.length; i++) {
            if (projects[i] != null) {
                System.out.println((i + 1) + ". " + projects[i].getName());
            }
        }
        System.out.println("Enter the project number to select (-1 to cancel):");
        int choice = getUserInput();
        if (choice == -1 || choice > projects.length || projects[choice - 1] == null) {
            return null;
        }
        return projects[choice - 1];
    }

    private Project viewAndSelectOwn() {
        System.out.println("Your Projects:");
        for (int i = 0; i < projects.length; i++) {
            if (projects[i] != null && projects[i].getManagerName().equals(this.name)) {
                System.out.println((i + 1) + ". " + projects[i].getName());
            }
        }
        System.out.println("Enter the project number to select (-1 to cancel):");
        int choice = getUserInput();
        if (choice == -1 || choice > projects.length || projects[choice - 1] == null) {
            return null;
        }
        return projects[choice - 1];
    }

    private void viewSpecific(Project project) {
        if (project != null) {
            project.viewAllDetails();
        } else {
            System.out.println("No project selected.");
        }
    }

    private Officer viewAndSelectPending() {
        System.out.println("Pending Officer Requests:");
        for (int i = 0; i < officerRegistration.length; i++) {
            if (officerRegistration[i] != null && officerRegistration[i].isPending()) {
                System.out.println((i + 1) + ". " + officerRegistration[i].getName());
            }
        }
        System.out.println("Enter the officer number to select (-1 to cancel):");
        int choice = getUserInput();
        if (choice == -1 || choice > officerRegistration.length || officerRegistration[choice - 1] == null) {
            return null;
        }
        return officerRegistration[choice - 1];
    }

    private Applicant viewAndSelectApplicants() {
        System.out.println("Applicants:");
        for (Project project : projects) {
            if (project != null) {
                List<Applicant> applicants = project.getArrOfApplicants();
                for (int i = 0; i < applicants.size(); i++) {
                    System.out.println((i + 1) + ". " + applicants.get(i).getName());
                }
            }
        }
        System.out.println("Enter the applicant number to select (-1 to cancel):");
        int choice = getUserInput();
        if (choice == -1) {
            return null;
        }
        // Assuming we can map the choice to an applicant
        return null; // Replace with actual logic
    }

    private void handleAcceptWithdraw(Applicant applicant) {
        if (applicant.getAppStatus().equals("Successful")) {
            Project project = applicant.getProject();
            project.removeSuccessful(applicant);
            project.incrementRoom(applicant.getTypeFlat());
        }
        applicant.setAppStatus("Null");
        applicant.setWithdrawalStatus(true);
    }

    private void handleRejectWithdraw(Applicant applicant) {
        applicant.setWithdrawalStatus(false);
    }

    // Public methods
    public void createProject() {
        System.out.println("Enter project details:");
        System.out.println("Place Name:");
        String placeName = getUserInputString();
        System.out.println("Neighbourhood:");
        String neighbourhood = getUserInputString();
        System.out.println("Application Period:");
        String appPeriod = getUserInputString();
        System.out.println("Number of 2-room flats:");
        int num2Rooms = getUserInput();
        System.out.println("Number of 3-room flats:");
        int num3Rooms = getUserInput();

        for (Project project : projects) {
            if (project != null && project.getAppPeriod().equals(appPeriod)) {
                System.out.println("Error: Application period clashes with an existing project.");
                return;
            }
        }

        Project newProject = new Project(placeName, neighbourhood, appPeriod, num2Rooms, num3Rooms, this.name);
        for (int i = 0; i < projects.length; i++) {
            if (projects[i] == null) {
                projects[i] = newProject;
                System.out.println("Project created successfully.");
                return;
            }
        }
        System.out.println("Error: No space to add a new project.");
    }

    public void editProject() {
        Project project = viewAndSelectOwn();
        if (project == null) {
            System.out.println("No project selected.");
            return;
        }

        System.out.println("Enter new details for the project:");
        System.out.println("Place Name:");
        String placeName = getUserInputString();
        System.out.println("Neighbourhood:");
        String neighbourhood = getUserInputString();
        System.out.println("Application Period:");
        String appPeriod = getUserInputString();
        System.out.println("Number of 2-room flats:");
        int num2Rooms = getUserInput();
        System.out.println("Number of 3-room flats:");
        int num3Rooms = getUserInput();

        project.setDetails(placeName, neighbourhood, appPeriod, num2Rooms, num3Rooms);
        System.out.println("Project updated successfully.");
    }

    public void delProject() {
        Project project = viewAndSelectOwn();
        if (project == null) {
            System.out.println("No project selected.");
            return;
        }

        for (int i = 0; i < projects.length; i++) {
            if (projects[i] == project) {
                projects[i] = null;
                System.out.println("Project deleted successfully.");
                return;
            }
        }
        System.out.println("Error: Project not found.");
    }

    public void toggleProject() {
        Project project = viewAndSelectOwn();
        if (project == null) {
            System.out.println("No project selected.");
            return;
        }

        project.setVisibility(!project.isVisible());
        System.out.println("Project visibility toggled.");
    }

    public void viewProject() {
        while (true) {
            System.out.println("View all projects or own projects? (1: All, 2: Own, -1: Quit)");
            int choice = getUserInput();
            if (choice == -1) {
                break;
            }

            Project project = (choice == 1) ? viewAndSelectAll() : viewAndSelectOwn();
            if (project != null) {
                viewSpecific(project);
            }
        }
    }

    public void updateRegOfficer() {
        Officer officer = viewAndSelectPending();
        if (officer == null) {
            System.out.println("No officer selected.");
            return;
        }

        System.out.println("Approve or Reject? (1: Approve, 2: Reject)");
        int choice = getUserInput();
        if (choice == 1) {
            for (Project project : projects) {
                if (project != null && project.getManagerName().equals(this.name)) {
                    project.updateArrOfOfficers(officer);
                }
            }
            System.out.println("Officer approved.");
        } else {
            System.out.println("Officer rejected.");
        }
    }

    public void viewApproved() {
        for (Project project : projects) {
            if (project != null) {
                List<Officer> officers = project.getArrOfOfficers();
                System.out.println("Approved Officers for " + project.getName() + ":");
                for (Officer officer : officers) {
                    System.out.println(officer.getName());
                }
            }
        }
    }

    public void updateApp() {
        Applicant applicant = viewAndSelectApplicants();
        if (applicant == null) {
            System.out.println("No applicant selected.");
            return;
        }

        System.out.println("Accept or Reject? (1: Accept, 2: Reject)");
        int choice = getUserInput();
        if (choice == 1) {
            Project project = applicant.getProject();
            if (project.hasRoom(applicant.getTypeFlat())) {
                project.updateApplicantAccepted(applicant);
                System.out.println("Applicant accepted.");
            } else {
                System.out.println("Error: Not enough room available.");
            }
        } else {
            applicant.setAppStatus("Unsuccessful");
            System.out.println("Applicant rejected.");
        }
    }

    public void updateWithdrawal() {
        for (Project project : projects) {
            if (project != null) {
                List<Applicant> withdrawalRequests = project.getWithdrawReq();
                System.out.println("Withdrawal Requests for " + project.getName() + ":");
                for (int i = 0; i < withdrawalRequests.size(); i++) {
                    System.out.println((i + 1) + ". " + withdrawalRequests.get(i).getName());
                }

                System.out.println("Enter the applicant number to process (-1 to cancel):");
                int choice = getUserInput();
                if (choice == -1 || choice > withdrawalRequests.size()) {
                    continue;
                }

                Applicant applicant = withdrawalRequests.get(choice - 1);
                System.out.println("Accept or Reject? (1: Accept, 2: Reject)");
                int action = getUserInput();
                if (action == 1) {
                    handleAcceptWithdraw(applicant);
                    System.out.println("Withdrawal accepted.");
                } else {
                    handleRejectWithdraw(applicant);
                    System.out.println("Withdrawal rejected.");
                }
            }
        }
    }

    public void generateReport() {
        System.out.println("Generating report...");
        for (Project project : projects) {
            if (project != null) {
                project.generateReport();
            }
        }
        System.out.println("Report generated.");
    }

    public void setOfficerRegistration(Officer[] officerRegistration) {
        this.officerRegistration = officerRegistration;
    }

    // Helper methods for user input (mocked for simplicity)
    private int getUserInput() {
        // Mocked input logic
        return -1;
    }

    private String getUserInputString() {
        // Mocked input logic
        return "";
    }
}