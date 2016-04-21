package com.risevision.dap;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public class Globals {

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new GsonFactory();
	public static final String BIGQUERY_SCOPE = "https://www.googleapis.com/auth/bigquery";

	public static final String PROJECT_ID = "rise-core-log";

	public static final RVLogger LOGGER = new RVLogger();

	public static final String PRODUCTION_APP_ID = "s~rvaserver2";

}
