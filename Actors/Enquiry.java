package Actors;

import Actors.User; // Base class
import Actors.Applicant;
import Actors.Officer;
import Actors.Manager;
import Actors.Reply;
import Project.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enquiry {
    private final static Map<String, Integer> applicantCounters = new HashMap<>();
	private final int id;
	private final String applicantNRIC;
	private String content;
	private final String project;
	private final List<Reply> replies;
	
	public Enquiry(String applicantNRIC, String content, String project) {
		this.applicantNRIC = applicantNRIC;
		this.content = content;
		this.project = project;
		this.replies = new ArrayList<>();
		
		int currentCount = applicantCounters.getOrDefault(applicantNRIC, 0);
		this.id = currentCount + 1;
		
		applicantCounters.put(applicantNRIC, this.id);
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
}
