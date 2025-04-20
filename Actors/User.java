package Actors; 

import java.util.Objects;
import java.security.SecureRandom;
import java.util.Base64;

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
    protected String salt; 
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
        this.salt = generateSalt();
        String passwordToHash = (password == null || password.isEmpty()) ? "password" : password;
        this.password = hashPassword(passwordToHash, this.salt);
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
     * @return The user's hashed password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets salt for password
     * @return salt of password
     */
    public String getSalt() { 
        return salt; 
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
        if (newPassword == null || newPassword.isEmpty()) {
            System.err.println("WARN: Attempted to set an empty password. Ignoring.");
            return; 
       }
       // Generate a new salt EVERY time the password changes
       this.salt = generateSalt();
       this.password = hashPassword(newPassword, this.salt);
    }

    
    // --- Password Hashing and Salt Generation (Add these methods) ---
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16]; // 16 bytes = 128 bits, common salt size
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes); // Encode salt for storage
    }

    public static String hashPassword(String plainPassword, String salt) {
        if (plainPassword == null || salt == null) return null; // Handle null inputs
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            // Decode salt from Base64 and prepend/append to password before hashing
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            md.update(saltBytes); // Add salt first
            byte[] passwordBytes = plainPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] hashedBytes = md.digest(passwordBytes); // Hash password+salt
            return Base64.getEncoder().encodeToString(hashedBytes); // Encode hash for storage
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println("FATAL: SHA-256 Hashing Algorithm Not Found!");
            throw new RuntimeException("Password hashing failed.", e); 
        } catch (IllegalArgumentException e) {
             System.err.println("ERROR: Invalid Base64 salt string encountered during hashing.");
             return null;
        }
    }

    // Helper to verify password during login
    public boolean verifyPassword(String plainPassword) {
        if (plainPassword == null || this.password == null || this.salt == null) return false;
        String hashOfEnteredPassword = hashPassword(plainPassword, this.salt);
        boolean match = this.password.equals(hashOfEnteredPassword);
        return match;
    }

        /**
     * Used by DataManager during loading to set the stored hash and salt
     * directly, bypassing the normal setPassword hashing logic.
     * Should only be called during the loading process.
     * @param loadedHash The password hash read from storage.
     * @param loadedSalt The salt read from storage.
     */
    public void loadCredentials(String loadedHash, String loadedSalt) {
        if (loadedHash != null && loadedSalt != null) {
             this.password = loadedHash;
             this.salt = loadedSalt;
        } else {
             System.err.println("WARN: Missing hash or salt during credential loading for user " + this.nric + ". Password may not work.");
        }
    }

    // --- Abstract method ---
    // None for now

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
