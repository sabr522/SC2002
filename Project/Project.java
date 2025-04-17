package Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Actors.Applicant;
import Actors.Officer; 

public class Project {
	
	private String name;
	private Boolean visibility;
	private String creatorName;
	private String neighbourhood;
	private LocalDate appOpeningDate;
	private LocalDate appClosingDate;
	private List<Officer> arrOfPendingOfficers = new ArrayList<>();
	private List<Officer> arrOfOfficers = new ArrayList<>();
	
	private int no2Room;
	private int no3Room;
	private int avalNo2Room;
	private int avalNo3Room;
	
	List<Applicant> allApplicants = new ArrayList<>(); // all applicants
	private List<Applicant> arrOfApplicants = new ArrayList<>(); //pending applicants (not successful or unsuccessful)
	private List<Applicant> successfulApplicants = new ArrayList<>(); //all successful applicants who haven't booked yet or haven't withdrawn yet
	private List<Applicant> unsuccessfulApplicants = new ArrayList<>(); //all unsuccessful applicants (initially unsuccessful or successful and then withdraw)
	private List<Applicant> bookedApplicants = new ArrayList<>(); //all successful applicants who have booked
	private List<Applicant> withdrawRequests = new ArrayList<>(); //all successful applicants who apply for withdrawal but don't have withdrawal decision yet
	
	static private ArrayList<Project> allProjects = new ArrayList<>();
	private boolean updatedInAllProjects = false;
	
	
	//Constructor
	public Project(String name, Boolean visibility, String creatorName, String neighbourhood, LocalDate appOpeningDate, LocalDate appClosingDate, int no2Room, int no3Room ) {
		
		this.name=name;
		this.visibility=visibility;
		this.creatorName=creatorName;
		this.neighbourhood=neighbourhood;
		this.appOpeningDate=appOpeningDate;
		this.appClosingDate=appClosingDate;
		this.no2Room=no2Room;
		this.no3Room=no3Room;
		this.avalNo2Room=this.no2Room;
		this.avalNo3Room=this.no3Room;
		
	}
	
	//Adds project to static array containing all projects
	public static void updateAllProjects(Project project) {
		if (project.updatedInAllProjects==false)
		allProjects.add(project);
		project.updatedInAllProjects=true;
	}
	
	//Getter Methods
	public String getName() {
		return this.name;
	}
	
	public Boolean getVisibility() {
		return this.visibility;
	}
	
	public String getCreatorName() {
		return this.creatorName;
	}
	
	public String getNeighbourhood() {
		return this.neighbourhood;
	}
	
	public LocalDate getAppOpeningDate() {
		return this.appOpeningDate;
	}
	
	public LocalDate getAppClosingDate() {
		return this.appClosingDate;
	}
	
	public int getNo2Room() {
		return this.no2Room;
	}
	
	public int getNo3Room() {
		return this.no3Room;
	}
	
	public int getAvalNo2Room() {
		return this.avalNo2Room;
	}
	
	public int getAvalNo3Room() {
		return this.avalNo3Room;
	}

	public List<Officer> getPendingOfficerRegistrations(){
		return this.arrOfPendingOfficers;
	}

	public List<Officer> getArrOfOfficers(){
		return this.arrOfOfficers;
	}
	public List<Applicant> getAllApplicants() {
	
		// Add pending applicants
		if (getArrOfApplicants() != null) {
			allApplicants.addAll(getArrOfApplicants());
		}
	
		// Add successful applicants
		if (getSuccessfulApplicants() != null) {
			allApplicants.addAll(getSuccessfulApplicants());
		}
	
		// Add unsuccessful applicants
		if (getUnsuccessfulApplicants() != null) {
			allApplicants.addAll(getUnsuccessfulApplicants());
		}
	
		return allApplicants;
	}
	
	public List<Applicant> getArrOfApplicants(){
		return this.arrOfApplicants;
	}
	
	
	public List<Applicant> getSuccessfulApplicants(){
		return this.successfulApplicants;
	}
	
	
	public List<Applicant> getUnsuccessfulApplicants(){
		return this.unsuccessfulApplicants;
	}
	
	
	public List<Applicant> getBookedApplicants(){
		return this.bookedApplicants;
	}
	
	
	public List<Applicant> getWithdrawReq(){
		return this.withdrawRequests;
	}
	
	
	public static List<Project> getAllProjects(){
		return allProjects;
	}
	
	//Prints details
	public void viewAllDetails() {
		System.out.println("Project Name: " + this.name);
		System.out.println("Manager Name: " + this.creatorName);
		System.out.println("Visibility: " + this.visibility);
		System.out.println("Neighbourhood: " + this.neighbourhood);
		System.out.println("Application Opening: " + this.appOpeningDate);
		System.out.println("Application Closing: " + this.appClosingDate);
		System.out.println("Officers: " + this.arrOfOfficers);
		System.out.println("Number of 2-Room: " + this.no2Room);
		System.out.println("Number of 3-Room: " + this.no3Room);
	
	}
	
	
	
	//Setter Methods
	public void setName(String creatorName, String name) {
		if (creatorName.equals(this.creatorName)){
			this.name=name;
		}
		else{
			System.out.println("Unauthorised access!");
		}
		
	}
	
	public void setCreatorName(String creatorName) {
		this.creatorName=creatorName;
	}
	
	public void setVisibility(String creatorName, Boolean visibility) {
		if (creatorName.equals(this.creatorName)){
			this.visibility=visibility;
		}
		else{
			System.out.println("Unauthorised access!");
		}
	}
	
	public void setNeighbourhood(String creatorName, String neighbourhood) {
		if (creatorName.equals(this.creatorName)){
			this.neighbourhood=neighbourhood;
		}
		else{
			System.out.println("Unauthorised access!");
		}
	}
	
	public void setAppOpeningDate(String creatorName, LocalDate appOpeningDate) {
		if (creatorName.equals(this.creatorName)){
			this.appOpeningDate=appOpeningDate;
		}
		else{
			System.out.println("Unauthorised access!");
		}	
	}
	
	public void setAppClosingDate(String creatorName, LocalDate appClosingDate) {
		if (creatorName.equals(this.creatorName)){
			this.appClosingDate=appClosingDate;
		}
		else{
			System.out.println("Unauthorised access!");
		}
	}
	
	public void setAvalNo2Room(int avalNo2Room) {
			this.avalNo2Room=avalNo2Room;
	}
	
	public void setAvalNo3Room(int avalNo3Room) {
			this.avalNo3Room=avalNo3Room;
	}
	
	public void setNo2Room(String creatorName, int no2Room) {
		if (creatorName.equals(this.creatorName)){
			this.no2Room=no2Room;
		}
		else{
			System.out.println("Unauthorised access!");
		}
	}
	
	public void setNo3Room(String creatorName, int no3Room) {
		if (creatorName.equals(this.creatorName)){
			this.no3Room=no3Room;
		}
		else{
			System.out.println("Unauthorised access!");
		}
	}
	
	public void updateArrOfPendingOfficers (Officer officer) {
		this.arrOfPendingOfficers.add(officer);
	}

	public void updateArrOfOfficers(String creatorName, Officer officer) {
		if (creatorName.equals(this.creatorName)){
			if (this.arrOfPendingOfficers.remove(officer))
			{
				if (arrOfOfficers.size() < 10) {
					this.arrOfOfficers.add(officer);
				} else {
					System.out.println("Cannot add more officers. Maximum reached.");
				}
				
			}
			else{System.out.println("Officer does not exist.");}
			
		}
		else{
			System.out.println("Unauthorised access!");
		}
	}
	
	public void addApplicantToCorrectList(Applicant applicant){
		switch(applicant.getAppStatus()){
			case "Pending":
			this.arrOfApplicants.add(applicant);
			break;

			case "Successful":
			this.successfulApplicants.add(applicant);
			break;

			case "Unsuccessful":
			this.unsuccessfulApplicants.add(applicant);
			break;

			case "Withdrawn":
			this.withdrawRequests.add(applicant);
			break;

			case "Booked":
			this.bookedApplicants.add(applicant);
			break;
		}
	}


	public void updateArrOfApplicants(Applicant applicant) {
			this.arrOfApplicants.add(applicant);
	}
	
	public void updateSuccessfulApplicants(Applicant applicant) {
		if (applicant != null && applicant.getNric() != null) { 
			String targetNRIC=applicant.getNric();
			if (arrOfApplicants.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.successfulApplicants.add(applicant);
			}
		}	
	}
	
	public void updateUnsuccessfulApplicants(Applicant applicant) {
		if (applicant != null && applicant.getNric() != null) { 
			String targetNRIC=applicant.getNric();
			if (arrOfApplicants.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.unsuccessfulApplicants.add(applicant);
			}
		}		
	}
	
	public void updateBookedApplicants(Applicant applicant) {
		if (applicant != null && applicant.getNric() != null) { 
			String targetNRIC=applicant.getNric();
			if (successfulApplicants.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.bookedApplicants.add(applicant);
				if (applicant.getTypeFlat().equals("2-Room")){
					if (this.avalNo2Room>0){
						this.avalNo2Room -=1;
					}
					else{
						System.out.println("Unsuccessful Booking. No remaining 2 Room Flats.");
					}
						
				}
				else{
					if (this.avalNo3Room>0){
						this.avalNo3Room -=1;
					}
					else{
						System.out.println("Unsuccessful Booking. No remaining 3 Room Flats.");
					}

					
				}
			}
		}	
	}
	
	
	public void updateWithdrawRequests(Applicant applicant) {
		if (applicant != null && applicant.getNric() != null) { 
			String targetNRIC=applicant.getNric();
			if (successfulApplicants.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.withdrawRequests.add(applicant);
			}

			else if (bookedApplicants.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.withdrawRequests.add(applicant);
				if (applicant.getTypeFlat().equals("2Room")){
					this.avalNo2Room +=1;
				}
				else{
					this.avalNo3Room +=1;
				}
			}
		}	
	}
	
	public void updateWithdrawToUnsuccessful(Applicant applicant) {
		if (applicant != null && applicant.getNric() != null) { 
			String targetNRIC=applicant.getNric();
			if (withdrawRequests.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.unsuccessfulApplicants.add(applicant);
			}	
		}
	}

	// checks if 2 projects are in same period
	public boolean isClashing(LocalDate start1, LocalDate end1) {
		return !(end1.isBefore(this.appClosingDate) || start1.isAfter(this.appOpeningDate));
	}	

}
