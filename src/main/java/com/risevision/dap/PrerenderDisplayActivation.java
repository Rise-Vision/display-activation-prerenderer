package com.risevision.dap;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationQuery;
import com.google.api.services.bigquery.model.TableReference;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.common.collect.ImmutableMap;

public class PrerenderDisplayActivation extends QueueTaskImplementation {

	private static final long serialVersionUID = 4092802200850647956L;

	public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

		runBQJob();

	}

	private void runBQJob() {

		String querySql = "SELECT * FROM [dashboards.bime_displayActivation]";

		getLogger().info("Query: " + querySql);

		Environment env = ApiProxy.getCurrentEnvironment();
		String appId = env.getAppId();

		AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(Globals.BIGQUERY_SCOPE));

		Bigquery bigquery = new Bigquery.Builder(Globals.HTTP_TRANSPORT, Globals.JSON_FACTORY, credential).setApplicationName(appId).build();

		JobConfigurationQuery queryConfig = new JobConfigurationQuery()
		.setQuery(querySql)
		.setAllowLargeResults(true)
		.setUseQueryCache(false)
		.setWriteDisposition("WRITE_TRUNCATE")
		.setDestinationTable(new TableReference().setProjectId(Globals.PROJECT_ID).setDatasetId("dashboards").setTableId("displayActivationPrerendered"));


		try {

			Job job = bigquery.jobs().insert(Globals.PROJECT_ID, new Job().setConfiguration(new JobConfiguration().setQuery(queryConfig))).execute();

			String jobId = job.getJobReference().getJobId(); 
			String state = job.getStatus().getState();
			ErrorProto error = job.getStatus().getErrorResult();

			getLogger().info("Job " + jobId + " state: " + state + (error != null ? " error: " + error.toString() : ""));

			if (error != null)
				throw new BQException(error.getMessage());

			if ("RUNNING".equals(state))
				QueueTask.CHECK_STATUS.enqueue(ImmutableMap.of(QueryParam.ID, jobId), 120000); //query execution takes about 90 seconds

		} catch (BQException e) {

			String errorDetails = "BQ Error trying to prerender displayActivation query: " + e.getMessage() + ".";
			Globals.LOGGER.alert("Failure to prerender displayActivation query", errorDetails);

		} catch (Exception e) {

			Utils.remoteLogException(e, "BQ Error trying to prerender displayActivation query.");
			Utils.logException(e);

		}
	}


}
