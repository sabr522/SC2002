package Actors;

import java.util.HashMap;
import java.util.Map;

public class Reply {
    private static Map<Integer, Integer> enquiryReplyCounters = new HashMap<>(); // Key: Enquiry ID, Value: Last Reply ID
    private final int id;
    private final int enquiryId; 
    private final String responderNRIC;
    private String content;

	// this constructor FOR LOADING ONLY
    public Reply(Enquiry enquiry, String responderNRIC, String content, int existingId) {
		if (enquiry == null) throw new IllegalArgumentException("Enquiry cannot be null for Reply");
		this.id = existingId;
		this.enquiryId = enquiry.getId(); // Store parent ID
		this.responderNRIC = responderNRIC;
		this.content = content;
   }

    // Default Constructor
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
	
	public int getId() {
		return id;
	}

	public int getEnquiryId() {
		return enquiryId;
   }
	
	public String getResponderNRIC() {
		return responderNRIC;
	}
	
	public String getContent() {
		return content;
	}
		
	public void setContent(String content) {
		this.content = content;
	}

	 // Add this method to reset counter AFTER loading all replies
	public static void resetIdCounters(Map<Integer, Integer> loadedMaxIdsPerEnquiry) {
		enquiryReplyCounters = new HashMap<>(loadedMaxIdsPerEnquiry); // Replace with loaded max counts
		System.out.println("Reply ID counters reset based on loaded max IDs per enquiry.");
	}
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reply reply = (Reply) o;
        return id == reply.id && enquiryId == reply.enquiryId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, enquiryId);
    }
	
}
