
package Actors;

import Project.Project;

import java.util.List;
import java.util.Map;

/**
 * Interface defining behaviors expected from any applicant.
 * Includes methods to apply, view status, book, or withdraw applications.
 */
public interface ApplicantRole {

    /**
     * Gets list of available projects that the applicant can apply for.
     * @param allProjectsMap Map of all projects.
     * @return List of eligible projects.
     */
    List<Project> viewAvailProjects(Map<String, Project> allProjectsMap);

    /**
     * To apply to a given project using project name and flat type.
     * @param availableProjects List of projects user is eligible for.
     * @param projectName Name of the selected project.
     * @param chosenFlatType Type of flat requested (2-Room or 3-Room).
     */
    void applyProject(List<Project> availableProjects, String projectName, String chosenFlatType);

    /**
     * To view the project the applicant applied for, if any.
     * @return Project details or message.
     */
    String viewAppliedProject();
    
    /**
     * Checks and returns current application status.
     * @return The application status string.
     */
    String checkApplicationStatus();
    
    /**
     * Books a flat if the application is marked as successful.
     */
    void bookFlat();
    
    /**
     * Requests to withdraw an application if applicant is eligible.
     */
    void withdrawApp();

}
