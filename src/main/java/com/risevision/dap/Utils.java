package com.risevision.dap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.apphosting.api.ApiProxy;

public class Utils {

	static public void logStackTrace(Exception e) {

		Logger log = Logger.getAnonymousLogger();
		Utils.logStackTrace(e, log);

	}

	static public void logStackTrace(Exception e, Logger log) {

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log.warning(sw.toString());

	}

	static public void logException(Exception e) {

		Logger log = Logger.getAnonymousLogger();
		log.warning("Error: " + e.toString() + ", " +  e.getMessage());
		Utils.logStackTrace(e, log);

	}

	static public void remoteLogException(Exception exception, String additionalDetails) {

		String msg = String.format("Rise Display Activity Extractor, unexpected '%s'. MSG: '%s'", 
				exception.getClass().getSimpleName(), exception.toString() != null ? exception.toString() : "none");

		if (additionalDetails == null)
			additionalDetails = "";

		String details = additionalDetails + "\n" + ExceptionUtils.getStackTrace(exception);

		Globals.LOGGER.alert(msg, details);
	}

	static public boolean isProduction() {

		return Globals.PRODUCTION_APP_ID.equalsIgnoreCase(ApiProxy.getCurrentEnvironment() != null ? ApiProxy.getCurrentEnvironment().getAppId() : null);
	}

}
