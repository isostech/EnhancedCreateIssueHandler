package com.isostech.jira.mailhandler;

import java.util.Map;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.Collection;
import java.util.Set;

import java.util.HashMap;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.mail.MailUtils;

/**
 * Message handler to set subject on emails w/ no subject.
 * 
 * @author roberthall
 * 
 */
public class ExtendedCreateIssueHandler implements MessageHandler {

	private String projectKey = null;
	private String issueType = null;
	private String configuredSubject = null;
	private String configuredReporter = null;
	private Map<String, String> handlerParams = null;

	private String defaultSubject = "NO SUBJECT";
	private String defaultReporter = "admin";
	private final ProjectKeyValidator projectKeyValidator;
	private final MessageUserProcessor messageUserProcessor;
	public static final String KEY_ISSUE_TYPE = "issuetype";
	public static final String KEY_PROJECT_KEY = "project";
	public static final String KEY_SUBJECT = "subject";
	public static final String KEY_REPORTER = "reporterusername";

	/**
	 * Constructor
	 * 
	 * @param messageUserProcessor
	 * @param projectKeyValidator
	 */
	public ExtendedCreateIssueHandler(
			MessageUserProcessor messageUserProcessor,
			ProjectKeyValidator projectKeyValidator) {

		this.messageUserProcessor = messageUserProcessor;
		this.projectKeyValidator = projectKeyValidator;
	}

	@Override
	public void init(Map<String, String> params,
			MessageHandlerErrorCollector monitor) {
		handlerParams = params;

		projectKey = params.get(KEY_PROJECT_KEY);
		if (StringUtils.isBlank(projectKey)) {
			// this message will be either logged or displayed to the user (if
			// the handler is tested from web UI)
			monitor.error("Project key has not been specified ('"
					+ KEY_PROJECT_KEY
					+ "' parameter). This handler will not work correctly.");
		}

		issueType = params.get(KEY_ISSUE_TYPE);
		if (StringUtils.isBlank(issueType)) { // this message will be either
			// logged or displayed to the user (if // the handler is tested from
			// web
			// UI)
			monitor.error("Issue Type has not been specified ('"
					+ KEY_ISSUE_TYPE
					+ "' parameter). This handler will not work correctly.");
		}

		configuredSubject = params.get(KEY_SUBJECT);
		if (StringUtils.isBlank(configuredSubject)) {
			// this message will be either logged or displayed to the user (if
			// the handler is tested from web UI)
			monitor.error("Subject has not been specified ('" + KEY_SUBJECT
					+ "' parameter). Default subject will be used: "
					+ defaultSubject);
		}
		configuredReporter = params.get(KEY_REPORTER);
		if (StringUtils.isBlank(configuredReporter)) {
			// this message will be either logged or displayed to the user (if
			// the handler is tested from web UI)
			monitor.error("Reporter has not been specified ('" + KEY_REPORTER
					+ "' parameter). Default reporter will be used: "
					+ defaultReporter);
		}
		projectKeyValidator.validateProject(projectKey, monitor);
	}

	/*
	 * Process email messages (non-Javadoc)
	 * 
	 * @see
	 * com.atlassian.jira.service.util.handler.MessageHandler#handleMessage(
	 * javax.mail.Message,
	 * com.atlassian.jira.service.util.handler.MessageHandlerContext)
	 */
	@Override
	public boolean handleMessage(Message message, MessageHandlerContext context)
			throws MessagingException {

		ProjectManager pm = ComponentAccessor.getProjectManager();
		Project proj = pm.getProjectObjByKey(projectKey);
		if (proj != null) {
			Collection<IssueType> types = proj.getIssueTypes();
			for (IssueType type : types) {

				if (issueType != null && issueType.contains(type.getName())) {

					issueType = type.getId();
					break;
				}
			}
		}
		if (issueType == null) {
			issueType = "1";
		}
		try {
			if (message != null) {
				String subject = message.getSubject();

				String fromAddr = null;
				String fromName = null;
				String fromEmail = null;
				String body = null;

				Address[] addr = message.getFrom();

				if (addr != null) {

					fromAddr = addr[0].toString();

					String[] frags = fromAddr.split("<");

					if (frags[1] != null) {
						frags[1] = frags[1].replace(">", "");
					}

					fromName = frags[0];
					fromEmail = frags[1];
				}
				body = getMessageBody(message);

				if (subject == null || StringUtils.isBlank(subject)) {
					if (configuredSubject != null)
						subject = configuredSubject;
					else
						subject = defaultSubject;

					Properties props = new Properties();
					Session session = Session.getDefaultInstance(props, null);
					Message msg = new MimeMessage(session);
					msg.setSubject(subject);
					msg.setText(getMessageBody(message));
					msg.setSentDate(message.getSentDate());
					if (addr != null) {
						msg.setFrom(new InternetAddress(fromEmail, fromName));

					} else {
						msg.setFrom(new InternetAddress(
								"someone@somewhere.com", "sender"));
					}

					msg.setDescription(message.getDescription());
					msg.setRecipients(Message.RecipientType.TO,
							message.getRecipients(Message.RecipientType.TO));
					Object p = message.getContent();
					if (p instanceof Multipart) {
						msg.setContent((Multipart) p);
					}
					msg.addHeader("Content-Type", message.getContentType());

					message = msg;
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return createIssue(message, context);
	}

	/**
	 * Call the internal Jira CreateIssueHandler to process the message.
	 * 
	 * @param message
	 * @param context
	 * @return
	 * @throws MessagingException
	 */
	private boolean createIssue(Message message, MessageHandlerContext context)
			throws MessagingException {
		String reporter = defaultReporter;
		if (configuredReporter != null)
			reporter = configuredReporter;

		CreateIssueHandler cih = new CreateIssueHandler();
		Map<String, String> params = new HashMap<String, String>();

		cih.init(handlerParams, context.getMonitor());
		return cih.handleMessage(message, context);

	}

	/**
	 * Get plain-text message body
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	private String getMessageBody(Message message) throws Exception {
		Object content = message.getContent();
		String body = null;

		if (content != null) {
			if (content instanceof String) {
				body = (String) content;
			} else if (content instanceof MimeMultipart) {
				MimeMultipart msgBody = (MimeMultipart) content;
				for (int i = 0; i < msgBody.getCount(); i++) {
					MimeBodyPart bodyPart = (MimeBodyPart) msgBody
							.getBodyPart(i);

					if (i == 0) {
						body = bodyPart.getContent().toString(); // text

					}
				}
			}

		}
		return body;
	}
}
