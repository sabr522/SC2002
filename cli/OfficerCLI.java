package cli;

import java.util.Map;
import java.util.Scanner;

import Services.EnquiryService;
import cli.EnquiryCLI;
import Actors.Officer;
import Actors.User;
import Project.Project;
import data.DataManager;

public class OfficerCLI {
    private Officer officer;
    private Scanner scanner;
    private DataManager dataManager;
    private Map<String, Project> allProjectsMap;
    private Map<String, User> allUsersMap;
    private EnquiryService enquiryService;

    public OfficerCLI(Officer officer, Scanner scanner, DataManager dataManager,
                      Map<String, Project> allProjectsMap, Map<String, User> allUsersMap) {
        this.officer = officer;
        this.scanner = scanner;
        this.dataManager = dataManager;
        this.allProjectsMap = allProjectsMap;
        this.allUsersMap = allUsersMap;
        this.enquiryService = enquiryService;
    }

    public void showOfficerMenu() {
        while (true) {
            System.out.println("\n--- Officer Menu ---");
            System.out.println("1. Register for a Project");
            System.out.println("2. Show Officer Profile");
            System.out.println("3. View Project Details");
            System.out.println("4. List Successful Applicants and Book Flats");
            System.out.println("5. Generate Receipt");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    registerForProject();
                    break;
                case 2:
                    showOfficerProfile();
                    break;
                case 3:
                    viewProjectDetails();
                    break;
                case 4:
                    listSuccessfulApplicants();
                    break;
                case 5:
                    generateReceipt();
                    break;
                case 6:
                    manageEnquiries();
                    break;
                case 0:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private void registerForProject() {
        System.out.println("\n--- Register for a Project ---");
        System.out.println("Available Projects:");
        for (Project project : allProjectsMap.values()) {
            System.out.println("Project Name: " + project.getName());
        }

        System.out.print("Enter Project Name to register: ");
        String projectName = scanner.nextLine();

        officer.registerProject(projectName);
    }

    private void showOfficerProfile() {
        officer.showProfile();
    }

    private void viewProjectDetails() {
        officer.viewProject();
    }

    private void listSuccessfulApplicants() {
        officer.successfulApplicants();
    }

    private void generateReceipt() {
        System.out.print("Enter NRIC of applicant: ");
        String nric = scanner.nextLine();

        officer.generateReceipt(nric);
    }
    
    private void manageEnquiries() {
        EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, officer.getNRIC(), true);
        enquiryCLI.showEnquiryMenu(officer.getOfficerApplication());
    }
}
