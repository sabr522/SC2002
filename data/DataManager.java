package data;

import Actors.User;
import Project.Project;
// Add imports for Applicant, Officer, Enquiry, etc.
import Actors.Applicant;
import Actors.Officer;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // For building relationships

public class DataManager {

    // Define file paths using the new structure
    private static final String USERS_CSV_PATH = "data_files/users.csv";
    private static final String PROJECTS_CSV_PATH = "data_files/projects.csv";
    private static final String PROJECT_FLATS_CSV_PATH = "data_files/project_flats.csv";
    private static final String PROJECT_OFFICERS_CSV_PATH = "data_files/project_officers.csv";
    private static final String APPLICATIONS_CSV_PATH = "data_files/applications.csv";
    // private static final String ENQUIRIES_CSV_PATH = "data_files/enquiries.csv"; // If needed

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    // --- Constructor ---
    public DataManager() {
        // Ensure data files exist on initialization (optional)
        ensureFileExists(USERS_CSV_PATH, "NRIC,Name,Age,MaritalStatus,PasswordHash,Role");
        ensureFileExists(PROJECTS_CSV_PATH, "ProjectName,Neighborhood,Visibility,CreatorName,AppOpeningDate,AppClosingDate");
        ensureFileExists(PROJECT_FLATS_CSV_PATH, "ProjectName,FlatType,TotalUnits,AvailableUnits,SellingPrice");
        ensureFileExists(PROJECT_OFFICERS_CSV_PATH, "ProjectName,OfficerNRIC,Status");
        ensureFileExists(APPLICATIONS_CSV_PATH, "ApplicantNRIC,ProjectName,FlatTypeApplied,ApplicationStatus,WithdrawalStatus,HasApplied");
        // ensureFileExists(ENQUIRIES_CSV_PATH, "EnquiryID,SubmitterNRIC,ProjectName,EnquiryText,ReplyText,Status");
    }

    // --- File Existence Check ---
    private void ensureFileExists(String filePath, String header) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found, creating: " + filePath);
            try {
                file.getParentFile().mkdirs(); // Ensure directory exists
                if (file.createNewFile()) {
                    // Write header to the new file
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                        bw.write(header);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file or writing header: " + filePath);
                e.printStackTrace();
            }
        }
    }

    // --- Generic CSV Reading (Simplified) ---
    private List<String[]> readCsvFile(String filePath) {
        List<String[]> data = new ArrayList<>();
        // Skip header row during processing now
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Basic CSV split - still doesn't handle quoted commas well
                    String[] values = line.split(",", -1);
                    data.add(values);
                }
            }
        } catch (FileNotFoundException e) {
             System.err.println("Error: File not found when reading: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + filePath);
            e.printStackTrace();
        }
        return data;
    }

    // --- Generic CSV Writing (Simplified) ---
    private void writeCsvFile(String filePath, List<String[]> data, String header) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            bw.write(header); // Write header every time
            bw.newLine();
            for (String[] rowData : data) {
                bw.write(String.join(",", rowData));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + filePath);
            e.printStackTrace();
        }
    }


    // === Loading Methods ===

    /**
     * Loads all users (Applicants, Officers, Managers) from users.csv.
     * E.g { "S1234567A": User(Alice Tan...), "G1234567B": User(Bob Lim...), ... }
     * @return A Map where the key is NRIC and the value is the User object.
     */
    public Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        List<String[]> csvData = readCsvFile(USERS_CSV_PATH);
        // Header: NRIC[0],Name[1],Age[2],MaritalStatus[3],PasswordHash[4],Role[5]
        for (String[] values : csvData) {
             if (values.length < 6) continue; // Basic check
             try {
                String nric = values[0];
                String name = values[1];
                int age = Integer.parseInt(values[2]);
                String maritalStatus = values[3];
                String password = values[4]; // Plain text or hash
                String role = values[5];

                // Here you could potentially instantiate specific subclasses (Applicant, Officer, Manager)
                // if they inherit from User and you load their specific data elsewhere.
                // For simplicity now, we use the base User class.
                User user = new User(name, nric, age, maritalStatus, password, role);
                users.put(nric, user); // Store by NRIC for easy lookup

             } catch (NumberFormatException e) { /* handle error */ }
             catch (Exception e) { /* handle error */ }
        }
        System.out.println("Loaded " + users.size() + " users.");
        return users;
    }

    /**
     * Loads the core project data from projects.csv.
     * Does NOT load related data like flats, officers, or applicants yet.
     * E.g { "Acacia Breeze": Project(Acacia Breeze...), "Orchid Grove": Project(Orchid Grove...), ... }
     * @return A Map where the key is ProjectName and the value is the Project object.
     */
    public Map<String, Project> loadProjectsCore() {
        Map<String, Project> projects = new HashMap<>();
        List<String[]> csvData = readCsvFi le(PROJECTS_CSV_PATH);
        // Header: ProjectName[0],Neighborhood[1],Visibility[2],CreatorName[3],AppOpeningDate[4],AppClosingDate[5]
        for (String[] values : csvData) {
            if (values.length < 6) continue;
            try {
                String projectName = values[0];
                String neighborhood = values[1];
                boolean visibility = Boolean.parseBoolean(values[2]);
                String creatorName = values[3]; // Manager's Name
                LocalDate openDate = LocalDate.parse(values[4], DATE_FORMATTER);
                LocalDate closeDate = LocalDate.parse(values[5], DATE_FORMATTER);

                // Create project with basic info - flat/officer/applicant lists are initially empty
                // Assumes a constructor like this exists, adjust as needed.
                 // Project(String name, Boolean visibility, String creatorName, String neighbourhood,
                 //         LocalDate appOpeningDate, LocalDate appClosingDate, int num2Rooms, int num3Rooms)
                 // --> We need to adapt the constructor or loading process because room numbers aren't here.
                 // Let's assume a simpler constructor for core data, and rooms are added later.
                 Project project = new Project(projectName, visibility, creatorName, neighborhood, openDate, closeDate);
                 projects.put(projectName, project);

            } catch (DateTimeParseException e) { /* handle error */ }
            catch (Exception e) { /* handle error */ }
        }
         System.out.println("Loaded " + projects.size() + " core projects.");
        return projects;
    }

    /**
      * Loads flat information and adds it to the corresponding Project objects.
      * Must be called *after* loadProjectsCore.
      * @param projects The map of projects loaded by loadProjectsCore.
      */
    public void loadProjectFlats(Map<String, Project> projects) {
        List<String[]> csvData = readCsvFile(PROJECT_FLATS_CSV_PATH);
        int flatsLoaded = 0;
        // Header: ProjectName[0],FlatType[1],TotalUnits[2],AvailableUnits[3],SellingPrice[4]
        for (String[] values : csvData) {
             if (values.length < 5) continue;
             try {
                String projectName = values[0];
                Project project = projects.get(projectName); // Find the project object
                if (project != null) {
                    String flatType = values[1];
                    int totalUnits = Integer.parseInt(values[2]);
                    int availableUnits = Integer.parseInt(values[3]);
                    // double sellingPrice = Double.parseDouble(values[4]); // If needed

                    // **Need methods in Project to set these details**
                    // Example: project.addOrUpdateFlatType(flatType, totalUnits, availableUnits);
                    // Or directly set fields if Project structure allows (e.g., for 2/3 room)
                     if ("2-Room".equalsIgnoreCase(flatType)) {
                         project.setNum2RoomUnits(totalUnits); // Assumes setter exists
                         project.setAvalNo2Room(availableUnits); // Assumes setter exists
                     } else if ("3-Room".equalsIgnoreCase(flatType)) {
                         project.setNum3RoomUnits(totalUnits); // Assumes setter exists
                         project.setAvalNo3Room(availableUnits); // Assumes setter exists
                     }
                    flatsLoaded++;
                }
             } catch (NumberFormatException e) { /* handle error */ }
             catch (Exception e) { /* handle error */ }
        }
        System.out.println("Loaded flat info for " + flatsLoaded + " entries.");
    }

     /**
      * Loads officer assignments and adds Officer objects to the corresponding Project objects.
      * Must be called *after* loadProjectsCore and loadUsers.
      * @param projects The map of projects loaded by loadProjectsCore.
      * @param users The map of users loaded by loadUsers.
      */
     public void loadProjectOfficers(Map<String, Project> projects, Map<String, User> users) {
         List<String[]> csvData = readCsvFile(PROJECT_OFFICERS_CSV_PATH);
         int assignmentsLoaded = 0;
         // Header: ProjectName[0],OfficerNRIC[1],Status[2]
         for (String[] values : csvData) {
             if (values.length < 3) continue;
             try {
                 String projectName = values[0];
                 String officerNric = values[1];
                 String status = values[2]; // "Approved" or "Pending"

                 Project project = projects.get(projectName);
                 User user = users.get(officerNric);

                 if (project != null && user instanceof Officer) { // Check if user is actually an Officer
                     Officer officer = (Officer) user;
                      // **Need methods in Project to add officers**
                      // Example: project.addOfficerToLists(officer, status);
                      // This method in Project would add to arrOfOfficers or pending list based on status
                      if ("Approved".equalsIgnoreCase(status)) {
                          project.getArrOfOfficers().add(officer); // Assumes direct access or add method
                           officer.setStatus(true); // Assuming boolean status in Officer
                      } else {
                          // project.getPendingOfficerRegistrations().add(officer); // Assumes method/list exists
                           officer.setStatus(false);
                      }
                      assignmentsLoaded++;
                 } else if (project!= null && user != null && !"Officer".equalsIgnoreCase(user.getRole())) {
                     System.err.println("Warning: User " + officerNric + " assigned to project " + projectName + " is not an Officer.");
                 }

             } catch (Exception e) { /* handle error */ }
         }
         System.out.println("Loaded " + assignmentsLoaded + " officer assignments.");
     }


     /**
      * Loads application data and adds Applicant objects to the correct lists within Project objects.
      * Must be called *after* loadProjectsCore and loadUsers.
      * @param projects Map of projects loaded by loadProjectsCore.
      * @param users Map of users loaded by loadUsers.
      */
     public void loadApplications(Map<String, Project> projects, Map<String, User> users) {
         List<String[]> csvData = readCsvFile(APPLICATIONS_CSV_PATH);
         int appsLoaded = 0;
         // Header: ApplicantNRIC[0],ProjectName[1],FlatTypeApplied[2],ApplicationStatus[3],WithdrawalStatus[4],HasApplied[5]
         for (String[] values : csvData) {
             if (values.length < 6) continue;
             try {
                 String applicantNric = values[0];
                 String projectName = values[1];
                 String flatTypeApplied = values[2];
                 String appStatus = values[3]; // "Pending", "Successful", "Unsuccessful", "Withdrawn", "Booked"
                 boolean withdrawalStatus = Boolean.parseBoolean(values[4]); // True if withdrawal accepted/pending? Check meaning
                 boolean hasApplied = Boolean.parseBoolean(values[5]); // If they submitted

                 Project project = projects.get(projectName);
                 User user = users.get(applicantNric);

                 if (project != null && user instanceof Applicant) {
                     Applicant applicant = (Applicant) user;

                     // Update applicant object state based on CSV
                     applicant.setProject(project); // Assumes setter exists
                     applicant.setTypeFlat(flatTypeApplied); // Assumes setter exists
                     applicant.setAppStatus(appStatus); // Assumes setter exists
                     applicant.setWithdrawalStatus(withdrawalStatus); // Assumes setter exists
                     applicant.setApplied(hasApplied); // Assumes setter exists

                     // Add applicant to the correct list within the project based on status
                     project.addApplicantToCorrectList(applicant); // **Need this logic in Project**
                     appsLoaded++;

                 } else if (project!= null && user != null && !"Applicant".equalsIgnoreCase(user.getRole())) {
                      System.err.println("Warning: User " + applicantNric + " applying to project " + projectName + " is not an Applicant.");
                 }
             } catch (Exception e) { /* handle error */ }
         }
          System.out.println("Loaded " + appsLoaded + " applications.");
     }

    // --- Add loadEnquiries if needed ---


    // === Saving Methods ===

    /**
     * Saves all user data back to users.csv.
     * @param users The map of all users (NRIC -> User object).
     */
    public void saveUsers(Map<String, User> users) {
        List<String[]> csvData = new ArrayList<>();
        String header = "NRIC,Name,Age,MaritalStatus,PasswordHash,Role";
        for (User user : users.values()) {
            // Use User.toCsvString() or similar, making sure order matches header
             csvData.add(new String[] {
                 escapeCsvField(user.getNric()),
                 escapeCsvField(user.getName()),
                 String.valueOf(user.getAge()),
                 escapeCsvField(user.getMaritalStatus()),
                 escapeCsvField(user.getPassword()), // Plain text or hash
                 escapeCsvField(user.getRole())
             });
        }
        writeCsvFile(USERS_CSV_PATH, csvData, header);
    }

    /**
     * Saves core project data, flat data, officer assignments, and application data.
     * Takes the authoritative list of projects as input.
     * @param projects The list (or map) of all Project objects.
     */
    public void saveAllProjectData(Map<String, Project> projects) {
        saveProjectsCore(projects);
        saveProjectFlats(projects);
        saveProjectOfficers(projects);
        saveApplications(projects); // Assumes Project objects contain all linked Applicants
        System.out.println("Completed saving all project-related data.");
    }


    // --- Private helper save methods ---

    private void saveProjectsCore(Map<String, Project> projects) {
        List<String[]> csvData = new ArrayList<>();
        String header = "ProjectName,Neighborhood,Visibility,CreatorName,AppOpeningDate,AppClosingDate";
        for (Project project : projects.values()) {
             csvData.add(new String[] {
                 escapeCsvField(project.getName()),
                 escapeCsvField(project.getNeighbourhood()),
                 String.valueOf(project.getVisibility()),
                 escapeCsvField(project.getCreatorName()),
                 project.getAppOpeningDate().format(DATE_FORMATTER),
                 project.getAppClosingDate().format(DATE_FORMATTER)
             });
        }
        writeCsvFile(PROJECTS_CSV_PATH, csvData, header);
    }

    private void saveProjectFlats(Map<String, Project> projects) {
        List<String[]> csvData = new ArrayList<>();
        String header = "ProjectName,FlatType,TotalUnits,AvailableUnits,SellingPrice";
        for (Project project : projects.values()) {
             // **Need logic based on how Project stores flat info**
             // Example assumes specific 2/3 room getters:
             if (project.getNum2RoomUnits() > 0) { // Only save if exists
                 csvData.add(new String[] {
                     escapeCsvField(project.getName()),
                     "2-Room",
                     String.valueOf(project.getNum2RoomUnits()), // Total
                     String.valueOf(project.getAvalNo2Room()), // Available
                     "350000" // Need getter for price
                 });
             }
              if (project.getNum3RoomUnits() > 0) { // Only save if exists
                 csvData.add(new String[] {
                     escapeCsvField(project.getName()),
                     "3-Room",
                     String.valueOf(project.getNum3RoomUnits()), // Total
                     String.valueOf(project.getAvalNo3Room()), // Available
                     "450000" // Need getter for price
                 });
             }
        }
        writeCsvFile(PROJECT_FLATS_CSV_PATH, csvData, header);
    }

    private void saveProjectOfficers(Map<String, Project> projects) {
        List<String[]> csvData = new ArrayList<>();
        String header = "ProjectName,OfficerNRIC,Status";
        for (Project project : projects.values()) {
            // Save approved officers
             for (Officer officer : project.getArrOfOfficers()) { // Assumes getter exists
                 csvData.add(new String[] {
                     escapeCsvField(project.getName()),
                     escapeCsvField(officer.getNric()),
                     "Approved"
                 });
             }
             // Save pending officers
              // for (Officer officer : project.getPendingOfficerRegistrations()) { // Assumes getter exists
              //    csvData.add(new String[] {
              //        escapeCsvField(project.getName()),
              //        escapeCsvField(officer.getNric()),
              //        "Pending"
              //    });
              // }
        }
        writeCsvFile(PROJECT_OFFICERS_CSV_PATH, csvData, header);
    }

     private void saveApplications(Map<String, Project> projects) {
         List<String[]> csvData = new ArrayList<>();
         String header = "ApplicantNRIC,ProjectName,FlatTypeApplied,ApplicationStatus,WithdrawalStatus,HasApplied";
          // Iterate through all projects and ALL applicant lists within them
         for (Project project : projects.values()) {
              // **Need method in Project to get ALL applicants associated with it, regardless of list**
              // Or iterate through each list (pending, successful, unsuccessful, booked, withdrawRequests)
             List<Applicant> allProjectApplicants = project.getAllApplicants(); // Assumes this method exists

             for (Applicant applicant : allProjectApplicants) {
                  csvData.add(new String[] {
                      escapeCsvField(applicant.getNric()),
                      escapeCsvField(project.getName()), // Project name from the project context
                      escapeCsvField(applicant.getTypeFlat()),
                      escapeCsvField(applicant.getAppStatus()),
                      String.valueOf(applicant.getWithdrawalStatus()),
                      String.valueOf(applicant.getApplied())
                  });
             }
         }
         writeCsvFile(APPLICATIONS_CSV_PATH, csvData, header);
     }


    // --- Add saveEnquiries if needed ---


    // --- Helper to escape fields for CSV writing ---
    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

     // --- Methods to get specific filtered data (as requested by CLI before) ---
     // These now need to operate on the loaded data (maps/lists)

     public List<Applicant> getAllPendingApplicantsForManager(String managerName, Map<String, Project> allProjects, Map<String, User> allUsers) {
          List<Applicant> pending = new ArrayList<>();
          for (Project p : allProjects.values()) {
              if (managerName.equals(p.getCreatorName())) {
                   // Assumes Project has a method to get applicants in "Pending" state
                   pending.addAll(p.getApplicantsByStatus("Pending"));
              }
          }
          return pending;
     }

      public List<Officer> getAllPendingOfficersForManager(String managerName, Map<String, Project> allProjects, Map<String, User> allUsers) {
           List<Officer> pending = new ArrayList<>();
           for (Project p : allProjects.values()) {
                if (managerName.equals(p.getCreatorName())) {
                     // Assumes Project has a method to get officers in "Pending" state
                     pending.addAll(p.getOfficersByStatus("Pending"));
                }
           }
           return pending;
      }

      public List<Applicant> getAllWithdrawalApplicantsForManager(String managerName, Map<String, Project> allProjects, Map<String, User> allUsers) {
          List<Applicant> withdrawing = new ArrayList<>();
           for (Project p : allProjects.values()) {
                if (managerName.equals(p.getCreatorName())) {
                    withdrawing.addAll(p.getWithdrawReq());
                }
           }
           return withdrawing;
      }


}