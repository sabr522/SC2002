package Actors;

import java.util.HashMap;
import java.util.Map;

public class Reply {
    private final static Map<Enquiry, Integer> enquiryReplyCounters = new HashMap<>();
    private final int id;
	private final Enquiry enquiry;
	private final String responderNRIC;
	private String content;
	
	public Reply(Enquiry enquiry, String responderNRIC, String content) {
		this.enquiry = enquiry;
		this.responderNRIC = responderNRIC;
		this.content = content;
		
		int currentReplyCount = enquiryReplyCounters.getOrDefault(enquiry, 0);
        this.id = currentReplyCount + 1;
        
        enquiryReplyCounters.put(enquiry, this.id);
	}
	
	public int getId() {
		return id;
	}
	
	public String getResponderNRIC() {
		return responderNRIC;
	}
	
	public String getContent() {
		return content;
	}
	
	public Enquiry getEnquiry() {
		return enquiry;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
}
