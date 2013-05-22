package com.isostech.jira.mailhandler;

import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;

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
			
		}else{
			collector.error("Cannot add an issue from mail to project '" + projectKey + "'. The project does not exist.");
			return null;
		}
	 
		return proj;
	}

}
