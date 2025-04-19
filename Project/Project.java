package Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	private List<Applicant> arrOfApplicants = new ArrayList<>(); //pending applicants (not successful or unsuccessful)
	private List<Applicant> successfulApplicants = new ArrayList<>(); //all successful applicants who haven't booked yet or haven't withdrawn yet
	private List<Applicant> unsuccessfulApplicants = new ArrayList<>(); //all unsuccessful applicants (initially unsuccessful or successful and then withdraw)
	private List<Applicant> bookedApplicants = new ArrayList<>(); //all successful applicants who have booked
	private List<Applicant> withdrawRequests = new ArrayList<>(); //all successful applicants who apply for withdrawal but don't have withdrawal decision yet
	

	
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
		return new ArrayList<>(this.arrOfPendingOfficers);
	}

	public List<Officer> getArrOfOfficers(){
		return new ArrayList<>(this.arrOfOfficers);
	}
	public List<Applicant> getAllApplicants() {
		Set<Applicant> uniqueApplicants = new HashSet<>(); 

		// Add pending applicants
		if (getArrOfApplicants() != null) {
			uniqueApplicants.addAll(getArrOfApplicants());
		}
	
		// Add successful applicants
		if (getSuccessfulApplicants() != null) {
			uniqueApplicants.addAll(getSuccessfulApplicants());
		}
	
		// Add unsuccessful applicants
		if (getUnsuccessfulApplicants() != null) {
			uniqueApplicants.addAll(getUnsuccessfulApplicants());
		}

		// Add booked
		if (this.bookedApplicants != null) {      
			uniqueApplicants.addAll(this.bookedApplicants);
		}

		// Add withdrawn
		if (this.withdrawRequests != null) {         
				uniqueApplicants.addAll(this.withdrawRequests);
		}
	
		return new ArrayList<>(uniqueApplicants);
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
	

	
	//Prints details
	public void viewAllDetails(boolean isStaff) {
		System.out.println("Project Name: " + this.name);
		System.out.println("Manager Name: " + this.creatorName);
		if (isStaff) System.out.println("Visibility: " + this.visibility);
		System.out.println("Neighbourhood: " + this.neighbourhood);
		System.out.println("Application Opening: " + this.appOpeningDate);
		System.out.println("Application Closing: " + this.appClosingDate);
		if (isStaff) {
			System.out.println("Officers:");
			for (Officer o : arrOfOfficers) {
				System.out.println("- " + o.getName());
			}
		}
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
	
	public boolean updateArrOfPendingOfficers (Officer officer) {
     if (officer != null && !this.arrOfPendingOfficers.contains(officer)) {
          return this.arrOfPendingOfficers.add(officer);
     }
     return false; 
	}

	/**
	 * Adds an Officer to the approved list for this project, removing them
	 * from the pending list if present. Checks authorization and limits.
	 * Designed to work correctly both during loading and runtime approval.
	 * @param creatorName The NRIC/Name of the manager attempting the action (for auth check during runtime).
	 * @param officer The Officer to approve/add.
	 * @return true if the officer is successfully in the approved list, false otherwise.
	 */
	public boolean updateArrOfOfficers(String creatorName, Officer officer) {
		// Authorization check
		if (!creatorName.equals(this.creatorName)) {
			System.out.println("Unauthorised access to update officers for project " + this.name);
			return false;
		}
		if (officer == null) {
			System.out.println("Cannot add null officer to approved list.");
			return false;
		}

		// 1. Remove from pending list 
		boolean wasPending = this.arrOfPendingOfficers.removeIf(p -> p != null && p.equals(officer));

		// 2. Check if already approved
		if (this.arrOfOfficers.contains(officer)) {
			return true; 
		}

		// 3. Check limit
		int MAX_OFFICERS = 10; // Define limit
		if (this.arrOfOfficers.size() >= MAX_OFFICERS) {
			System.out.println("Cannot add officer " + officer.getNric() + ". Maximum officer limit (" + MAX_OFFICERS + ") reached for project " + this.name + ".");
			return false; // Failed due to limit
		}

		// 4. Add to approved list
		if (this.arrOfOfficers.add(officer)) {
			return true;
		} else {
			System.err.println("ERROR: Failed to add officer " + officer.getNric() + " to approved list for unknown reason.");
			if(wasPending) this.arrOfPendingOfficers.add(officer);
			return false;
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
	
	public boolean updateBookedApplicants(Applicant applicant) {
		if (applicant != null && applicant.getNric() != null) { 
			String targetNRIC=applicant.getNric();
			if (successfulApplicants.removeIf(a -> a.getNric().equals(targetNRIC)))
			{
				this.bookedApplicants.add(applicant);
				if (applicant.getTypeFlat().equals("2-Room")){
					if (this.avalNo2Room>0){
						this.avalNo2Room -=1;
						return true;
					}
					else{
						System.out.println("Unsuccessful Booking. No remaining 2 Room Flats.");
						return false;
					}
						
				}
				else{
					if (this.avalNo3Room>0){
						this.avalNo3Room -=1;
						return true;
					}
					else{
						System.out.println("Unsuccessful Booking. No remaining 3 Room Flats.");
						return false;
					}
				}
			}
		}	
		return false;
	}
	
	
	public boolean updateWithdrawRequests(Applicant applicant) {
        if (applicant == null || applicant.getNric() == null) {
            System.err.println("WARN: updateWithdrawRequests called with null applicant or NRIC.");
            return false;
        }
        String targetNRIC = applicant.getNric();

        // Check if already requested withdrawal
        if (this.withdrawRequests != null && this.withdrawRequests.contains(applicant)){
            return true; 
        }

        boolean moved = false;

        // Try removing from Successful list
        if (this.successfulApplicants != null && this.successfulApplicants.removeIf(a -> a != null && targetNRIC.equals(a.getNric()))) {
            moved = true;
        }
        // Try removing from Booked list (only if not found in successful)
        else if (this.bookedApplicants != null && this.bookedApplicants.removeIf(a -> a != null && targetNRIC.equals(a.getNric()))) {
             moved = true;
             String flatType = applicant.getTypeFlat();
              if (flatType != null) {
                 if ("2-Room".equals(flatType)){
                     this.avalNo2Room +=1;
                      System.out.println("DEBUG: Incremented avalNo2Room to " + this.avalNo2Room + " due to booked withdrawal request.");
                 }
                 else if ("3-Room".equals(flatType)){
                     this.avalNo3Room +=1;
                     System.out.println("DEBUG: Incremented avalNo3Room to " + this.avalNo3Room + " due to booked withdrawal request.");
                 } else {
                      System.err.println("WARN: Booked applicant " + targetNRIC + " withdrawing had unexpected flat type '" + flatType + "'. Room count not adjusted.");
                 }
             } else {
                 System.err.println("WARN: Booked applicant " + targetNRIC + " withdrawing had null flat type. Room count not adjusted.");
             }
        }

        // If successfully removed from either list, add to withdrawRequests
        if (moved) {
             if (this.withdrawRequests != null) {
                 if (!this.withdrawRequests.contains(applicant)){
                      this.withdrawRequests.add(applicant);
                 }
                 return true;
             } else {
                  System.err.println("ERROR: withdrawRequests list is null in project " + this.name);
                  return false;
             }
        } else {
            System.err.println("Warning: Applicant " + targetNRIC + " requesting withdrawal not found in successful or booked lists for project " + this.name + ". Cannot process request.");
            return false;
        }
	}
	    /**
     * Processes an accepted withdrawal request for an applicant.
     * Removes the applicant from the withdrawRequests list and adds them
     * to the unsuccessfulApplicants list to finalize their journey for this application.
     * Room count should have been adjusted when the withdrawal was initially requested if they were booked.
     * @param applicant The applicant whose withdrawal was accepted.
     * @return true if the applicant was successfully moved from requests to unsuccessful, false otherwise.
     */
    public boolean processAcceptedWithdrawal(Applicant applicant) {
        if (applicant == null) {
            System.err.println("Cannot process accepted withdrawal for null applicant.");
            return false;
        }

        // 1. Remove from withdrawRequests list
        boolean removedFromWithdraw = false;
        if (this.withdrawRequests != null) {
            removedFromWithdraw = this.withdrawRequests.removeIf(a -> a != null && a.equals(applicant));
        }

        if (!removedFromWithdraw) {
             System.err.println("Warning: Applicant " + applicant.getNric() + " not found in withdrawRequests list during withdrawal acceptance for project " + this.name);
             return false;
        }

        // 2. Add to unsuccessful list (marks end of this application lifecycle)
        boolean addedToUnsuccessful = false;
        if (this.unsuccessfulApplicants != null) {
             if (!this.unsuccessfulApplicants.contains(applicant)) {
                 addedToUnsuccessful = this.unsuccessfulApplicants.add(applicant);
             } else {
                  addedToUnsuccessful = true; 
             }
        } else {
             System.err.println("ERROR: unsuccessfulApplicants list is null in project " + this.name);
             return false; 
        }

        if (addedToUnsuccessful) {
             return true; // Successfully processed
        } else {
             System.err.println("ERROR: Failed to add applicant " + applicant.getNric() + " to unsuccessful list during withdrawal acceptance.");
             return false;
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

	/**
	 * Checks if the date range of this project overlaps with a given date range.
	 * Overlap occurs if one period starts before the other ends.
	 * Assumes dates are inclusive.
	 *
	 * @param otherStart The start date of the other period.
	 * @param otherEnd   The end date of the other period.
	 * @return true if the periods overlap, false otherwise.
	 */
	public boolean isClashing(LocalDate otherStart, LocalDate otherEnd) {
		// Ensure all dates are valid before comparison
		if (otherStart == null || otherEnd == null || this.appOpeningDate == null || this.appClosingDate == null) {
			System.err.println("Warning: Cannot check clash due to null dates for project " + this.name);
			return false; 
		}

		// Check for invalid date ranges (end before start)
		if (otherEnd.isBefore(otherStart) || this.appClosingDate.isBefore(this.appOpeningDate)) {
			System.err.println("Warning: Invalid date range detected during clash check for project " + this.name);
			return false; 
		}

		// Condition 1: The other period ends before this one starts.
		boolean otherEndsBeforeThisStarts = otherEnd.isBefore(this.appOpeningDate);
		// Condition 2: This period ends before the other one starts.
		boolean thisEndsBeforeOtherStarts = this.appClosingDate.isBefore(otherStart);

		boolean noOverlap = otherEndsBeforeThisStarts || thisEndsBeforeOtherStarts;

		return !noOverlap; 
	}

}
