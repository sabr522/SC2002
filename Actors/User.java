package Actors; 

import java.util.Objects;

/**
 * Abstract base class representing a user in the system.
 * Contains common attributes shared by Applicants, Officers, and Managers.
 * Cannot be instantiated directly; subclasses must be used.
 */
public abstract class User {

    // Protected fields accessible by subclasses (Applicant, Officer, Manager)
    protected String name;
    protected String nric;
    protected int age;
    protected String maritalStatus;
    protected String password; 
    protected String role;

    /**
     * Constructor for subclasses to initialize common user attributes.
     *
     * @param name          The user's name.
     * @param nric          The user's NRIC (unique identifier).
     * @param age           The user's age.
     * @param maritalStatus The user's marital status.
     * @param password      The user's password.
     * @param role          The user's role ("Applicant", "Officer", "Manager").
     */
    protected User(String name, String nric, int age, String maritalStatus, String password, String role) {
        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password; 
        this.role = role;
    }

    // --- Public Getters ---
    /** 
     * Returns the name of the user.
     * @return User's name */
    public String getName() {
        return name;
    }

    /** 
     * Return the NRIC of the user.
     * @return User's NRIC */
    public String getNric() {
        return nric;
    }

    /** 
     * Returns the age of the user.
     * @return User's age */
    public int getAge() {
        return age;
    }

    /** 
     * Returns the marital status of the user.
     * @return User's marital status */
    public String getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Gets the user's password.
     * @return The user's plain text password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the role assigned to the user (e.g., Applicant, Officer, Manager). 
     * @return User's role */
    public String getRole() {
        return role;
    }

    // --- Public Setter (Only for Password, as required by change password logic) ---

    /**
     * Sets the user's password.
     * @param newPassword The new plain text password.
     */
    public void setPassword(String newPassword) {
        this.password = newPassword; // Store directly
    }

    // --- Abstract method ---
    // If all user types MUST provide a specific behavior, declare it abstract.

    // --- Common methods  ---

    /**
     * Generates a CSV-formatted string for this user's data.
     * Matches the header: NRIC,Name,Age,MaritalStatus,Password,Role
     * @return A string formatted for CSV output.
     */
    public String toCsvString() {
        return String.join(",",
                escapeCsvField(nric),
                escapeCsvField(name),
                String.valueOf(age),
                escapeCsvField(maritalStatus),
                escapeCsvField(password),
                escapeCsvField(role)
        );
    }

    /**
     * Helper to handle potential commas or quotes in fields for CSV saving.
     * @param field Input field
     * @return Escaped field
     */  
    protected String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Enclose in double quotes and escape existing double quotes
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Returns a string representation of the user object.
     * Exclude password from default toString for security/privacy.
     * @return User summary
     */
    @Override
    public String toString() {
        // Exclude password from default toString for security/privacy
        return getClass().getSimpleName() + "{" + // Show actual subclass name
               "name='" + name + '\'' +
               ", nric='" + nric + '\'' +
               ", age=" + age +
               ", maritalStatus='" + maritalStatus + '\'' +
               ", role='" + role + '\'' +
               '}';
    }

    /**
     * Compares users by NRIC (unique identifier).
     * @param o Object to compare
     * @return true if NRICs match
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false; // Compare actual class types
        User user = (User) o;
        return Objects.equals(nric, user.nric); // NRIC should be unique
    }

    /**
     * Generates hash code based on NRIC (unique identifier).
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(nric); // NRIC is the unique identifier
    }
}
