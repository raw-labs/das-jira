package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategy;
import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategyFactory;
import com.rawlabs.das.rest.jira.ApiClient;
import com.rawlabs.das.rest.jira.Configuration;

import java.util.Map;

public class DASJiraInitializer {

  public static void initialize(Map<String, String> options) {
    ApiClient apiClient = new ApiClient();
    setupJiraUrl(apiClient, options);
    DASJiraAuthStrategy auth = DASJiraAuthStrategyFactory.createAuthStrategy(options);
    auth.setupAuthentication(apiClient, options);
    Configuration.setDefaultApiClient(apiClient);
  }

  private static void setupJiraUrl(ApiClient apiClient, Map<String, String> options) {
    apiClient.setBasePath(options.get("base_url"));
  }
}
