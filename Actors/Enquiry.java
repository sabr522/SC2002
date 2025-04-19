package Actors;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an enquiry submitted by an applicant.
 * Contains an ID, associated project, content, and optional replies.
 */
public class Enquiry {
	private final int id;
	private static int idCounter = 0;
	private final String applicantNRIC;
	private String content;
	private final String project;
	private final List<Reply> replies;

    /**
     * Constructs a new Enquiry with a pre-assigned ID (used during file loading).
     * @param applicantNRIC Applicant NRIC
     * @param content Enquiry message
     * @param project Associated project name
     * @param existingId ID loaded from file
     */
	public Enquiry(String applicantNRIC, String content, String project, int existingId) {
		this.id = existingId; 
		this.applicantNRIC = applicantNRIC;
		this.content = content;
		this.project = project;
		this.replies = new ArrayList<>();
	}
	
	
    /**
     * Constructs a new Enquiry with an auto-incremented ID.
     * @param applicantNRIC Applicant NRIC
     * @param content Enquiry message
     * @param project Associated project name
     */
	public Enquiry(String applicantNRIC, String content, String project) {
		this.applicantNRIC = applicantNRIC;
		this.content = content;
		this.project = project;
		this.replies = new ArrayList<>();
		this.id = ++idCounter; 
	}
	
    /** 
     * Returns the unique ID of the enquiry.
     * @return Unique Enquiry ID */
	public int getId() {
		return id;
	}
	
	/** 
	 * Returns the NRIC of the applicant who submitted the enquiry.
	 * @return NRIC of the applicant who submitted the enquiry */
	public String getApplicantNRIC() {
		return applicantNRIC;
	}	
	
	/** 
	 * Returns the message content of the enquiry.
	 * @return The enquiry content */
	public String getContent() {
		return content;
	}
	
	/** 
	 * Returns the name of the project this enquiry is about.
	 * @return The associated project name */
	public String getProject() {
		return project;
	}
		
    /**
     * Updates the enquiry content.
     * @param content New enquiry text
     */
	public void setContent(String content) {
		this.content = content;
	}
	
	/** 
	 * Returns the list of replies associated with this enquiry.
	 * @return List of replies associated with this enquiry */
	public List<Reply> getReplies() {
		return replies;
	}
	
    /**
     * Adds a reply to this enquiry.
     * @param reply A valid reply instance
     */
	public void addReply(Reply reply) {
		replies.add(reply);
	}
	
    /**
     * Deletes a reply by its ID.
     * @param replyId ID of reply to delete
     */
	public void deleteReply(int replyId) {
		replies.removeIf(r -> r.getId() == replyId);
	}

    /**
     * Resets the enquiry ID counter based on the highest ID loaded.
     * @param maxLoadedId Highest existing ID to reset from
     */
	public static void resetIdCounter(int maxLoadedId) {
		idCounter = Math.max(idCounter, maxLoadedId);
		System.out.println("Enquiry ID counter reset based on loaded max ID: " + idCounter);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Enquiry enquiry = (Enquiry) o;
		return id == enquiry.id;
	}
   
	@Override
	public int hashCode() {
		return java.util.Objects.hash(id);
	}
}
