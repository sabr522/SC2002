package Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Actors.Applicant;
import Actors.Officer;

public class Project {
	
	private String name;
	private String visibility;
	private String creatorName;
	private String neighbourhood;
	private LocalDate appOpeningDate;
	private LocalDate appClosingDate;
	private List<Officer> arrOfOfficers = new ArrayList<>();
	
	private int no2Room;
	private int no3Room;
	private int avalNo2Room;
	private int avalNo3Room;
	
	private List<Applicant> arrOfApplicants = new ArrayList<>(); //pending applicants (not successful or unsuccessful)
	private List<Applicant> successfulApplicants = new ArrayList<>(); //all successful applicants who haven't booked yet or haven't withdrawn yet
	private List<Applicant> unsuccessfulApplicants = new ArrayList<>(); //all unsuccessful applicants (initially unsuccessful or successful and then withdraw)
	private List<Applicant> bookedApplicants = new ArrayList<>(); //all successful applicants who have booked
	private List<Applicant> withdrawRequests = new ArrayList<>(); //all successful applicants who apply for withdrawal but don't have withdrawal decision yet
	
	static private ArrayList<Project> allProjects = new ArrayList<>();
	private boolean updatedInAllProjects = false;
	
	
	//Constructor
	public Project(String name, String visibility, String creatorName, String neighbourhood, LocalDate appOpeningDate, LocalDate appClosingDate, int no2Room, int no3Room ) {
		
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
	
	public String getVisibility() {
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
	
	public LocalDate getAppCloseingDate() {
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
	public List<Officer> getArrOfOfficers(){
		return this.arrOfOfficers;
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
	public void setName(String name) {
		this.name=name;
	}
	
	public void setCreatorName(String creatorName) {
		this.creatorName=creatorName;
	}
	
	public void setVisibility(String visibility) {
		this.visibility=visibility;
	}
	
	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood=neighbourhood;
	}
	
	public void setAppOpeningDate(LocalDate appOpeningDate) {
		this.appOpeningDate=appOpeningDate;
	}
	
	public void setAppClosingDate(LocalDate appClosingDate) {
		this.appClosingDate=appClosingDate;
	}
	
	public void setNo2Room(int no2Room) {
		this.no2Room=no2Room;
	}
	
	public void setNo3Room(int no3Room) {
		this.no3Room=no3Room;
	}
	
	
	public void updateArrOfOfficers(Officer officer) {
		this.arrOfOfficers.add(officer);
	}
	
	public void updateArrOfApplicants(Applicant applicant) {
		this.arrOfApplicants.add(applicant);
	}
	
	public void updateSuccessfulApplicants(Applicant applicant) {
		this.successfulApplicants.add(applicant);
	}
	
	public void updateUnsuccessfulApplicants(Applicant applicant) {
		this.unsuccessfulApplicants.add(applicant);
	}
	
	public void updateBookedApplicants(Applicant applicant) {
		String targetNRIC=applicant.getNRIC();
		if (successfulApplicants.removeIf(a -> a.getNRIC().equals(targetNRIC)))
		{
			this.bookedApplicants.add(applicant);
		}	
	}
	
	
	public void updateWithdrawRequests(Applicant applicant) {
		String targetNRIC=applicant.getNRIC();
		if (successfulApplicants.removeIf(a -> a.getNRIC().equals(targetNRIC)) || bookedApplicants.removeIf(a -> a.getNRIC().equals(targetNRIC)))
		{
			this.withdrawRequests.add(applicant);
		}	
	}
	
	public void updateWithdrawToUnsuccessful(Applicant applicant) {
		String targetNRIC=applicant.getNRIC();
		if (withdrawRequests.removeIf(a -> a.getNRIC().equals(targetNRIC)))
		{
			this.unsuccessfulApplicants.add(applicant);
		}	
	}
	
	
	
	
}
