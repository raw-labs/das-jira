package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategy;
import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategyFactory;
import com.rawlabs.das.jira.rest.platform.ApiClient;
import com.rawlabs.das.jira.rest.platform.Configuration;

import java.util.Map;

public class DASJiraInitializer {

  public static void initialize(Map<String, String> options) {
    setupPlatformApi(options);
    setupSoftwareApi(options);
  }

  private static void setupPlatformApi(Map<String, String> options){
    ApiClient apiClient = Configuration.getDefaultApiClient();
    apiClient.setBasePath(options.get("base_url"));
    DASJiraAuthStrategy auth = DASJiraAuthStrategyFactory.createAuthStrategy(options);
    auth.setupAuthentication(options);
  }

  private static void setupSoftwareApi(Map<String, String> options){
    com.rawlabs.das.jira.rest.software.ApiClient apiClient = com.rawlabs.das.jira.rest.software.Configuration.getDefaultApiClient();
    apiClient.setBasePath(options.get("base_url"));
    DASJiraAuthStrategy auth = DASJiraAuthStrategyFactory.createAuthStrategy(options);
    auth.setupAuthentication(options);
  }
}
