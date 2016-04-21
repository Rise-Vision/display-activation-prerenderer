package com.risevision.dap;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobStatistics;
import com.google.api.services.bigquery.model.JobStatistics2;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.common.collect.ImmutableMap;

public class CheckStatus extends QueueTaskImplementation {

	private static final long serialVersionUID = 8783237272984310242L;

	public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String jobId = request.getParameter(QueryParam.ID);

		if (StringUtils.isBlank(jobId)) {
			getLogger().warning("Job ID is not provided, exiting.");
			return;
		}

		checkJob(jobId);

	}

	private void checkJob(String jobId) throws Exception {

		getLogger().info("Checking status of " + jobId);

		try {

			Environment env = ApiProxy.getCurrentEnvironment();
			String appId = env.getAppId();

			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(Globals.BIGQUERY_SCOPE));

			Bigquery bigquery = new Bigquery.Builder(Globals.HTTP_TRANSPORT, Globals.JSON_FACTORY, credential).setApplicationName(appId).build();

			Job job = bigquery.jobs().get(Globals.PROJECT_ID, jobId).execute();

			String state = job.getStatus().getState();
			ErrorProto error = job.getStatus().getErrorResult();
			JobStatistics stats = job.getStatistics();
			JobStatistics2 loadStats = stats != null ? stats.getQuery() : null;

			getLogger().info("Job " + jobId + " state: " + state + 
					(error != null ? " error: " + error.toString() : "") + 
					(loadStats != null ? " bytes processed: " + loadStats.getTotalBytesProcessed() + " bytes billed: " + loadStats.getTotalBytesBilled() : ""));

			if ("RUNNING".equals(state)) {

				QueueTask.CHECK_STATUS.enqueue(ImmutableMap.of(QueryParam.ID, jobId), 30000);

			}

		} catch (Exception e) {

			Utils.remoteLogException(e, "Error trying to check job status");
			Utils.logException(e);

		}
	}
}
