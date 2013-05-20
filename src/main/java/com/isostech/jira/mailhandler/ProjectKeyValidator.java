package com.isostech.jira.mailhandler;

import java.util.Collection;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import org.apache.commons.lang.StringUtils;

public class ProjectKeyValidator
{
	private final ProjectManager  projectManager;

	public ProjectKeyValidator(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}

	public Project validateProject(String projectKey, MessageHandlerErrorCollector collector) {
		if (StringUtils.isBlank(projectKey)) {
			collector.error("Project key has to be defined. Found '" + projectKey + "'" );
			return null;
		} 
		
		Project proj = projectManager.getProjectObjByKey(projectKey);
		if (proj != null) {
			Collection<IssueType> types = proj.getIssueTypes();
			for (IssueType type : types) {
				System.out.println("IssueType: id: " + type.getId() + " name: "
						+ type.getName() + " desc: " + type.getDescription());
			}
		}else{
			collector.error("Cannot add an issue from mail to project '" + projectKey + "'. The project does not exist.");
			return null;
		}
	 
		return proj;
	}

}
