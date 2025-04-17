package Services;

import java.util.*;

import Actors.User; // Base class
import Actors.Applicant;
import Actors.Officer;
import Actors.Manager;
import Actors.Enquiry;
import Actors.Reply;
import Project.Project;

public class EnquiryService {
	private final Map<Integer, Enquiry> enquiryMap;
	
	public EnquiryService() {
		this.enquiryMap = new HashMap<>();
	}
	
	public Enquiry getEnquiryById(int enquiryId) {
		return enquiryMap.get(enquiryId);
	}
	
	public List<Enquiry> getEnquiriesByProject(String project) {
		List<Enquiry> list = new ArrayList<>();
		for (Enquiry e : enquiryMap.values()) {
			if (e.getProject().equals(project)) {
				list.add(e);
			}
		}
		return list;
	}
	
	public List<Enquiry> getEnquiryByApplicantNRIC(String applicantNRIC) {
		List<Enquiry> list = new ArrayList<>();
		for (Enquiry e : enquiryMap.values()) {
			if (e.getApplicantNRIC().equals(applicantNRIC)) {
				list.add(e);
			}
		}
		return list;
	}
	
	public List<Enquiry> getAllEnquiries() {
		return new ArrayList<>(enquiryMap.values());
	}
	
	public Enquiry submitEnquiry(String applicantNRIC, String content, String project) {
		Enquiry enquiry = new Enquiry(applicantNRIC, content, project);
		enquiryMap.put(enquiry.getId(), enquiry);
		return enquiry;
	}
	
	public boolean editEnquiry(int enquiryId, String applicantNRIC,  String newContent) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null && enquiry.getApplicantNRIC().equals(applicantNRIC)) {
			enquiry.setContent(newContent);
			return true;
		}
		return false;
	}
	
	public boolean deleteEnquiry(int enquiryId, String applicantNRIC) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null && enquiry.getApplicantNRIC().equals(applicantNRIC)) {
			enquiryMap.remove(enquiryId);
			return true;
		}
		return false;
	}
	
	public boolean replyToEnquiry(int enquiryId, String responderNRIC, String content) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null) {
			Reply reply = new Reply(enquiry, responderNRIC, content);
			enquiry.addReply(reply);
			return true;
		}
		return false;
	}
	
	public boolean editReply(int enquiryId, int replyId, String responderNRIC, String newContent) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null) {
			for (Reply reply : enquiry.getReplies()) {
				if(reply.getId() == replyId && reply.getResponderNRIC().equals(responderNRIC)) {
					reply.setContent(newContent);
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Reply> viewReplies(int enquiryId) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null) {
			return enquiry.getReplies();
		}
		return new ArrayList<>();
	}
}
