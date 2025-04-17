
package Actors;

import Project.Project;

import java.util.List;
import java.util.Map;

public interface ApplicantRole {

	// To view the list of available projects for the applicant based on eligibility
    List<Project> viewAvailProjects(Map<String, Project> allProjectsMap);

    // TO apply for a project, passes the project name and chosen flat type
    void applyProject(List<Project> availableProjects, String projectName, String chosenFlatType);

    // To view the project the applicant applied for, if any
    String viewAppliedProject();
    
    // Checks appStatus
    String checkApplicationStatus();
    
    // Method to book a flat if application status is 'Successful'
    void bookFlat();
    
    // Method to withdraw the application if the applicant is eligible
    void withdrawApp();

}
