package cli;

import Actors.Applicant;
import Actors.Officer;
import Actors.Manager;
import Actors.User;
import Project.Project;
import Services.EnquiryService;
import cli.EnquiryCLI;
import data.DataManager;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

/**
 * Provides a Command Line Interface (CLI) for users logged in as an Applicant.
 * Handles user input, calls methods on the Applicant logic object, and displays results.
 */

public class ApplicantCLI {
    private Applicant applicant;
    private Scanner scanner;
    private DataManager dataManager;
    private EnquiryService enquiryService;
    private final Map<String, Project> allProjectsMap;

 

    // --- Constructor for ApplicantCLI ---
    
    public ApplicantCLI(Applicant applicant, Scanner scanner, EnquiryService enquiryService, DataManager dataManager,
            Map<String, Project> allProjectsMap) {
        this.applicant = applicant;
        this.scanner = scanner;
        this.dataManager = dataManager;
        this.enquiryService = enquiryService;
        this.allProjectsMap = allProjectsMap;
    }
    
    

    public void showApplicantMenu() {
        int choice;
        do {
            System.out.println("\n--- Applicant Menu (" + applicant.getName() + ") ---");
            System.out.println("1. View Available Projects");
            System.out.println("2. Apply for Project");
            System.out.println("3. View My applied Project");
            System.out.println("4. Book Flat (if eligible)");
            System.out.println("5. Request to Withdraw Application");
            System.out.println("6. Submit or Handle Enquiries (View/Edit/Delete)");
            System.out.println("0. Logout");
            System.out.println("Enter choice: ");

            choice = readIntInput();

            switch (choice) {
                case 1: handleViewAvailableProjects(); break;
                case 2: handleApplyForProject(); break;
                case 3: handleViewApplication(); break;
                case 4: handleBookFlat(); break;
                case 5: handleWithdrawApplication(); break;
                case 6: handleEnquiryActions(); break;
                case 0:
                    System.out.println("Logging out applicant " + applicant.getName() + "...");
                    break;
                default:
                    System.out.print("Invalid choice. Try again.");
                    break;
            }
        } while (choice != 0);
    }

    
    // --- Handler Methods for Menu Options ---
    
    private void handleViewAvailableProjects() {
    	List<Project> available = applicant.viewAvailProjects(allProjectsMap);
    	System.out.println("Available Projects:");

        String status = applicant.getMaritalStatus();
        int age = applicant.getAge();

        for (Project p : available) {
        	
            System.out.print("Project: " + p.getName());

            if (status.equals("Single") && age >= 35) 
                System.out.println(" | 2-Room units: " + p.getAvalNo2Room());
                
            
            else if (status.equals("Married") && age >= 21) {
                System.out.println(" | 2-Room units: " + p.getAvalNo2Room());
                System.out.println(" | 3-Room units: " + p.getAvalNo3Room());  
            } 
            
            else  
                System.out.println(" | Not eligible for any units.");
            
            System.out.println();
        }
    }

    private void handleApplyForProject() {
        List<Project> available = applicant.viewAvailProjects(allProjectsMap);
        if (available.isEmpty()) {
        	System.out.println("No available projects for you.");
        	return;
        }

        System.out.print("Enter the project name you want to apply for: ");
        String projectName = scanner.nextLine();
        System.out.print("Enter flat type (2-Room/3-Room): ");
        String flatType = scanner.nextLine().trim();

        applicant.applyProject(available, projectName, flatType);
    }

    private void handleViewApplication() {
        System.out.println(applicant.viewAppliedProject());
    }


    private void handleBookFlat() {
        
        if (applicant.getProject() != null) {
            applicant.bookFlat();
        } else {
            System.out.println("You have not applied to any project yet.");
        }
    }
    private void handleWithdrawApplication() {
        applicant.withdrawApp();
    }

    private void handleEnquiryActions() {
        System.out.print("Enter the project name you want to enquire about: ");
        String projectName = scanner.nextLine();
    	EnquiryCLI enquiryCLI = new EnquiryCLI(enquiryService, applicant.getNric(), false); // false = isStaff
        enquiryCLI.showEnquiryMenu(projectName);
   }


    private int readIntInput() {
        int i = -1;        // Default to an invalid value
         while (true) {    // Loop until valid input is received
             try {
                 String line = scanner.nextLine().trim(); // Read whole line
                 if (line.isEmpty()) {
                      System.out.println("Input cannot be empty. Please enter a number.");
                      System.out.print("Enter choice: "); // Re-prompt if needed by context
                      continue;
                 }
                 i = Integer.parseInt(line);
                 break;    // Exit loop if parsing is successful
             } catch (InputMismatchException | NumberFormatException e) { 
                 // scanner.nextLine(); // Consume the invalid input - already handled by reading line
                 System.out.println("Invalid input. Please enter a valid number.");
                 System.out.print("Enter choice: "); // Re-prompt if needed 
                 
             }
        }
         return i;
    }
}
