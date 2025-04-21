package cli;

import java.util.*;

import Actors.Enquiry;
import Actors.Officer;
import Actors.Reply;
import Actors.User;
import Project.Project;
import Services.EnquiryService;

/**
 * CLI class for managing the enquiry interface for all users.
 * Provides functionality to submit, view, edit, and reply to enquiries.
 */
public class EnquiryCLI {
	private final EnquiryService enquiryService;
	private final String currentNRIC;
	private final boolean isStaff; // officer & manager
	private final boolean isManager;
	private final Scanner scanner;
    private final Map<String, User> allUsersMap;
    private final Map<String, Project> allProjectsMap;
	
    /**
     * Constructs a CLI handler for enquiry operations.
     *
     * @param enquiryService The service handling enquiry data
     * @param currentNRIC The NRIC of the current user
     * @param isStaff Flag indicating if the user is a staff member (officer or manager)
     * @param isManager Flag indicating if the user is a manager
     * @param scanner Scanner object for reading input
     * @param allUsersMap Map of all users by NRIC
     * @param allProjectsMap Map of all projects by name
     */
	public EnquiryCLI(EnquiryService enquiryService, String currentNRIC, boolean isStaff, boolean isManager,Scanner scanner, Map<String, User> allUsersMap, Map<String, Project> allProjectsMap) {
		this.enquiryService = enquiryService;
		this.currentNRIC = currentNRIC;
		this.isStaff = isStaff;
		this.isManager = isManager;
		this.scanner = scanner;
        this.allUsersMap = allUsersMap;
        this.allProjectsMap = allProjectsMap;
	}
	
    /**
     * Displays the interactive enquiry menu.
     * Allows users to submit, view, edit, delete, or reply to enquiries based on role.
     *
     * @param projectName The project context passed from caller (optional)
     */
	public void showEnquiryMenu(String projectName) {
        while (true) {
            System.out.println("\n--- Enquiry Menu ---");
            System.out.println("1. Submit Enquiry");
            System.out.println("2. View My Enquiries");
            System.out.println("3. Edit My Enquiry");
            System.out.println("4. Delete My Enquiry");
            System.out.println("5. View Replies for Enquiry");
            if (isStaff) {
                System.out.println("6. Reply to Enquiry");
                System.out.println("7. Edit My Reply");
                System.out.println("8. View Enquiries for Project");
            }
            if (isManager) {
            	System.out.println("9. View All Enquiries");
            }
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> submitEnquiry(projectName);
                case 2 -> viewMyEnquiries();
                case 3 -> editEnquiry();
                case 4 -> deleteEnquiry();
                case 5 -> viewReplies();
                case 6 -> {
                    if (isStaff) replyToEnquiry(projectName);
                }
                case 7 -> {
                    if (isStaff) editReply();
                }
                case 8 -> {
                	if (isStaff) viewEnquiriesByProject(projectName);
                }
                case 9 -> {
                    if (isManager) viewAllEnquiries();
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles submitting a new enquiry. If no project context is provided,
     * it lists visible projects and prompts the user to select one by index.
     * @param contextProjectName The project name passed from the calling menu, or null.
     */
    private void submitEnquiry(String contextProjectName) {
        String projectToSubmit = contextProjectName;

        // general page (0) chosen
        if (projectToSubmit == null) {
            System.out.println("\nPlease select the project you want to submit an enquiry for:");

            // 1. Get list of visible projects (requires Applicant object or passing visibility logic info)
            if (allProjectsMap == null || allProjectsMap.isEmpty()) {
                System.out.println("Error: No project data available.");
                return;
            }

            List<Project> projectOptions = new ArrayList<>(allProjectsMap.values()); // Get all projects
            Map<Integer, String> optionMap = new HashMap<>();
            int displayIndex = 1;

            System.out.println("Available Projects:");
            for (Project p : projectOptions) {
                if (p != null && p.getVisibility()) { 
                    System.out.printf("%d. %s (%s)%n", displayIndex, p.getName(), p.getNeighbourhood());
                    optionMap.put(displayIndex, p.getName());
                    displayIndex++;
                }
            }
            System.out.println("0. Cancel Submission");

            // 2. Get user choice by index
            int choice = -1;
            while (true) {
                System.out.print("Enter project number: ");
                choice = readIntInput(); 
                if (choice == 0) {
                    System.out.println("Enquiry submission cancelled.");
                    return;
                }
                if (optionMap.containsKey(choice)) {
                    projectToSubmit = optionMap.get(choice); // Get the selected project name
                    break;
                } else {
                    System.out.println("Invalid selection. Please enter a number from the list or 0.");
                }
            }
        }
        if (projectToSubmit == null) {
            System.out.println("No project selected. Cannot submit enquiry.");
            return;
        }

        // 3. Get enquiry content
        System.out.print("Enter your enquiry message for project '" + projectToSubmit + "': ");
        String content = scanner.nextLine();
        if (content.trim().isEmpty()){
            System.out.println("Enquiry message cannot be empty. Submission cancelled.");
            return;
        }

        // 4. Submit via service
        try {
            Enquiry enquiry = enquiryService.submitEnquiry(currentNRIC, content, projectToSubmit);
            System.out.println("Enquiry submitted successfully for project '" + projectToSubmit + "' with ID: " + enquiry.getId());
        } catch (Exception e) {
            System.err.println("Error submitting enquiry: " + e.getMessage());
        }
    }

    // officer & manager: view enquiries of their project
    private void viewEnquiriesByProject(String projectName) {
        List<Enquiry> enquiries = enquiryService.getEnquiriesByProject(projectName);
        if (enquiries.isEmpty()) {
            System.out.println("No project specified (Chose General Option). Use option 9 to view all enquiries.");
            return;
        }
        for (Enquiry e : enquiries) {
            String nric = e.getApplicantNRIC();
            User submitter = allUsersMap.get(nric); 
            String displayName = (submitter != null) ? submitter.getName() : nric + " (Name not found)";
    
            System.out.println("\nFrom: " + displayName);
            System.out.println("Enquiry ID: " + e.getId());
            System.out.println("Message: " + e.getContent());
            System.out.println(" Replies: " + e.getReplies().size());
        }
    }

    // applicant: view their submitted enquiries
    private void viewMyEnquiries() {
        System.out.println("\n--- My Submitted Enquiries ---");
        List<Enquiry> enquiries = enquiryService.getEnquiryByApplicantNRIC(currentNRIC);
        if (enquiries.isEmpty()) {
            System.out.println("You have submitted no enquiries.");
            return;
        }
        int displayIndex = 1;
        for (Enquiry e : enquiries) {
             System.out.println("\n--------------------");
             System.out.printf("Enquiry Option #%d%n", displayIndex++);
             System.out.println("--------------------");
             System.out.println("Project: " + e.getProject());
             System.out.println("Enquiry ID: " + e.getId());
             System.out.println("My Message: " + e.getContent());
        }
         System.out.println("--------------------");    
    }

    // applicant: edit their enquiries
    private void editEnquiry() {
        System.out.println("\n--- Edit My Submitted Enquiry ---");
        List<Enquiry> enquiries = enquiryService.getEnquiryByApplicantNRIC(currentNRIC);
    
        if (enquiries.isEmpty()) {
            System.out.println("You have no enquiries to edit.");
            return;
        }
    
        // Display enquiries with indices
        Map<Integer, Integer> optionMap = new HashMap<>(); // Map display index to Enquiry ID
        System.out.println("Your Submitted Enquiries:");
        int displayIndex = 1;
        for (Enquiry e : enquiries) {
             System.out.printf("%d. Project: %s | ID: %d | Message: %s | Replies: %d%n",
                               displayIndex, e.getProject(), e.getId(), e.getContent(), e.getReplies().size());
             optionMap.put(displayIndex, e.getId());
             displayIndex++;
        }
        System.out.println("0. Cancel");
    
        // Get user choice
        System.out.println("Enter ID number of the Enquiry to edit: ");
        int choice = readIntInput();
        if (choice == 0 || !optionMap.containsKey(choice)) {
            System.out.println("Edit cancelled or invalid selection.");
            return;
        }
    
        int enquiryIdToEdit = optionMap.get(choice);
        Enquiry existingEnquiry = enquiryService.getEnquiryById(enquiryIdToEdit); // Get the actual object
    
        // Check if it exists and has replies
        if (existingEnquiry == null) {
             System.out.println("Error: Enquiry not found."); return;
        }
        if (!existingEnquiry.getReplies().isEmpty()) {
            System.out.println("Cannot edit this enquiry (ID: " + enquiryIdToEdit + ") because it already has replies.");
            return;
        }
    
        System.out.println("Current message: " + existingEnquiry.getContent());
        System.out.print("Enter new message: ");
        String newContent = scanner.nextLine();
        if (newContent.trim().isEmpty()) {
             System.out.println("New message cannot be empty. Edit cancelled.");
             return;
        }
    
        if (enquiryService.editEnquiry(enquiryIdToEdit, currentNRIC, newContent)) {
            System.out.println("Enquiry ID " + enquiryIdToEdit + " updated.");
        } else {
            System.out.println("Edit failed (Should not happen after checks).");
        }
    }

    // applicant: delete their enquiries
    private void deleteEnquiry() {
        System.out.println("\n--- Delete My Submitted Enquiry ---");
        List<Enquiry> enquiries = enquiryService.getEnquiryByApplicantNRIC(currentNRIC);

        if (enquiries.isEmpty()) {
            System.out.println("You have no enquiries to delete.");
            return;
        }

        // Display enquiries with indices
        Map<Integer, Integer> optionMap = new HashMap<>();
        System.out.println("Your Submitted Enquiries:");
        int displayIndex = 1;
        for (Enquiry e : enquiries) {
            System.out.printf("%d. Project: %s | ID: %d | Message: %s%n",
                            displayIndex, e.getProject(), e.getId(), e.getContent());
            optionMap.put(displayIndex, e.getId());
            displayIndex++;
        }
        System.out.println("0. Cancel");

        // Get user choice
        System.out.println("Enter the number of the Enquiry to delete: ");
        int choice = readIntInput();
        if (choice == 0 || !optionMap.containsKey(choice)) {
            System.out.println("Deletion cancelled or invalid selection.");
            return;
        }

        int enquiryIdToDelete = optionMap.get(choice);
        // Optional: Confirm deletion
        System.out.print("Are you sure you want to delete Enquiry ID " + enquiryIdToDelete + "? (yes/no): ");
        if (!readYesNoInput()) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (enquiryService.deleteEnquiry(enquiryIdToDelete, currentNRIC)) {
            System.out.println("Enquiry ID " + enquiryIdToDelete + " deleted.");
        } else {
            System.out.println("Delete failed (Should not happen after checks).");
        }
    }

    private void viewReplies() {
        System.out.println("\n--- View Replies for an Enquiry ---");
        // 1. Display user's own enquiries first to select from
        List<Enquiry> enquiries = enquiryService.getEnquiryByApplicantNRIC(currentNRIC);

        // Also add enquiries from projects staff might be handling (if applicable)
        if (isStaff) {
            enquiries.addAll(enquiryService.getAllEnquiries()); 
            enquiries = new ArrayList<>(new LinkedHashSet<>(enquiries));
        }


        if (enquiries.isEmpty()) {
            System.out.println("No enquiries available to view replies for.");
            return;
        }

        Map<Integer, Integer> optionMap = new HashMap<>();
        System.out.println("Select an Enquiry to view its replies:");
        int displayIndex = 1;
        for (Enquiry e : enquiries) {
            String submitterName = getUserName(e.getApplicantNRIC()); // Use helper
            System.out.printf("%d. Project: %s | ID: %d | From: %s | Message: %s%n",
                            displayIndex, e.getProject(), e.getId(), submitterName, e.getContent());
            optionMap.put(displayIndex, e.getId());
            displayIndex++;
        }
        System.out.println("0. Cancel");

        // 2. Get user choice
        System.out.println("Enter the number of the Enquiry: ");
        int choice = readIntInput();
        if (choice == 0 || !optionMap.containsKey(choice)) {
            System.out.println("Cancelled or invalid selection.");
            return;
        }
        int enquiryIdToView = optionMap.get(choice);

        // 3. Get and display the selected enquiry and its replies
        Enquiry enquiry = enquiryService.getEnquiryById(enquiryIdToView);
        if (enquiry == null) { 
            System.out.println("Error: Selected enquiry not found."); return;
        }

        System.out.println("\n--- Replies for Enquiry ID: " + enquiryIdToView + " ---");
        System.out.println("Original Enquiry (" + getUserName(enquiry.getApplicantNRIC()) + "): " + enquiry.getContent());
        System.out.println("---------------------------------");

        List<Reply> replies = enquiry.getReplies(); // Get replies directly from enquiry object
        if (replies.isEmpty()) {
            System.out.println("No replies yet for this enquiry.");
        } else {
            for (Reply r : replies) {
                String responderName = getUserName(r.getResponderNRIC());
                User responderUser = allUsersMap.get(r.getResponderNRIC());
                String responderRole = responderUser.getRole(); 

                System.out.println(" -> Reply #" + r.getId() + " From: " + responderName + " (" + responderRole + ")");
                System.out.println("    Message: " + r.getContent());
                System.out.println("    ----------");
            }
        }
        System.out.println("--- End of Replies ---");
    }

    /**
     * Handles replying to an enquiry. Lists relevant enquiries based on user role and context.
     * @param contextProjectName The specific project context, or null if viewing generally (Manager only).
     */
    private void replyToEnquiry(String contextProjectName) {
        // 1. Permission Check
        if (!isStaff) {
            System.out.println("Access denied. Only Staff can reply.");
            return;
        }

        // 2. Determine which enquiries to list based on role and context
        List<Enquiry> enquiriesToList;
        String listingHeader;
        String projectToFilter = contextProjectName; // Use the passed context initially

        if (isManager && projectToFilter == null) {
            // Manager viewing generally -> List ALL enquiries
            listingHeader = "--- Replying to Enquiry (All Projects) ---";
            enquiriesToList = enquiryService.getAllEnquiries();
        } else if (projectToFilter != null) {
            // Manager or Officer viewing specific project context provided by caller
            if (!isManager) { // If it's an Officer, verify they handle this project
                 User currentUser = allUsersMap.get(currentNRIC);
                 boolean handlesThisProject = false;
                 if (currentUser instanceof Officer) {
                      Map<Project, String> assignments = ((Officer)currentUser).getProjectAssignments();
                      for (Project p : assignments.keySet()) {
                           if(p != null && projectToFilter.equals(p.getName()) && "Approved".equalsIgnoreCase(assignments.get(p))) {
                                handlesThisProject = true;
                                break;
                           }
                      }
                 }
                 if (!handlesThisProject) {
                      System.out.println("Error: You are not the approved Officer for project '" + projectToFilter + "'. Cannot reply.");
                      return;
                 }
            }
            // Proceed for Manager with context or verified Officer
            listingHeader = "--- Replying to Enquiries for Project: " + projectToFilter + " ---";
            enquiriesToList = enquiryService.getEnquiriesByProject(projectToFilter);
        } else {
            // This case: Officer (isStaff=true, isManager=false) but projectToFilter is null.
            // This should not happen if OfficerCLI forces project selection first.
            System.out.println("Error: A specific project context is required for Officers to reply.");
            return;
        }

        System.out.println(listingHeader);

        // 3. Display relevant enquiries for selection
        if (enquiriesToList == null || enquiriesToList.isEmpty()) { // Added null check
            System.out.println("No enquiries found in this scope to reply to.");
            return;
        }

        Map<Integer, Integer> optionMap = new HashMap<>(); // Display Index -> Enquiry ID
        System.out.println("Select an Enquiry to reply to:");
        int displayIndex = 1;
        for (Enquiry e : enquiriesToList) {
             if (e == null) continue; // Safety check
            String submitterName = getUserName(e.getApplicantNRIC());
            // Show project only if manager is viewing all
            String projectInfo = (isManager && contextProjectName == null) ? " | Project: " + e.getProject() : "";
            System.out.printf("%d. ID: %d%s | From: %s | Message: %s | Replies: %d%n",
                            displayIndex, e.getId(), projectInfo, submitterName, e.getContent(), e.getReplies().size());
            optionMap.put(displayIndex, e.getId());
            displayIndex++;
        }
        System.out.println("0. Cancel");

        // 4. Get user choice for Enquiry ID
        System.out.println("Enter the number of the Enquiry: ");
        int choice = readIntInput();
        if (choice == 0 || !optionMap.containsKey(choice)) {
            System.out.println("Cancelled or invalid selection.");
            return;
        }
        int enquiryIdToReply = optionMap.get(choice);

        // 5. Get reply content
        System.out.print("Enter your reply message: ");
        String content = scanner.nextLine(); // Use class scanner
        if (content.trim().isEmpty()) {
            System.out.println("Reply cannot be empty. Action cancelled.");
            return;
        }

        // 6. Submit reply via service
        if (enquiryService.replyToEnquiry(enquiryIdToReply, currentNRIC, content)) {
            System.out.println("Reply added successfully to Enquiry ID " + enquiryIdToReply + ".");
        } else {
            System.out.println("Reply failed. Enquiry might have been deleted or an error occurred.");
        }
    }

    // officer & manager: edit replies
    private void editReply() {
        if (!isStaff) { System.out.println("Access denied."); return; }

        System.out.println("\n--- Edit Your Reply ---");

        // 1. Find all replies made by the current staff member across all enquiries
        List<Reply> myReplies = new ArrayList<>();
        List<Enquiry> allEnquiries = enquiryService.getAllEnquiries(); // Get all enquiries
        for (Enquiry e : allEnquiries) {
            for (Reply r : e.getReplies()) {
                if (r.getResponderNRIC().equals(currentNRIC)) {
                    // We need the reply object itself to edit later
                    myReplies.add(r);
                }
            }
        }

        if (myReplies.isEmpty()) {
            System.out.println("You have not made any replies to edit.");
            return;
        }

        // 2. Display the found replies with context, mapping display index to the Reply object
        System.out.println("Select the reply you wish to edit:");
        Map<Integer, Reply> optionMap = new HashMap<>(); // Display Index -> Reply Object
        int displayIndex = 1;
        for (Reply r : myReplies) {
            Enquiry parentEnquiry = enquiryService.getEnquiryById(r.getEnquiryId()); // Get parent
            if (parentEnquiry != null) {
                System.out.println("\n--------------------");
                System.out.printf("Option #%d%n", displayIndex);
                System.out.println("--------------------");
                System.out.println("Project: " + parentEnquiry.getProject());
                System.out.println("Original Enquiry (ID: " + parentEnquiry.getId() + " From: "+ getUserName(parentEnquiry.getApplicantNRIC()) +"):");
                System.out.println("  \"" + parentEnquiry.getContent() + "\"");
                System.out.println("Your Reply (ID: " + r.getId() + "):");
                System.out.println("  \"" + r.getContent() + "\"");

                optionMap.put(displayIndex, r); // Map display index to the actual Reply object
                displayIndex++;
            }
        }
        System.out.println("--------------------");
        System.out.println("0. Cancel");

        // 3. Get user choice
        System.out.print("Enter the number of the Reply to edit: ");
        int choice = readIntInput();
        if (choice == 0 || !optionMap.containsKey(choice)) {
            System.out.println("Edit cancelled or invalid selection.");
            return;
        }

        Reply replyToEdit = optionMap.get(choice);
        int targetEnquiryId = replyToEdit.getEnquiryId();
        int targetReplyId = replyToEdit.getId();

        // 4. Get new content
        System.out.println("\nCurrent reply message: \"" + replyToEdit.getContent() + "\"");
        System.out.print("Enter new reply message: ");
        String newContent = scanner.nextLine();
        if (newContent.trim().isEmpty()) {
            System.out.println("New message cannot be empty. Edit cancelled.");
            return;
        }

        // 5. Submit edit via service
        if (enquiryService.editReply(targetEnquiryId, targetReplyId, currentNRIC, newContent)) {
            System.out.println("Reply ID " + targetReplyId + " (for Enquiry ID " + targetEnquiryId + ") updated successfully.");
        } else {
            // This could happen if the enquiry/reply was deleted between listing and editing attempt
            System.out.println("Edit failed. Reply or Enquiry may no longer exist, or you did not write that reply.");
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
            String nric = e.getApplicantNRIC();
            User submitter = allUsersMap.get(nric); // Look up user
            String displayName = (submitter != null) ? submitter.getName() : nric + " (Name not found)"; // Get name or fallback
   
           System.out.println("\nProject: " + e.getProject());
           System.out.println("From: " + displayName);
           System.out.println("Enquiry ID: " + e.getId());
           System.out.println("Message: " + e.getContent());
            System.out.println(" Replies: " + e.getReplies().size());
        }
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

    private boolean readYesNoInput() {
        String input = "";
        while (true) {
            input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("true") || input.equals("yes") || input.equals("t") || input.equals("y")) {
                return true;
            } else if (input.equals("false") || input.equals("no") || input.equals("f") || input.equals("n")) {
                return false;
            } else {
                System.out.print("Invalid input. Please enter true/false or yes/no: ");
            }
        }
    }
    private String getUserName(String nric) {
        if (allUsersMap != null && nric != null) {
            User user = allUsersMap.get(nric);
            if (user != null) {
                return user.getName();
            }
        }
        return nric + " (Unknown)"; // Fallback
    }
}
