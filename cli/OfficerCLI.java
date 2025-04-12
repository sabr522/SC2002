package cli;
import java.time.LocalDate;
import java.util.Scanner;

import Actors.Applicant;
import Actors.Officer;
import Project.Project;

public class OfficerCLI {
	
	// hard-coded part start
    private static Officer currentOfficer = new Officer(
        "Officer One",            
        "S1111111A",                
        "password123",              
        "Single",                   
        30,                         
        null,                      
        null,                       
        true                       
    );

    private static Project[] allProjects = {
            new Project(
                "Project A", 
                "Public", 
                "Officer One", 
                "Punggol", 
                LocalDate.of(2025, 4, 1), 
                LocalDate.of(2025, 5, 1), 
                10, 
                5  
            ),
            new Project(
                "Project B", 
                "Public", 
                "Officer One", 
                "Sengkang", 
                LocalDate.of(2025, 3, 1), 
                LocalDate.of(2025, 4, 30), 
                8, 
                7
            ),
            new Project(
                "Project C", 
                "Private", 
                "Officer Two", 
                "Tampines", 
                LocalDate.of(2025, 2, 15), 
                LocalDate.of(2025, 5, 15), 
                12, 
                4
            )
        };
    // hard-coded part end
 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // hard-coded part start
        for (Project project : allProjects) {
            Project.updateAllProjects(project); 
        }
        
        Applicant applicant1 = new Applicant("John Doe", "S1234567A", "pass123", "Married", 35, "2-Room", "Project A");
        Applicant applicant2 = new Applicant("Jane Smith", "S7654321B", "pass456", "Single", 28, "3-Room", "Project A");

        applicant1.setAppStatus("Successful");
        applicant1.setTypeFlat("2-Room");
        applicant1.setProject("Project A");

        applicant2.setAppStatus("Successful");
        applicant2.setTypeFlat("3-Room");
        applicant2.setProject("Project A");
        
        allProjects[0].getSuccessfulApplicants().add(applicant1);
        allProjects[0].getSuccessfulApplicants().add(applicant2);
        // hard-coded part end

        while (true) {
            System.out.println("\n--- Officer CLI ---");
            System.out.println("1. Register for a Project");
            System.out.println("2. Show Officer Profile");
            System.out.println("3. View Project Details");
            System.out.println("4. List Successful Applicants and Book Flats");
            System.out.println("5. Generate Receipt");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    registerForProject(scanner);
                    break;
                case 2:
                    showOfficerProfile();
                    break;
                case 3:
                    viewProjectDetails(scanner);
                    break;
                case 4:
                    listSuccessfulApplicants();
                    break;
                case 5:
                    generateReceipt(scanner);
                    break;
                case 0:
                    System.out.println("Exiting the application...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private static void registerForProject(Scanner scanner) {
        System.out.println("\n--- Register for a Project ---");
        System.out.println("Available Projects:");
        for (Project project : allProjects) {
            System.out.println("Project Name: " + project.getName());
        }

        System.out.print("Enter Project Name to register: ");
        String projectName = scanner.nextLine();
        
        currentOfficer.registerProject(projectName);
    }

    private static void showOfficerProfile() {
        currentOfficer.showProfile();
    }

    private static void viewProjectDetails(Scanner scanner) {
        System.out.println("\n--- View Project Details ---");
        currentOfficer.viewProject();
    }

    private static void listSuccessfulApplicants() {
        currentOfficer.successfulApplicants();
    }

    private static void generateReceipt(Scanner scanner) {
        System.out.println("\n--- Generate Receipt ---");
        System.out.print("Enter NRIC of applicant: ");
        String nric = scanner.nextLine();

        currentOfficer.generateReceipt(nric);
    }
}
