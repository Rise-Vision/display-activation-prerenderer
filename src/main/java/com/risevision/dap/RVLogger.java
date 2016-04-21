/*
 * RV-Logger
 * version: 1.0.0
 * modified: Feb 17, 2015
 */

package com.risevision.dap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.apphosting.api.ApiProxy;

public class RVLogger {

	private static final String MODULE_NAME = "display-activation-prerenderer";
	private static final String PRODUCTION_APP_ID = "s~rvaserver2";

	private static Logger log = Logger.getAnonymousLogger();
	private String token;
	private String appId;
	private String appModule;
	private String appVersion;
	private String environment;
	
	public static final String LOGGER_VERSION = "1";
	public static final String QUEUE_NAME_LOGGER = "logger";

	public class QueryParam {
		public static final String ENVIRONMENT = "environment";
		public static final String ERROR_DETAILS = "error_details";
		public static final String ERROR_MESSAGE = "error_message";
		public static final String LOGGER_VERSION = "logger_version";
		public static final String SEVERITY = "severity";
		public static final String TASK = "task";
		public static final String TOKEN = "token";
	}

	public class QueueTask {
		public static final String SUBMIT = "submit";
		public static final String MAIL = "mail";
		public static final String CHAT = "chat";
		public static final String PERSIST = "persist";
	}

	public class Severity {
		public static final String INFO = "info";
		public static final String WARNING = "warning";
		public static final String ERROR = "error";
		public static final String ALERT = "alert";
	}

	/**
	 * Default constructor. It sets token and environment based on your Module Name and App ID.
	 * For the default modules use RVLogger(String token) form of constructor.
	 */
	public RVLogger() {
		this(MODULE_NAME);
	}

	/**
	 * Use this constructor if you need to set token manually, otherwise use RVLogger().
	 * 
	 * @param token - a name that clearly identifies your app. Example "core", "store", "store-server"
	 */
	public RVLogger(String token) {
		this(token, PRODUCTION_APP_ID.equalsIgnoreCase(ApiProxy.getCurrentEnvironment() != null ? 
		    ApiProxy.getCurrentEnvironment().getAppId() : null) ? "prod" : "test");
	}

	/**
	 * Use this constructor if you need to set token and environment manually, otherwise use RVLogger().
	 * 
	 * @param token - a name that clearly identifies your app. Example "core", "store", "store-server"
	 * @param environment - deployment environment. Example "prod", "test", "stage".
	 */
	public RVLogger(String token, String environment) {
		this.token = token;
		this.environment = environment;
		this.appId = ApiProxy.getCurrentEnvironment() != null ? ApiProxy.getCurrentEnvironment().getAppId() : null;
		this.appModule = ApiProxy.getCurrentEnvironment() != null ? ApiProxy.getCurrentEnvironment().getModuleId() : null;
		this.appVersion = ApiProxy.getCurrentEnvironment() != null ? ApiProxy.getCurrentEnvironment().getVersionId() : null;
	}

	private void enqueue(String severity, String errorMessage, String errorDetails) {
		
		enqueue(QueueTask.SUBMIT, this.token, this.environment, severity, errorMessage,  errorDetails);
		
	}

	public static void enqueue(String task, String token, String environment, String severity, String errorMessage, String errorDetails) {

		try {

			Queue queue = QueueFactory.getQueue(QUEUE_NAME_LOGGER);

			TaskOptions taskOptions = TaskOptions.Builder.withUrl("/queue")
					.header("Host", ModulesServiceFactory.getModulesService().getVersionHostname("logger", null))
					.param(QueryParam.TASK, task)
					.param(QueryParam.LOGGER_VERSION, LOGGER_VERSION)
					.param(QueryParam.TOKEN, token)
					.param(QueryParam.ENVIRONMENT, environment)
					.param(QueryParam.SEVERITY, severity)
					.param(QueryParam.ERROR_MESSAGE, errorMessage)
					.param(QueryParam.ERROR_DETAILS, errorDetails)
					.method(Method.POST);

			queue.add(taskOptions);
			
		} catch (Exception e) {
			log.warning(e.getMessage());
		}
		
	}

	private void log(String severity, String errorMessage, String errorDetails) {
		
		errorMessage = errorMessage == null ? "" : errorMessage;
		errorDetails = errorDetails == null ? "" : errorDetails;
		
		SimpleDateFormat sd = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");

		errorDetails = "App ID: " + appId + "\n" 
				+ "App Module: " + appModule + "\n"
				+ "App Version: " + appVersion + "\n"
				+ "Timestamp: " + sd.format(new Date()) + "\n"
				+ errorDetails;
		
		//limit error details to 10,000 characters just in case
		if (errorDetails.length() > 10000)
			errorDetails = errorDetails.substring(0, 9999);
				
		enqueue(severity, errorMessage, errorDetails);
	}

	// info methods

	public void info(String errorMessage) {		
		info(errorMessage, "");
	}

	public void info(String errorMessage, String errorDetails) {		
		log(Severity.INFO, errorMessage, errorDetails);
	}

	// warning methods

	public void warning(String errorMessage) {		
		warning(errorMessage, "");
	}

	public void warning(String errorMessage, String errorDetails) {		
		log(Severity.WARNING, errorMessage, errorDetails);
	}
	
	// error methods

	public void error(String errorMessage) {		
		error(errorMessage, "");
	}

	public void error(String errorMessage, String errorDetails) {		
		log(Severity.ERROR, errorMessage, errorDetails);
	}

	/*
	 * alert methods are intended for triggering notification alerts
	 */
	
	public void alert(String errorMessage) {		
		alert(errorMessage, "");
	}

	public void alert(String errorMessage, String errorDetails) {		
		log(Severity.ALERT, errorMessage, errorDetails);
	}

}
