package com.isostech.jira.mailhandler;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.plugins.mail.webwork.AbstractEditHandlerDetailsWebAction;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;

import java.util.Map;

/**
 * @author roberthall
 *
 */
public class EditExtendedCreateIssueHandlerDetailsWebAction extends AbstractEditHandlerDetailsWebAction {
	private final ProjectKeyValidator projectKeyValidator;

	public EditExtendedCreateIssueHandlerDetailsWebAction(PluginAccessor pluginAccessor, ProjectKeyValidator projectKeyValidator) {
		super(pluginAccessor);
		this.projectKeyValidator = projectKeyValidator;
	}

	private String issueType;
	private String projectKey;
	private String subject;
	private String reporter;
	
	

    public String getIssueType() {
		return issueType;
	}


	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}


	public String getProjectKey() {
		return projectKey;
	}


	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	} 

	public String getReporter() {
		return reporter;
	}


	public void setReporter(String reporter) {
		this.reporter = reporter;
	}


	// this method is called to let us populate our variables (or action state) with current handler settings
    // managed by associated service (file or mail).
	@Override
	protected void copyServiceSettings(JiraServiceContainer jiraServiceContainer) throws ObjectConfigurationException {
		final String params = jiraServiceContainer.getProperty(AbstractMessageHandlingService.KEY_HANDLER_PARAMS);
		final Map<String, String> parameterMap = ServiceUtils.getParameterMap(params);
		issueType = parameterMap.get(ExtendedCreateIssueHandler.KEY_ISSUE_TYPE);
		projectKey = parameterMap.get(ExtendedCreateIssueHandler.KEY_PROJECT_KEY);
		subject = parameterMap.get(ExtendedCreateIssueHandler.KEY_SUBJECT);
		reporter = parameterMap.get(ExtendedCreateIssueHandler.KEY_REPORTER);
	}


	@Override
	protected Map<String, String> getHandlerParams() {
		return MapBuilder.build(ExtendedCreateIssueHandler.KEY_ISSUE_TYPE, issueType, ExtendedCreateIssueHandler.KEY_PROJECT_KEY, projectKey, ExtendedCreateIssueHandler.KEY_SUBJECT, subject, ExtendedCreateIssueHandler.KEY_REPORTER, reporter);
	}


	@Override
	protected void doValidation() {
		if (configuration == null) {
			return; // short-circuit in case we lost session, goes directly to doExecute which redirects user
		}
		super.doValidation();
		projectKeyValidator.validateProject(projectKey, new WebWorkErrorCollector());
	}


}
