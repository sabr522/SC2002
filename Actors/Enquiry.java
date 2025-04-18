package Actors;

import java.util.ArrayList;
import java.util.List;

public class Enquiry {
	private final int id;
	private static int idCounter = 0;
	private final String applicantNRIC;
	private String content;
	private final String project;
	private final List<Reply> replies;

	// constructor FOR LOADING ONLY
	public Enquiry(String applicantNRIC, String content, String project, int existingId) {
		this.id = existingId; // Use loaded ID
		this.applicantNRIC = applicantNRIC;
		this.content = content;
		this.project = project;
		this.replies = new ArrayList<>();
	}
	
	public Enquiry(String applicantNRIC, String content, String project) {
		this.applicantNRIC = applicantNRIC;
		this.content = content;
		this.project = project;
		this.replies = new ArrayList<>();
	
		this.id = ++idCounter; // Pre-increment to start IDs from 1
	}
	
	public int getId() {
		return id;
	}
	
	public String getApplicantNRIC() {
		return applicantNRIC;
	}	
	
	public String getContent() {
		return content;
	}
	
	public String getProject() {
		return project;
	}
		
	public void setContent(String content) {
		this.content = content;
	}
	
	public List<Reply> getReplies() {
		return replies;
	}
	
	public void addReply(Reply reply) {
		replies.add(reply);
	}
	
	public void deleteReply(int replyId) {
		replies.removeIf(r -> r.getId() == replyId);
	}

	// Add this method to reset counter AFTER loading all enquiries
	public static void resetIdCounter(int maxLoadedId) {
		// Check if the loaded ID is greater than current counter
		// Handles case where file is empty or IDs are non-sequential
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
