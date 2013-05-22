package com.isostech.jira.mailhandler;

import com.atlassian.configurable.ObjectConfigurationException;
import java.util.HashMap;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugins.mail.webwork.AbstractEditHandlerDetailsWebAction;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.plugins.mail.handlers.RegexCommentHandler;
import com.atlassian.jira.plugins.mail.HandlerDetailsValidator;
import com.isostech.jira.plugins.mail.model.HandlerDetailsModel;
import com.atlassian.jira.plugins.mail.model.IssueTypeModel;
import com.google.common.collect.ImmutableMap;
import com.atlassian.jira.plugins.mail.model.OptionModel;
import com.atlassian.jira.plugins.mail.model.ProjectModel;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * @author roberthall
 * 
 */
public class EditExtendedCreateIssueHandlerDetailsWebAction extends
		AbstractEditHandlerDetailsWebAction {

	// this mapping is used both in VM to render the form labels and in handlers
	// table - order matters.
	private static final Map<String, String> fieldLabels = ImmutableMap
			.<String, String> builder()
			.put("project", "common.concepts.project")
			.put("issuetype", "common.concepts.issuetype")
			.put("stripquotes", "jmp.editHandlerDetails.stripquotes")
			.put("reporterusername", "jmp.editHandlerDetails.reporterusername")
			.put("splitregex", "jmp.editHandlerDetails.splitregex")
			.put("subject", "jmp.editHandlerDetails.subject")
			.put("catchemail", "jmp.editHandlerDetails.catchemail")
			.put("bulk", "jmp.editHandlerDetails.bulk")
			.put("forwardEmail", "admin.service.common.handler.forward.email")
			.put("createusers", "jmp.editHandlerDetails.createusers")
			.put("notifyusers", "jmp.editHandlerDetails.notifyusers")
			.put("ccassignee", "jmp.editHandlerDetails.ccassignee")
			.put("ccwatcher", "jmp.editHandlerDetails.ccwatcher")
			.put("port", "jmp.editHandlerDetails.port")
			.put("usessl", "jmp.editHandlerDetails.usessl").build();

	private final IssueTypeSchemeManager issueTypeSchemeManager;
	private final HandlerDetailsValidator detailsValidator = null;

	private HandlerDetailsModel details;

	private final List<Project> projects;

	private String detailsJson;

	private final ProjectKeyValidator projectKeyValidator;

	private String issuetype;
	private String project;
	private String subject;
	private String reporterusername;
	private String catchemail;
	private String bulk;
	private String forwardEmail;
	private boolean createusers;
	private boolean notifyusers;
	private boolean ccwatcher;
	private boolean ccassignee = CreateIssueHandler.DEFAULT_CC_ASSIGNEE;

	public EditExtendedCreateIssueHandlerDetailsWebAction(
			IssueTypeSchemeManager issueTypeSchemeManager,
			PluginAccessor pluginAccessor,
			ProjectKeyValidator projectKeyValidator) {
		super(pluginAccessor);

		this.issueTypeSchemeManager = issueTypeSchemeManager;
		this.projectKeyValidator = projectKeyValidator;
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		projects = projectManager.getProjectObjects();
	}

	public String getIssuetype() {
		return issuetype;
	}

	public void setIssuetype(String issuetype) {
		this.issuetype = issuetype;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getReporterusername() {
		return reporterusername;
	}

	public void setReporterusername(String reporterusername) {
		this.reporterusername = reporterusername;
	}

	public HandlerDetailsModel getDetails() {
		return details;
	}

	public void setDetails(HandlerDetailsModel details) {
		this.details = details;
	}

	public HandlerDetailsValidator getDetailsValidator() {
		return detailsValidator;
	}

	public ProjectKeyValidator getProjectKeyValidator() {
		return projectKeyValidator;
	}

	public String getCatchemail() {
		return catchemail;
	}

	public void setCatchemail(String catchemail) {
		this.catchemail = catchemail;
	}

	public String getBulk() {
		return bulk;
	}

	public void setBulk(String bulk) {
		this.bulk = bulk;
	}

	public String getForwardEmail() {
		return forwardEmail;
	}

	public void setForwardEmail(String forwardEmail) {
		this.forwardEmail = forwardEmail;
	}

	public boolean isCreateusers() {
		return createusers;
	}

	public void setCreateusers(boolean createusers) {
		this.createusers = createusers;
	}

	public boolean isNotifyusers() {
		return notifyusers;
	}

	public void setNotifyusers(boolean notifyusers) {
		this.notifyusers = notifyusers;
	}

	public boolean isCcwatcher() {
		return ccwatcher;
	}

	public void setCcwatcher(boolean ccwatcher) {
		this.ccwatcher = ccwatcher;
	}

	public boolean isCcassignee() {
		return ccassignee;
	}

	public void setCcassignee(boolean ccassignee) {
		this.ccassignee = ccassignee;
	}

	// this method is called to let us populate our variables (or action state)
	// with current handler settings
	// managed by associated service (file or mail).
	@Override
	protected void copyServiceSettings(JiraServiceContainer jiraServiceContainer)
			throws ObjectConfigurationException {
		final String params = jiraServiceContainer
				.getProperty(AbstractMessageHandlingService.KEY_HANDLER_PARAMS);
		final Map<String, String> parameterMap = ServiceUtils
				.getParameterMap(params);

		issuetype = parameterMap.get(ExtendedCreateIssueHandler.KEY_ISSUE_TYPE);
		project = parameterMap.get(ExtendedCreateIssueHandler.KEY_PROJECT_KEY);
		subject = parameterMap.get(ExtendedCreateIssueHandler.KEY_SUBJECT);
		reporterusername = parameterMap
				.get(ExtendedCreateIssueHandler.KEY_REPORTER);
		catchemail = parameterMap.get(AbstractMessageHandler.KEY_CATCHEMAIL);
		bulk = parameterMap.get(AbstractMessageHandler.KEY_BULK);
		createusers = Boolean.parseBoolean(parameterMap
				.get(AbstractMessageHandler.KEY_CREATEUSERS));
		notifyusers = Boolean.parseBoolean(parameterMap
				.get(AbstractMessageHandler.KEY_NOTIFYUSERS));
		ccwatcher = Boolean.parseBoolean(parameterMap
				.get(CreateIssueHandler.CC_WATCHER));
		ccassignee = Boolean.parseBoolean(parameterMap
				.get(CreateIssueHandler.CC_ASSIGNEE));
	}

	@Override
	protected Map<String, String> getHandlerParams() {

		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(ExtendedCreateIssueHandler.KEY_ISSUE_TYPE, issuetype);
		aMap.put(ExtendedCreateIssueHandler.KEY_PROJECT_KEY, project);

		aMap.put(ExtendedCreateIssueHandler.KEY_SUBJECT, subject);
		aMap.put(ExtendedCreateIssueHandler.KEY_REPORTER, reporterusername);
		aMap.put(AbstractMessageHandler.KEY_CATCHEMAIL, catchemail);
		aMap.put(AbstractMessageHandler.KEY_BULK, bulk);
		aMap.put(AbstractMessageHandler.KEY_CREATEUSERS,
				String.valueOf(createusers));
		aMap.put(AbstractMessageHandler.KEY_NOTIFYUSERS,
				String.valueOf(notifyusers));
		aMap.put(CreateIssueHandler.CC_WATCHER, String.valueOf(ccwatcher));
		aMap.put(CreateIssueHandler.CC_ASSIGNEE, String.valueOf(ccassignee));
		return aMap;
	}

	@Override
	protected void doValidation() {

		if (configuration == null) {

			return; // short-circuit in case we lost session, goes directly to
					// doExecute which redirects user
		}
		// super.doValidation();
		// projectKeyValidator.validateProject(projectKey, new
		// WebWorkErrorCollector());
	}

	public static Map<String, String> getFieldlabels() {
		return fieldLabels;
	}

	public IssueTypeSchemeManager getIssueTypeSchemeManager() {
		return issueTypeSchemeManager;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public static Map<String, String> getFieldLabels() {
		return fieldLabels;
	}

	@SuppressWarnings("unused")
	public void setDetailsJson(String json) {
		this.detailsJson = json;
	}

	@Nonnull
	public String getDetailsJson() {
		try {
			return new ObjectMapper().writeValueAsString(details);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	public String getBulkOptionsJson() {
		final List<OptionModel> options = Lists.newArrayList(new OptionModel(
				getText("jmp.editHandlerDetails.bulk.ignore"),
				AbstractMessageHandler.VALUE_BULK_IGNORE), new OptionModel(
				getText("jmp.editHandlerDetails.bulk.forward"),
				AbstractMessageHandler.VALUE_BULK_FORWARD), new OptionModel(
				getText("jmp.editHandlerDetails.bulk.delete"),
				AbstractMessageHandler.VALUE_BULK_DELETE));
		try {
			return new ObjectMapper().writeValueAsString(options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	public String getProjectsJson() {
		final List<ProjectModel> suggestions = Lists.newArrayList(Iterables
				.transform(projects, new Function<Project, ProjectModel>() {
					@Override
					public ProjectModel apply(final Project project) {
						return new ProjectModel(
								project.getName(),
								project.getKey(),
								Lists.<IssueTypeModel> newArrayList(Iterables.transform(
										issueTypeSchemeManager
												.getIssueTypesForProject(project),
										new Function<IssueType, IssueTypeModel>() {
											@Override
											public IssueTypeModel apply(
													IssueType from) {
												return new IssueTypeModel(from
														.getName(), from
														.getId());
											}
										})));
					}
				}));

		try {
			return new ObjectMapper().writeValueAsString(suggestions);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isEnhancedCreateIssueHandlerSelected() {
		return true;
	}
}
