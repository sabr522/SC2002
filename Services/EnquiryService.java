package Services;

import java.util.*;

import Actors.Enquiry;
import Actors.Reply;

/**
 * Service class that manages enquiries and their replies.
 * Handles creation, lookup, and editing of enquiries and replies.
 */
public class EnquiryService {
	private final Map<Integer, Enquiry> enquiryMap;

    /**
     * Constructs a new empty enquiry service.
     */
	public EnquiryService() {
		this.enquiryMap = new HashMap<>();
	}

    /**
     * Retrieves an enquiry by ID.
     * @param enquiryId The enquiry ID
     * @return The matching enquiry or null
     */
	public Enquiry getEnquiryById(int enquiryId) {
		return enquiryMap.get(enquiryId);
	}

    /**
     * Gets enquiries related to a specific project.
     * @param project Project name
     * @return List of enquiries
     */
	public List<Enquiry> getEnquiriesByProject(String project) {
		List<Enquiry> list = new ArrayList<>();
		for (Enquiry e : enquiryMap.values()) {
			if (e.getProject().equals(project)) {
				list.add(e);
			}
		}
		return list;
	}

    /**
     * Gets all enquiries submitted by a specific applicant.
     * @param applicantNRIC The applicant's NRIC
     * @return List of enquiries
     */
	public List<Enquiry> getEnquiryByApplicantNRIC(String applicantNRIC) {
		List<Enquiry> list = new ArrayList<>();
		for (Enquiry e : enquiryMap.values()) {
			if (e.getApplicantNRIC().equals(applicantNRIC)) {
				list.add(e);
			}
		}
		return list;
	}

    /**
     * Gets all enquiries in the system.
     * @return List of all enquiries
     */
	public List<Enquiry> getAllEnquiries() {
		return new ArrayList<>(enquiryMap.values());
	}

    /**
     * Submits a new enquiry.
     * @param applicantNRIC NRIC of the applicant
     * @param content Enquiry text
     * @param project Project name
     * @return The created enquiry
     */
	public Enquiry submitEnquiry(String applicantNRIC, String content, String project) {
		Enquiry enquiry = new Enquiry(applicantNRIC, content, project);
		enquiryMap.put(enquiry.getId(), enquiry);
		return enquiry;
	}

    /**
     * Edit an existing enquiry.
     * @param enquiryId Enquiry ID
     * @param applicantNRIC Owner NRIC
     * @param newContent New enquiry text
     * @return true if successful
     */
	public boolean editEnquiry(int enquiryId, String applicantNRIC,  String newContent) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null && enquiry.getApplicantNRIC().equals(applicantNRIC)) {
			enquiry.setContent(newContent);
			return true;
		}
		return false;
	}

    /**
     * Deletes an enquiry.
     * @param enquiryId Enquiry ID
     * @param applicantNRIC Owner NRIC
     * @return true if successful
     */
	public boolean deleteEnquiry(int enquiryId, String applicantNRIC) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null && enquiry.getApplicantNRIC().equals(applicantNRIC)) {
			enquiryMap.remove(enquiryId);
			return true;
		}
		return false;
	}

    /**
     * Submits a reply to a specific enquiry.
     * @param enquiryId Enquiry ID
     * @param responderNRIC Responder NRIC
     * @param content Reply message
     * @return true if successful
     */
	public boolean replyToEnquiry(int enquiryId, String responderNRIC, String content) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null) {
			Reply reply = new Reply(enquiry, responderNRIC, content);
			enquiry.addReply(reply);
			return true;
		}
		return false;
	}

    /**
     * Edits an existing reply.
     * @param enquiryId Parent enquiry ID
     * @param replyId Reply ID
     * @param responderNRIC Owner NRIC
     * @param newContent New reply text
     * @return true if updated
     */
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

    /**
     * Gets replies for a given enquiry.
     * @param enquiryId Enquiry ID
     * @return List of replies
     */
	public List<Reply> viewReplies(int enquiryId) {
		Enquiry enquiry = enquiryMap.get(enquiryId);
		if (enquiry != null) {
			return enquiry.getReplies();
		}
		return new ArrayList<>();
	}
	
	/**
	 * Loads existing enquiries into the service (called by DataManager).
	 * Clears the current map before loading.
	 * @param loadedEnquiries A map of Enquiry objects loaded from file.
	 */
	public void loadExistingEnquiries(Map<Integer, Enquiry> loadedEnquiries) {
		if (loadedEnquiries != null) {
			this.enquiryMap.clear(); // Clear existing map
			this.enquiryMap.putAll(loadedEnquiries);
			System.out.println("EnquiryService populated with " + this.enquiryMap.size() + " loaded enquiries.");
		} else {
			this.enquiryMap.clear(); // Clear map if loaded data is null
			System.out.println("No enquiry data loaded, EnquiryService is empty.");
		}
	}
}
