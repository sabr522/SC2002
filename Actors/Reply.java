package Actors;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a reply to an enquiry. Each reply is linked to a specific enquiry and includes a unique ID.
 * Tracks who replied, what was said.
 */
public class Reply {
    private static Map<Integer, Integer> enquiryReplyCounters = new HashMap<>(); // Key: Enquiry ID, Value: Last Reply ID
    private final int id;
    private final int enquiryId; 
    private final String responderNRIC;
    private String content;

    /**
     * Constructor used for loading existing replies with known ID.
     * @param enquiry Parent enquiry
     * @param responderNRIC NRIC of responder
     * @param content Reply content
     * @param existingId Pre-assigned ID
     */
    public Reply(Enquiry enquiry, String responderNRIC, String content, int existingId) {
		if (enquiry == null) throw new IllegalArgumentException("Enquiry cannot be null for Reply");
		this.id = existingId;
		this.enquiryId = enquiry.getId(); // Store parent ID
		this.responderNRIC = responderNRIC;
		this.content = content;
   }

    /**
     * Constructor for creating a new reply. ID is auto-generated.
     * @param enquiry Parent enquiry
     * @param responderNRIC NRIC of responder
     * @param content Reply message
     */
    public Reply(Enquiry enquiry, String responderNRIC, String content) {
        if (enquiry == null) throw new IllegalArgumentException("Enquiry cannot be null for Reply");
        this.enquiryId = enquiry.getId();
        this.responderNRIC = responderNRIC;
        this.content = content;

        // Correct key type (Integer ID)
        int parentEnquiryId = enquiry.getId();
        int currentReplyCount = enquiryReplyCounters.getOrDefault(parentEnquiryId, 0);
        this.id = currentReplyCount + 1; // Generate next ID for *this* enquiry
        enquiryReplyCounters.put(parentEnquiryId, this.id); // Correct key type & Update counter
    }
	
    /** 
     * Gets the ID of this reply.
     * @return ID of the reply 
     */
	public int getId() {
		return id;
	}

	 /** 
	  * Gets the ID of the enquiry this reply is associated with.
	  * @return ID of the enquiry this reply belongs to 
	  */
	public int getEnquiryId() {
		return enquiryId;
   }
	
	 /** 
	  * Gets the NRIC of the user who wrote the reply.
	  * @return NRIC of the responder 
	  */
	public String getResponderNRIC() {
		return responderNRIC;
	}
	
	/** 
	 * Gets the content of the reply.
	 * @return Content of the reply 
	 */
	public String getContent() {
		return content;
	}
		
	/**
     * Sets new content for the reply.
     * @param content The new reply message
     */
	public void setContent(String content) {
		this.content = content;
	}

	   /**
     * Resets the reply ID counter per enquiry ID.
     * @param loadedMaxIdsPerEnquiry Map of max reply IDs for each enquiry
     */
	public static void resetIdCounters(Map<Integer, Integer> loadedMaxIdsPerEnquiry) {
		enquiryReplyCounters = new HashMap<>(loadedMaxIdsPerEnquiry); // Replace with loaded max counts
		System.out.println("Reply ID counters reset based on loaded max IDs per enquiry.");
	}

		/**
     * Compares this reply to another object.
     */
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reply reply = (Reply) o;
        return id == reply.id && enquiryId == reply.enquiryId;
    }

    /**
     * Generates a hash based on enquiry ID and reply ID.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, enquiryId);
    }
	
}
