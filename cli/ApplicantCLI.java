package cli;

import Actors.Applicant;
import Actors.Officer;
import Actors.Manager;
import Actors.User;
import Project.Project;
import data.DataManager;

import java.util.List;
import java.util.Scanner;

public class ApplicantCLI {
    private Applicant applicant;
    private User currentUser;
    private Scanner scanner;
    private DataManager dataManager;

    public ApplicantCLI(Applicant applicant, User currentUser, Scanner scanner, DataManager dataManager) {
        this.applicant = applicant;
        this.currentUser = currentUser;
        this.scanner = scanner;
        this.dataManager = dataManager;
    }

    public void showApplicantMenu() {
        int choice;
        do {
            System.out.println("\n--- Applicant Menu (" + applicant.getName() + ") ---");
            System.out.println("1. View Available Projects");
            System.out.println("2. Apply for Project");
            System.out.println("3. View My applied Project");
            System.out.println("4. Book Flat (if eligible)");
            System.out.println("5. Withdraw Application");
            System.out.println("6. Submit Enquiry");
            System.out.println("7. View/Edit/Delete Enquiries");
            System.out.println("0. Logout");
            System.out.println("Enter choice: ");

            choice = readIntInput();

            switch (choice) {
                case 1:
                    handleViewAvailableProjects();
                    break;
                case 2:
                    handleApplyForProject();
                    break;
                case 3:
                    handleViewApplication();
                    break;
                case 4:
                    handleBookFlat();
                    break;
                case 5:
                    handleWithdrawApplication();
                    break;
                case 6:
                    handleSubmitEnquiry();
                    break;
                case 7:
                    handleEnquiryActions();
                    break;
                case 0:
                    System.out.println("Logging out applicant " + applicant.getName() + "...");
                    break;
                default:
                    System.out.print("Invalid choice. Try again.");
            }
        } while (choice != 0);
    }

    private void handleViewAvailableProjects() {
        List<Project> available = applicant.viewAvailProjects(applicant.isApplied(), applicant.getProject());
        for (Project p : available) {
            System.out.println("- " + p.getName());
        }
    }

    private void handleApplyForProject() {
        List<Project> available = applicant.viewAvailProjects(applicant.isApplied(), applicant.getProject());
        if (available.isEmpty()) return;

        System.out.print("Enter project name to apply: ");
        String projectName = scanner.nextLine();
        System.out.print("Enter flat type (2-Room/3-Room): ");
        String flatType = scanner.nextLine();

        applicant.applyProject(available, projectName, flatType);
    }

    private void handleViewApplication() {
        System.out.println(applicant.viewAppliedProject());
    }


    private void handleBookFlat() {
        Project project = applicant.getProject();
        if (project != null) {
            applicant.bookFlat();
        } else {
            System.out.println("You have not applied to any project.");
        }
    }
    private void handleWithdrawApplication() {
        applicant.withdrawApp();
    }

    private void handleSubmitEnquiry() {
        List<Project> available = applicant.viewAvailProjects(applicant.isApplied(), applicant.getProject());
        System.out.print("Enter project name for enquiry: ");
        String projname = scanner.nextLine();
        System.out.print("Enter your enquiry: ");
        String content = scanner.nextLine();
        applicant.submitEnquiry(projname, content, available);
    }

    private void handleEnquiryActions() {
        System.out.println("1. View Enquiries\n2. Edit Enquiry\n3. Delete Enquiry");
        System.out.print("Choice: ");
        int choice = readIntInput();

        switch (choice) {
            case 1:
            	System.out.println("Your enquiries: ");
                applicant.viewEnquiries();
                break;
            case 2:
                System.out.print("Enter project name to edit enquiry: ");
                String projectToEdit = scanner.nextLine();
                System.out.print("Enter new enquiry content: ");
                String newContent = scanner.nextLine();
                applicant.editEnquiry(projectToEdit, newContent);
                break;
            case 3:
                System.out.print("Enter project name to delete enquiry: ");
                String projectToDelete = scanner.nextLine();
                applicant.deleteEnquiry(projectToDelete);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private int readIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Enter a number:");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume leftover newline
        return value;
    }
}