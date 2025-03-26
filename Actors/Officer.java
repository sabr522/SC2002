package Actors;

public class Officer implements ApplicantRole {
    private String citizenApplication;
    private String officerApplication;
    private boolean status;
    private boolean booked = false;
    private List<String> enquiries = new ArrayList<>();
    private List<Project> appliedProjects = new ArrayList<>();

    public Officer(String citizenApplication, String officerApplication, boolean status) {
        this.citizenApplication = citizenApplication;
        this.officerApplication = officerApplication;
        this.status = status;
    }

    public String getCitizenApplication() {
        return citizenApplication;
    }

    public void setCitizenApplication(String citizenApplication) {
        this.citizenApplication = citizenApplication;
    }

    public String getOfficerApplication() {
        return officerApplication;
    }

    public void setOfficerApplication(String officerApplication) {
        this.officerApplication = officerApplication;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
