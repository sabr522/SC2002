package Actors;

public class User {
    private String name;
    private String nric;
    private int age;
    private String maritalStatus;
    private String password; 
    private String role;     // e.g., "Applicant", "Officer", "Manager"

    // Constructor
    public User(String name, String nric, int age, String maritalStatus, String password, String role) {
        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password; 
        this.role = role;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getNric() {
        return nric;
    }

    public int getAge() {
        return age;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public String getPassword() {
        // maybe do hashing?
        return password;
    }

    public String getRole() {
        return role;
    }

    // Setter for password (needed for changePassword functionality)
    public void setPassword(String newPassword) {
        // hash the newPassword before storing?
        this.password = newPassword;
    }

    @Override
    public String toString() {
        return "User{" +
               "name='" + name + '\'' +
               ", nric='" + nric + '\'' +
               ", age=" + age +
               ", maritalStatus='" + maritalStatus + '\'' +
               ", role='" + role + '\'' +
               '}';
    }

    // Optional: Method to generate the CSV row string for saving
    public String toCsvString() {
        // Format: Name,NRIC,Age,Marital Status,Password
        return String.join(",",
                escapeCsvField(name),
                escapeCsvField(nric),
                String.valueOf(age),
                escapeCsvField(maritalStatus),
                escapeCsvField(password)
        );
    }

    // Helper to handle potential commas or quotes in fields for CSV saving
    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Enclose in double quotes and escape existing double quotes
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}