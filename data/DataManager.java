package data;

import Actors.User;
import Actors.Applicant;
import Actors.Enquiry;
import Actors.Manager;
import Actors.Officer;
import Actors.Reply;
import Project.Project;
// Add imports for Enquiry if needed
import Services.EnquiryService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // For building relationships

/**
 * Handles loading from and saving data to CSV files.
 * Acts as the persistence layer for the application.
 * Provides methods to retrieve filtered data subsets based on application state.
 */
public class DataManager {

    // Define file paths using a consistent structure
    private static final String DATA_FOLDER = "data_folders"; // Define base folder
    private static final String USERS_CSV_PATH = DATA_FOLDER + "/users.csv";
    private static final String PROJECTS_CSV_PATH = DATA_FOLDER + "/project.csv"; // Corrected name from project_core
    private static final String PROJECT_FLATS_CSV_PATH = DATA_FOLDER + "/project_flats.csv";
    private static final String PROJECT_OFFICERS_CSV_PATH = DATA_FOLDER + "/project_officers.csv";
    private static final String APPLICATIONS_CSV_PATH = DATA_FOLDER + "/applications.csv";
    private static final String ENQUIRIES_CSV_PATH = DATA_FOLDER + "/enquiries.csv"; 
    private static final String REPLIES_CSV_PATH = DATA_FOLDER + "/replies.csv";     

    // Consistent date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    // Define CSV Headers
    private static final String USERS_HEADER = "NRIC,Name,Age,MaritalStatus,PasswordHash,Salt,Role";
    private static final String PROJECTS_HEADER = "ProjectName,Neighborhood,Visibility,CreatorName,AppOpeningDate,AppClosingDate";
    private static final String FLATS_HEADER = "ProjectName,FlatType,TotalUnits,AvailableUnits,SellingPrice"; // Added SellingPrice for completeness
    private static final String OFFICERS_HEADER = "ProjectName,OfficerNRIC,Status"; // Status: Approved | Pending
    private static final String APPLICATIONS_HEADER = "ApplicantNRIC,ProjectName,FlatTypeApplied,ApplicationStatus,WithdrawalStatus,HasApplied"; // Status: Pending | Successful | Unsuccessful | Withdrawn | Booked
    private static final String ENQUIRIES_HEADER = "EnquiryID,SubmitterNRIC,ProjectName,EnquiryContent"; 
    private static final String REPLIES_HEADER = "EnquiryID,ReplyID,ResponderNRIC,ReplyContent"; 

    /**
     * Constructs the DataManager and ensures required files exist.
     */
    public DataManager() {
        // Ensure data files exist on initialization (optional but recommended)
        ensureFileExists(USERS_CSV_PATH, USERS_HEADER);
        ensureFileExists(PROJECTS_CSV_PATH, PROJECTS_HEADER);
        ensureFileExists(PROJECT_FLATS_CSV_PATH, FLATS_HEADER);
        ensureFileExists(PROJECT_OFFICERS_CSV_PATH, OFFICERS_HEADER);
        ensureFileExists(APPLICATIONS_CSV_PATH, APPLICATIONS_HEADER);
        ensureFileExists(ENQUIRIES_CSV_PATH, ENQUIRIES_HEADER); 
        ensureFileExists(REPLIES_CSV_PATH, REPLIES_HEADER);
    }

    // --- File Existence Check ---
    private void ensureFileExists(String filePath, String header) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found, creating: " + filePath);
            try {
                // Ensure parent directory exists
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                         System.out.println("Created directory: " + parentDir.getPath());
                    } else {
                         System.err.println("Failed to create directory: " + parentDir.getPath());
                         // Consider throwing an exception here if directory is crucial
                    }
                }

                if (file.createNewFile()) {
                    // Write header to the new file
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                        bw.write(header);
                        bw.newLine();
                         System.out.println("Header written to " + filePath);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file or writing header for: " + filePath);
                e.printStackTrace();
                 // Consider re-throwing or handling more gracefully
            }
        }
    }

    // --- Generic CSV Reading (Simplified) ---
    private List<String[]> readCsvFile(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        File file = new File(filePath);

        // Check if file exists before attempting to read
        if (!file.exists()) {
             // Returning empty list might be okay if file creation failed silently earlier
             // Or throw FileNotFoundException for clarity
             System.err.println("Warning: File not found during read: " + filePath + ". Returning empty data.");
            // throw new FileNotFoundException("CSV file not found: " + filePath);
             return data;
        }


        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Basic CSV split - doesn't handle quoted commas well.
                    // For this project, ensure data doesn't contain commas within fields
                    // or use a proper CSV library if needed.
                    String[] values = line.split(",", -1); // Keep trailing empty fields
                    data.add(values);
                }
            }
        } catch (FileNotFoundException e) {
             // Should be caught by the check above, but good practice to keep
             System.err.println("Error: File not found when reading: " + filePath);
             throw e; // Re-throw FNF
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + filePath);
            e.printStackTrace();
            throw e; // Re-throw IOE
        }
        return data;
    }


    // --- Generic CSV Writing (Simplified) ---
    private void writeCsvFile(String filePath, List<String[]> data, String header) throws IOException {
        // Ensure directory exists before writing
        File file = new File(filePath);
         File parentDir = file.getParentFile();
         if (parentDir != null && !parentDir.exists()) {
             if (!parentDir.mkdirs()) {
                  throw new IOException("Failed to create directory for saving: " + parentDir.getPath());
             }
         }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) { // Overwrite mode
            bw.write(header); // Write header every time
            bw.newLine();
            for (String[] rowData : data) {
                 if (rowData != null) { // Basic null check for row
                     // Escape each field before joining
                     List<String> escapedFields = new ArrayList<>();
                     for (String field : rowData) {
                         escapedFields.add(escapeCsvField(field));
                     }
                     bw.write(String.join(",", escapedFields));
                     bw.newLine();
                 }
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + filePath);
            e.printStackTrace();
            throw e; // Re-throw
        }
    }


    // === Loading Methods ===

    /**
     * Loads all users (Applicants, Officers, Managers) from users.csv.
     * @return A Map where the key is NRIC and the value is the User object.
     * @throws IOException If the file is missing or unreadable.
     */
     public Map<String, User> loadUsers() throws IOException {
         Map<String, User> users = new HashMap<>();
         List<String[]> csvData = readCsvFile(USERS_CSV_PATH);
         // NRIC[0],Name[1],Age[2],MaritalStatus[3],PasswordHash[4],(Salt[5]),Role[6]
 
         boolean isLoadingHashed = false; // Flag to track format
         if (!csvData.isEmpty() && csvData.get(0).length >= 7) {
              isLoadingHashed = true;
              System.out.println("Detected 7+ columns, attempting to load hashed passwords and salts.");
         } else if (!csvData.isEmpty()) {
              System.out.println("Detected 6 columns, assuming initial load with plain passwords.");
         }
 
 
         for (String[] values : csvData) {
             int expectedLength = isLoadingHashed ? 7 : 6;
             if (values.length < expectedLength) {
                  System.err.println("Skipping malformed user row (expected " + expectedLength + " columns): " + String.join(",", values));
                  continue;
             }
 
             try {
                 String nric = values[0].trim();
                 String name = values[1].trim();
                 int age = Integer.parseInt(values[2].trim());
                 String maritalStatus = values[3].trim();
                 String role = isLoadingHashed ? values[6].trim() : values[5].trim(); // Get role from correct index
                 if (nric.isEmpty()) continue;
 
                 User user = null;
 
                 String tempPasswordForConstructor = "password"; 
 
                 switch (role.toLowerCase()) {
                     case "manager": user = new Manager(name, nric, tempPasswordForConstructor, maritalStatus, age); break;
                     case "officer": user = new Officer(name, nric, tempPasswordForConstructor, maritalStatus, age); break;
                     case "applicant": user = new Applicant(name, nric, tempPasswordForConstructor, maritalStatus, age); break;
                     default: System.err.println("Warning: Invalid role '" + role + "' for NRIC " + nric); continue;
                 }
 
                 // Now, load credentials based on detected format
                 if (isLoadingHashed) {
                     // Loading existing hash and salt
                     String loadedPasswordHash = values[4];
                     String loadedSalt = values[5];
                     // Use a method to directly set the loaded hash and salt
                     user.loadCredentials(loadedPasswordHash, loadedSalt);
                 } else {
                     String plainPasswordFromCsv = values[4];
                      if (!"password".equals(plainPasswordFromCsv) && !plainPasswordFromCsv.isEmpty()) {
                           user.setPassword(plainPasswordFromCsv); // Re-call setPassword to hash this specific plain pass
                      }
                 }
 
                users.put(nric.toUpperCase(), user); // Use consistent key casing
 
             } catch (NumberFormatException e) { System.err.println("Error parsing age for user row: " + String.join(",", values) + ". Skipping.");
             } catch (IllegalArgumentException e) { System.err.println("Error creating user object: " + e.getMessage() + ". Skipping row: " + String.join(",", values));
             } catch (Exception e) { System.err.println("Unexpected error processing user row: " + String.join(",", values)); e.printStackTrace(); }
         }
         System.out.println("Loaded " + users.size() + " users.");
         return users;
    }




    /**
     * Loads the core project data from projects.csv.
     * Does NOT load related data like flats, officers, or applicants yet.
     * @return A Map where the key is ProjectName and the value is the Project object.
     * @throws IOException If the file cannot be read.
     */
    public Map<String, Project> loadProjectsCore() throws IOException {
        Map<String, Project> projects = new HashMap<>();
        List<String[]> csvData = readCsvFile(PROJECTS_CSV_PATH);
        // Header: ProjectName[0],Neighborhood[1],Visibility[2],CreatorName[3],AppOpeningDate[4],AppClosingDate[5]

        for (String[] values : csvData) {
            if (values.length < 6) {
                System.err.println("Skipping malformed project row: " + String.join(",", values));
                 continue;
            }
            try {
                String projectName = values[0].trim();
                String neighborhood = values[1].trim();
                boolean visibility = Boolean.parseBoolean(values[2].trim().toLowerCase());
                String creatorName = values[3].trim(); // Manager's Name (used for linking/filtering)
                LocalDate openDate = LocalDate.parse(values[4].trim(), DATE_FORMATTER);
                LocalDate closeDate = LocalDate.parse(values[5].trim(), DATE_FORMATTER);

                 if (projectName.isEmpty()) {
                     System.err.println("Skipping project row with empty Project Name.");
                     continue;
                 }
                 if (creatorName.isEmpty()) {
                      System.err.println("Warning: Project row with empty Creator Name: " + projectName);
                      // Decide if this is allowed or should be skipped
                 }

                // Assume a Project constructor that takes core info and initializes lists/counts
                // Example: Project(name, visibility, creatorName, neighborhood, openDate, closeDate, num2R, num3R)
                // We initialize room counts to 0 here; they will be updated by loadProjectFlats.
                Project project = new Project(projectName, visibility, creatorName, neighborhood,
                                              openDate, closeDate, 0, 0);

                 if (projects.containsKey(projectName)) {
                     System.err.println("Warning: Duplicate Project Name found: " + projectName + ". Overwriting previous entry.");
                 }
                projects.put(projectName, project);

            } catch (DateTimeParseException e) {
                System.err.println("Error parsing date for project row: " + String.join(",", values) + ". Skipping.");
            } catch (IllegalArgumentException e) { // Catch potential errors in Boolean.parseBoolean or Project constructor
                 System.err.println("Error processing project data: " + e.getMessage() + ". Skipping row: " + String.join(",", values));
            } catch (Exception e) {
                 System.err.println("Unexpected error processing project row: " + String.join(",", values));
                 e.printStackTrace();
            }
        }
        System.out.println("Loaded " + projects.size() + " core projects.");
        return projects;
    }


    /**
     * Loads flat information and adds it to the corresponding Project objects.
     * Must be called *after* loadProjectsCore.
     * @param projects The map of projects loaded by loadProjectsCore.
     * @throws IOException If the file cannot be read.
     */
    public void loadProjectFlats(Map<String, Project> projects) throws IOException {
        List<String[]> csvData = readCsvFile(PROJECT_FLATS_CSV_PATH);
        int flatsLoaded = 0;
        // Header: ProjectName[0],FlatType[1],TotalUnits[2],AvailableUnits[3],SellingPrice[4]

        for (String[] values : csvData) {
            if (values.length < 5) { // Expect at least 5 columns now
                System.err.println("Skipping malformed project flat row: " + String.join(",", values));
                continue;
            }
            try {
                String projectName = values[0].trim();
                Project project = projects.get(projectName); // Find the project object

                if (project != null) {
                    String flatType = values[1].trim();
                    int totalUnits = Integer.parseInt(values[2].trim());
                    int availableUnits = Integer.parseInt(values[3].trim());
                    // double sellingPrice = Double.parseDouble(values[4].trim()); // If needed

                    if ("2-Room".equalsIgnoreCase(flatType)) {
                        project.setNo2Room(project.getCreatorName(), totalUnits);
                        project.setAvalNo2Room(availableUnits);
                    } else if ("3-Room".equalsIgnoreCase(flatType)) {
                        project.setNo3Room(project.getCreatorName(),totalUnits);
                        project.setAvalNo3Room(availableUnits);
                    } else {
                         System.err.println("Warning: Unknown flat type '" + flatType + "' for project '" + projectName + "'. Skipping flat info.");
                         continue; // Skip this flat type
                    }
                    flatsLoaded++;
                } else {
                     System.err.println("Warning: Project '" + projectName + "' not found for flat info. Skipping row.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing number for project flat row: " + String.join(",", values) + ". Skipping.");
            } catch (Exception e) {
                 System.err.println("Unexpected error processing project flat row: " + String.join(",", values));
                 e.printStackTrace();
            }
        }
        System.out.println("Loaded flat info for " + flatsLoaded + " entries.");
    }


    /**
     * Loads officer assignments, links Officer objects to Projects, and sets officer status.
     * Must be called *after* loadProjectsCore and loadUsers.
     * @param projects The map of projects loaded by loadProjectsCore.
     * @param users The map of users loaded by loadUsers.
     * @throws IOException If the file cannot be read.
     */
    public void loadProjectOfficers(Map<String, Project> projects, Map<String, User> users) throws IOException {
        List<String[]> csvData = readCsvFile(PROJECT_OFFICERS_CSV_PATH);
        int assignmentsLoaded = 0;
        // Header: ProjectName[0],OfficerNRIC[1],Status[2] (Approved | Pending)

        for (String[] values : csvData) {
            if (values.length < 3) {
                System.err.println("Skipping malformed project officer row: " + String.join(",", values));
                continue;
            }
            try {
                String projectName = values[0].trim();
                String officerNric = values[1].trim();
                String status = values[2].trim(); // "Approved" or "Pending"

                Project project = projects.get(projectName);
                User user = users.get(officerNric);

                if (project == null) {
                     System.err.println("Warning: Project '" + projectName + "' not found for officer assignment. Skipping row.");
                     continue;
                }
                 if (user == null) {
                      System.err.println("Warning: Officer NRIC '" + officerNric + "' not found in users list for project '" + projectName + "'. Skipping row.");
                      continue;
                 }


                if (user instanceof Officer) { // Check if user is actually an Officer
                    Officer officer = (Officer) user;
                    boolean isApproved = "Approved".equalsIgnoreCase(status);
                    officer.setHandlingApproved(isApproved);
                    officer.setHandledProject(project);
                    
                    if (isApproved) {
                        project.updateArrOfOfficers(project.getCreatorName(), officer); //Adds approved officers 
                    } else {
                        project.updateArrOfPendingOfficers(officer); //Adds pending officers
                        System.out.println("Note: Logic to add Officer to Project's *pending* list needs implementation in Project class for NRIC " + officerNric);
                    }
                    assignmentsLoaded++;
                } else {
                    // User exists but is not an Officer
                    System.err.println("Warning: User '" + officerNric + "' assigned to project '" + projectName + "' is not an Officer (Role: " + user.getRole() + "). Skipping assignment.");
                }

            } catch (Exception e) {
                 System.err.println("Unexpected error processing project officer row: " + String.join(",", values));
                 e.printStackTrace();
            }
        }
        System.out.println("Loaded " + assignmentsLoaded + " officer assignments.");
    }

    /**
     * Loads application data, links Applicant objects to Projects, and updates Applicant state.
     * Must be called *after* loadProjectsCore and loadUsers.
     * @param projects Map of projects loaded by loadProjectsCore.
     * @param users Map of users loaded by loadUsers.
     * @throws IOException If the file cannot be read.
     */
    public void loadApplications(Map<String, Project> projects, Map<String, User> users) throws IOException {
        List<String[]> csvData = readCsvFile(APPLICATIONS_CSV_PATH);
        int appsLoaded = 0;
        // Header: ApplicantNRIC[0],ProjectName[1],FlatTypeApplied[2],ApplicationStatus[3],WithdrawalStatus[4],HasApplied[5]

        for (String[] values : csvData) {
            if (values.length < 6) {
                System.err.println("Skipping malformed application row: " + String.join(",", values));
                continue;
            }
            try {
                String applicantNric = values[0].trim();
                String projectName = values[1].trim();
                String flatTypeApplied = values[2].trim();
                String appStatus = values[3].trim(); // "Pending", "Successful", "Unsuccessful", "Withdrawn", "Booked"
                boolean withdrawalStatus = Boolean.parseBoolean(values[4].trim().toLowerCase()); // Check meaning - True if withdrawn/pending withdrawal?
                boolean hasApplied = Boolean.parseBoolean(values[5].trim().toLowerCase()); // If they submitted

                Project project = projects.get(projectName);
                User user = users.get(applicantNric);

                 if (project == null) {
                     System.err.println("Warning: Project '" + projectName + "' not found for application. Skipping row: " + applicantNric);
                     continue;
                 }
                  if (user == null) {
                       System.err.println("Warning: Applicant NRIC '" + applicantNric + "' not found in users list for project '" + projectName + "'. Skipping row.");
                       continue;
                  }

                if (user instanceof Applicant) {
                    Applicant applicant = (Applicant) user;

                    // **Update applicant object state based on CSV**
                   
                    applicant.setProject(project);    // Link applicant to project object 
                    applicant.setTypeFlat(flatTypeApplied);
                    applicant.setAppStatus(appStatus);
                    applicant.setWithdrawalStatus(withdrawalStatus);
                    applicant.setApplied(hasApplied);


                    // Adds applicant to the correct list within the project based on status
                    project.addApplicantToCorrectList(applicant); 

                    appsLoaded++;
                } else {
                    System.err.println("Warning: User '" + applicantNric + "' applying to project '" + projectName + "' is not an Applicant (Role: " + user.getRole() + "). Skipping application.");
                }
            } catch (IllegalArgumentException e) { // Catch potential errors in Boolean.parseBoolean
                 System.err.println("Error processing application data: " + e.getMessage() + ". Skipping row: " + String.join(",", values));
             } catch (Exception e) {
                  System.err.println("Unexpected error processing application row: " + String.join(",", values));
                  e.printStackTrace();
            }
        }
        System.out.println("Loaded " + appsLoaded + " applications.");
    }

    /**
     * Loads Enquiries and their Replies from CSV files.
     * Populates the passed EnquiryService instance.
     * Resets static ID counters in Enquiry and Reply classes.
     * @param enquiryService The EnquiryService to populate.
     * @throws IOException If reading the file fails.
     */
    public void loadEnquiries(EnquiryService enquiryService) throws IOException {
        if (enquiryService == null) {
            System.err.println("EnquiryService is null, cannot load enquiries.");
            return;
        }
        Map<Integer, Enquiry> loadedEnquiries = new HashMap<>(); // Temp map

        // 1. Load Enquiries
        List<String[]> enquiryData = readCsvFile(ENQUIRIES_CSV_PATH);
        int maxEnquiryId = 0;
        System.out.println("Reading " + enquiryData.size() + " enquiry rows...");
        for (String[] values : enquiryData) {
            if (values.length < 4) continue;
            try {
                int enquiryId = Integer.parseInt(values[0].trim());
                String submitterNric = values[1].trim();
                String projectName = values[2].trim();
                String content = values[3].trim(); // Assuming content is not escaped complexly

                Enquiry enquiry = new Enquiry(submitterNric, content, projectName, enquiryId); // Use loading constructor
                loadedEnquiries.put(enquiryId, enquiry);
                if (enquiryId > maxEnquiryId) maxEnquiryId = enquiryId;

            } catch (Exception e) { System.err.println("Error processing enquiry row: " + String.join(",", values) + " -> " + e.getMessage()); }
        }
        Enquiry.resetIdCounter(maxEnquiryId); // Reset static counter

        // 2. Load Replies
        List<String[]> replyData = readCsvFile(REPLIES_CSV_PATH);
        int repliesLoaded = 0;
        Map<Integer, Integer> replyCounters = new HashMap<>(); // Max reply ID per enquiry
        System.out.println("Reading " + replyData.size() + " reply rows...");
        for (String[] values : replyData) {
            if (values.length < 4) continue;
            try {
                int enquiryId = Integer.parseInt(values[0].trim());
                int replyId = Integer.parseInt(values[1].trim());
                String responderNric = values[2].trim();
                String content = values[3].trim(); // Assuming simple content

                Enquiry parentEnquiry = loadedEnquiries.get(enquiryId);
                if (parentEnquiry != null) {
                    Reply reply = new Reply(parentEnquiry, responderNric, content, replyId); // Use loading constructor
                    parentEnquiry.addReply(reply);
                    repliesLoaded++;
                    replyCounters.put(enquiryId, Math.max(replyCounters.getOrDefault(enquiryId, 0), replyId));
                } else System.err.println("Warning: Cannot load reply - parent enquiry ID " + enquiryId + " not found.");
            } catch (Exception e) { System.err.println("Error processing reply row: " + String.join(",", values) + " -> " + e.getMessage()); }
        }
        Reply.resetIdCounters(replyCounters); // Reset static counters

        // 3. Populate Service
        enquiryService.loadExistingEnquiries(loadedEnquiries); // Add method to EnquiryService
        System.out.println("Loaded " + loadedEnquiries.size() + " enquiries and " + repliesLoaded + " replies into service.");
    }


    // === Saving Methods ===

    /**
     * Saves all user data back to users.csv.
     * @param users The map of all users (NRIC -> User object).
     * @throws IOException If writing to file fails.
     */
    public void saveUsers(Map<String, User> users) throws IOException {
        List<String[]> csvData = new ArrayList<>();
        // Header defined as constant: USERS_HEADER

        for (User user : users.values()) {
            if (user == null) continue; // Safety check
            // Use getters to ensure order matches header
            csvData.add(new String[] {
                user.getNric(),
                user.getName(),
                String.valueOf(user.getAge()),
                user.getMaritalStatus(),
                user.getPassword(), 
                user.getSalt(),
                user.getRole()
            });
        }
        writeCsvFile(USERS_CSV_PATH, csvData, USERS_HEADER);
         System.out.println("User data saved.");
    }

    /**
     * Saves all project-related data (core, flats, officers, applications).
     * Takes the authoritative map of projects as input.
     * @param projects The map of all Project objects.
     * @throws IOException If any write operation fails.
     */
    public void saveAllProjectData(Map<String, Project> projects) throws IOException {
        saveProjectsCore(projects);
        saveProjectFlats(projects);
        saveProjectOfficers(projects);
        saveApplications(projects);
        System.out.println("Completed saving all project-related data.");
    }


    // --- Private helper save methods ---

    private void saveProjectsCore(Map<String, Project> projects) throws IOException {
        List<String[]> csvData = new ArrayList<>();
        // Header defined as constant: PROJECTS_HEADER

        for (Project project : projects.values()) {
             if (project == null) continue;
             try {
                csvData.add(new String[] {
                    project.getName(),
                    project.getNeighbourhood(),
                    String.valueOf(project.getVisibility()),
                    project.getCreatorName(),
                    project.getAppOpeningDate().format(DATE_FORMATTER), // Format dates
                    project.getAppClosingDate().format(DATE_FORMATTER)
                });
             } catch (NullPointerException npe) {
                  System.err.println("Error saving core data for project: " + (project.getName() != null ? project.getName() : "UNKNOWN") + ". Missing required fields (e.g., dates). Skipping.");
             }
        }
        writeCsvFile(PROJECTS_CSV_PATH, csvData, PROJECTS_HEADER);
         System.out.println("Core project data saved.");
    }

    private void saveProjectFlats(Map<String, Project> projects) throws IOException {
        List<String[]> csvData = new ArrayList<>();
        // Header defined as constant: FLATS_HEADER

        for (Project project : projects.values()) {
             if (project == null) continue;
            try {
                 if (project.getNo2Room() > 0) { // Only save if 2-room units exist
                     csvData.add(new String[] {
                         project.getName(),
                         "2-Room",
                         String.valueOf(project.getNo2Room()),
                         String.valueOf(project.getAvalNo2Room()),
                         "350000" 
                     });
                 }
                 if (project.getNo3Room() > 0) { // Only save if 3-room units exist
                     csvData.add(new String[] {
                         project.getName(),
                         "3-Room",
                         String.valueOf(project.getNo3Room()),
                         String.valueOf(project.getAvalNo3Room()),
                         "450000"
                     });
                 }
            } catch (Exception e) {
                 System.err.println("Error saving flat data for project: " + project.getName() + ". Skipping project flats.");
                 e.printStackTrace(); // For debugging
            }
        }
        writeCsvFile(PROJECT_FLATS_CSV_PATH, csvData, FLATS_HEADER);
         System.out.println("Project flat data saved.");
    }

    private void saveProjectOfficers(Map<String, Project> projects) throws IOException {
        List<String[]> csvData = new ArrayList<>();
        // Header defined as constant: OFFICERS_HEADER

        for (Project project : projects.values()) {
            if (project == null) continue;
             try {
                // Save approved officers
                List<Officer> approvedOfficers = project.getArrOfOfficers();
                if (approvedOfficers != null) {
                    for (Officer officer : approvedOfficers) {
                        if (officer != null && officer.getNric() != null) { 
                             csvData.add(new String[] {
                                 project.getName(),
                                 officer.getNric(),
                                 "Approved"
                             });
                        }
                    }
                }

                // Save pending officers
                List<Officer> pendingOfficers = project.getPendingOfficerRegistrations(); 
                 if (pendingOfficers != null) {
                     for (Officer officer : pendingOfficers) {
                         if (officer != null && officer.getNric() != null) { // Null checks
                             csvData.add(new String[] {
                                 project.getName(),
                                 officer.getNric(),
                                 "Pending"
                             });
                         }
                     }
                 }
             } catch (Exception e) {
                  System.err.println("Error saving officer data for project: " + project.getName() + ". Skipping project officers.");
                  e.printStackTrace();
             }
        }
        writeCsvFile(PROJECT_OFFICERS_CSV_PATH, csvData, OFFICERS_HEADER);
         System.out.println("Project officer assignment data saved.");
    }


    private void saveApplications(Map<String, Project> projects) throws IOException {
        List<String[]> csvData = new ArrayList<>();
        // Header defined as constant: APPLICATIONS_HEADER

        for (Project project : projects.values()) {
             if (project == null) continue;
             try {
                // **Need method in Project to get ALL applicants associated with it, regardless of list**
                List<Applicant> allProjectApplicants = project.getAllApplicants();
                if (allProjectApplicants != null) {
                    for (Applicant applicant : allProjectApplicants) {
                        if (applicant == null) continue; // Null check
                        
                         try {
                             csvData.add(new String[] {
                                 applicant.getNric(),
                                 project.getName(), // Project name from the project context
                                 applicant.getTypeFlat(),
                                 applicant.getAppStatus(),
                                 String.valueOf(applicant.getWithdrawalStatus()),
                                 String.valueOf(applicant.isApplied())
                             });
                         } catch (NullPointerException npe_app) {
                              System.err.println("Error saving application for applicant: " + (applicant.getNric() != null ? applicant.getNric() : "UNKNOWN") + " in project " + project.getName() + ". Missing required fields. Skipping application.");
                         }
                    }
                }
             } catch (Exception e) {
                  System.err.println("Error saving application data for project: " + project.getName() + ". Skipping project applications.");
                  e.printStackTrace();
             }
        }
        writeCsvFile(APPLICATIONS_CSV_PATH, csvData, APPLICATIONS_HEADER);
         System.out.println("Application data saved.");
    }

    /**
     * Saves all Enquiries and their Replies to CSV files.
     * Retrieves data from the EnquiryService.
     * @param enquiryService The service holding the enquiry data.
     * @throws IOException If saving the file fails.
     */
    public void saveEnquiries(EnquiryService enquiryService) throws IOException {
        if (enquiryService == null) {
            System.err.println("EnquiryService is null, cannot save enquiries.");
            return;
        }

        List<String[]> enquiryCsvData = new ArrayList<>();
        List<String[]> replyCsvData = new ArrayList<>();

        List<Enquiry> allEnquiries = enquiryService.getAllEnquiries(); 

        for (Enquiry enquiry : allEnquiries) {
            if (enquiry == null) continue;
            // Save enquiry data (Order matches ENQUIRIES_HEADER)
            enquiryCsvData.add(new String[] {
                String.valueOf(enquiry.getId()),
                enquiry.getApplicantNRIC(),
                enquiry.getProject(),
                enquiry.getContent() // Content for enquiry itself
            });

            // Save associated replies (Order matches REPLIES_HEADER)
            List<Reply> replies = enquiry.getReplies();
            if (replies != null) {
                for (Reply reply : replies) {
                    if (reply != null) {
                        replyCsvData.add(new String[] {
                            String.valueOf(enquiry.getId()), // Link back to enquiry
                            String.valueOf(reply.getId()),
                            reply.getResponderNRIC(),
                            reply.getContent() // Content for reply
                        });
                    }
                }
            }
        }

        writeCsvFile(ENQUIRIES_CSV_PATH, enquiryCsvData, ENQUIRIES_HEADER);
        writeCsvFile(REPLIES_CSV_PATH, replyCsvData, REPLIES_HEADER);
        System.out.println("Enquiry and Reply data saved.");
    }


    // --- Helper to escape fields for CSV writing ---
    private String escapeCsvField(String field) {
        if (field == null) return ""; // Represent null as empty string in CSV

        // If field contains comma, quote, or newline, enclose in double quotes
        // and escape existing double quotes by doubling them ("" -> """")
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        // Otherwise, return the field as is
        return field;
    }


    // === Methods to get specific filtered data (as requested by CLI before) ===
    // These now operate on the loaded data (maps/lists) passed as arguments

    /**
     * Gets a list of applicants with "Pending" status for projects created by a specific manager.
     * Assumes Project class has a method getApplicantsByStatus(String status).
     * @param managerName Manager's name.
     * @param allProjects Project map.
     * @param allUsers User map.
     * @return List of pending applicants.
     */
    public List<Applicant> getAllPendingApplicantsForManager(String managerName,
                                                             Map<String, Project> allProjects, Map<String, User> allUsers) {
        List<Applicant> pending = new ArrayList<>();
        if (managerName == null || allProjects == null) return pending; // Basic null checks

        for (Project p : allProjects.values()) {
            if (p != null && managerName.equals(p.getCreatorName())) {
                try {
                    List<Applicant> projectPending = p.getArrOfApplicants(); // Replace with actual method name
                    if (projectPending != null) {
                        pending.addAll(projectPending);
                    }
                } catch (UnsupportedOperationException e) {
                    System.err.println("Error: Project class does not support getApplicantsByStatus. Cannot filter pending applicants for " + p.getName());
                    // Handle this case - maybe return empty or throw?
                } catch (Exception e) {
                    System.err.println("Error retrieving pending applicants for project " + p.getName());
                    e.printStackTrace();
                }
            }
        }
        return pending;
    }

    /**
     * Gets a list of officers with "Pending" status for projects created by a specific manager.
     * Assumes Project class has a method getOfficersByStatus(String status) or similar.
     * @param managerName Manager's name.
     * @param allProjects All project records.
     * @param allUsers All users.
     * @return List of pending officers.
     */
    public List<Officer> getAllPendingOfficersForManager(String managerName,
                                                         Map<String, Project> allProjects, Map<String, User> allUsers) {
        List<Officer> pending = new ArrayList<>();
         if (managerName == null || allProjects == null) return pending;

        for (Project p : allProjects.values()) {
            if (p != null && managerName.equals(p.getCreatorName())) {
                // Assumes Project has a method to get *pending* officers
                // E.g., p.getPendingOfficerRegistrations() used in saving logic
                 try {
                     List<Officer> projectPending = p.getPendingOfficerRegistrations(); // Use the same method as in saving
                     if (projectPending != null) {
                         pending.addAll(projectPending);
                     }
                 } catch (UnsupportedOperationException e) {
                     System.err.println("Error: Project class does not support getPendingOfficerRegistrations. Cannot filter pending officers for " + p.getName());
                 } catch (Exception e) {
                      System.err.println("Error retrieving pending officers for project " + p.getName());
                      e.printStackTrace();
                 }
            }
        }
        return pending;
    }

    /**
     * Gets a list of applicants who have requested withdrawal for projects created by a specific manager.
     * Assumes Project class has a method getWithdrawReq().
     * @param managerName Name of manager.
     * @param allProjects Project map.
     * @param allUsers User map.
     * @return List of applicants with withdrawal requests.
     */
    public List<Applicant> getAllWithdrawalApplicantsForManager(String managerName,
                                                               Map<String, Project> allProjects, Map<String, User> allUsers) {
        List<Applicant> withdrawing = new ArrayList<>();
         if (managerName == null || allProjects == null) return withdrawing;

        for (Project p : allProjects.values()) {
            if (p != null && managerName.equals(p.getCreatorName())) {
                // Assumes Project has a method to get applicants requesting withdrawal
                 try {
                     List<Applicant> projectWithdrawals = p.getWithdrawReq(); // Assumes this method exists
                     if (projectWithdrawals != null) {
                         withdrawing.addAll(projectWithdrawals);
                     }
                 } catch (UnsupportedOperationException e) {
                      System.err.println("Error: Project class does not support getWithdrawReq. Cannot filter withdrawing applicants for " + p.getName());
                 } catch (Exception e) {
                       System.err.println("Error retrieving withdrawal applicants for project " + p.getName());
                       e.printStackTrace();
                 }
            }
        }
        return withdrawing;
    }

} 
