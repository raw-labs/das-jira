package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategy;
import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategyFactory;
import com.rawlabs.das.jira.rest.ApiClient;
import com.rawlabs.das.jira.rest.Configuration;

import java.util.Map;

public class DASJiraInitializer {

  public static void initialize(Map<String, String> options) {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    apiClient.setBasePath(options.get("base_url"));
    DASJiraAuthStrategy auth = DASJiraAuthStrategyFactory.createAuthStrategy(options);
    auth.setupAuthentication(options);
  }
}
