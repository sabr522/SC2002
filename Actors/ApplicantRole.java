package Actors;

import Project.Project;
import Services.EnquiryService;

import java.util.List;

public interface ApplicantRole {

    List<Project> viewAvailProjects();

    void applyProject(String projectName, String chosenFlatType);

    String viewAppliedProject();

    String checkApplicationStatus();

    void bookFlat();

    void withdrawApp();

    void submitEnquiry(EnquiryService enquiryService, String content, String projectName);

    void viewEnquiries(EnquiryService enquiryService);

    boolean editEnquiry(EnquiryService enquiryService, int enquiryId, String newContent);

    boolean deleteEnquiry(EnquiryService enquiryService, int enquiryId);
}