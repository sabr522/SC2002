package cli;

import Actors.Enquiry;
import Actors.Reply;
import Services.EnquiryService;

import java.util.*;

public class EnquiryCLI {
	private final EnquiryService enquiryService;
	private final String currentNRIC;
	private final boolean isStaff;
	private final Scanner scanner;
	
	public EnquiryCLI(EnquiryService enquiryService, String currentNRIC, boolean isStaff) {
		this.enquiryService = enquiryService;
		this.currentNRIC = currentNRIC;
		this.isStaff = isStaff;
		this.scanner = new Scanner(System.in);
	}
	
	public void showEnquiryMenu(String projectId) {
        while (true) {
            System.out.println("\n--- Enquiry Menu ---");
            System.out.println("1. Submit Enquiry");
            System.out.println("2. View Enquiries for Project");
            System.out.println("3. View My Enquiries");
            System.out.println("4. Edit My Enquiry");
            System.out.println("5. Delete My Enquiry");
            System.out.println("6. View Replies for Enquiry");
            if (isStaff) {
                System.out.println("7. Reply to Enquiry");
                System.out.println("8. Edit My Reply");
                System.out.println("9. View All Enquiries (HDB Manager)");
            }
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> submitEnquiry(projectId);
                case 2 -> viewEnquiriesByProject(projectId);
                case 3 -> viewMyEnquiries();
                case 4 -> editEnquiry();
                case 5 -> deleteEnquiry();
                case 6 -> viewReplies();
                case 7 -> {
                    if (isStaff) replyToEnquiry();
                }
                case 8 -> {
                    if (isStaff) editReply();
                }
                case 9 -> {
                    if (isStaff) viewAllEnquiries();
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

	// applicant: submit enquiry
    private void submitEnquiry(String projectId) {
        System.out.print("Enter your enquiry message: ");
        String message = scanner.nextLine();
        Enquiry enquiry = enquiryService.submitEnquiry(projectId, currentNRIC, message);
        System.out.println("Enquiry submitted with ID: " + enquiry.getId());
    }

    // officer & manager: view enquiries of their project
    private void viewEnquiriesByProject(String projectId) {
        List<Enquiry> enquiries = enquiryService.getEnquiriesByProject(projectId);
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries for this project.");
            return;
        }
        for (Enquiry e : enquiries) {
            System.out.println("\nEnquiry ID: " + e.getId());
            System.out.println("From: " + e.getApplicantNRIC());
            System.out.println("Message: " + e.getContent());
        }
    }

    // applicant: view their submitted enquiries
    private void viewMyEnquiries() {
        List<Enquiry> enquiries = enquiryService.getEnquiryByApplicantNRIC(currentNRIC);
        if (enquiries.isEmpty()) {
            System.out.println("You have no enquiries.");
            return;
        }
        for (Enquiry e : enquiries) {
            System.out.println("\nEnquiry ID: " + e.getId());
            System.out.println("Project: " + e.getProject());
            System.out.println("Message: " + e.getContent());
        }
    }

    // applicant: edit their enquiries
    private void editEnquiry() {
        System.out.print("Enter the Enquiry ID to edit: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter new message: ");
        String newMessage = scanner.nextLine();

        if (enquiryService.editEnquiry(id, currentNRIC, newMessage)) {
            System.out.println("Enquiry updated.");
        } else {
            System.out.println("Edit failed. You can only edit your own enquiry.");
        }
    }

    // applicant: delete their enquiries
    private void deleteEnquiry() {
        System.out.print("Enter the Enquiry ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (enquiryService.deleteEnquiry(id, currentNRIC)) {
            System.out.println("Enquiry deleted.");
        } else {
            System.out.println("Delete failed. You can only delete your own enquiry.");
        }
    }

    // everyone(?): view replies to an enquiry
    private void viewReplies() {
        System.out.print("Enter the Enquiry ID to view replies: ");
        int id = Integer.parseInt(scanner.nextLine());
        List<Reply> replies = enquiryService.viewReplies(id);

        if (replies.isEmpty()) {
            System.out.println("No replies yet.");
            return;
        }
        for (Reply r : replies) {
            System.out.println("Reply ID: " + r.getId());
            System.out.println("Responder: " + r.getResponder());
            System.out.println("Message: " + r.getContent());
            System.out.println("---------------");
        }
    }

    // officer & manager: reply to enquiry regarding their projects
    private void replyToEnquiry() {
        System.out.print("Enter the Enquiry ID to reply to: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter your reply message: ");
        String message = scanner.nextLine();

        if (enquiryService.replyToEnquiry(id, currentNRIC, message)) {
            System.out.println("Reply added.");
        } else {
            System.out.println("Reply failed. Enquiry not found.");
        }
    }

    // officer & manager: edit replies
    private void editReply() {
        System.out.print("Enter Enquiry ID of the reply: ");
        int enquiryId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Reply ID to edit: ");
        int replyId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter new message: ");
        String newMessage = scanner.nextLine();

        if (enquiryService.editReply(enquiryId, replyId, currentNRIC, newMessage)) {
            System.out.println("Reply updated.");
        } else {
            System.out.println("Edit failed. You can only edit your own replies.");
        }
    }

    // manager: view all enquiries
    private void viewAllEnquiries() {
        List<Enquiry> all = enquiryService.getAllEnquiries();
        if (all.isEmpty()) {
            System.out.println("There are no enquiries in the system.");
            return;
        }
        for (Enquiry e : all) {
            System.out.println("\nEnquiry ID: " + e.getId());
            System.out.println("Project: " + e.getProject());
            System.out.println("From: " + e.getApplicantNRIC());
            System.out.println("Message: " + e.getContent());
        }
    }
}
