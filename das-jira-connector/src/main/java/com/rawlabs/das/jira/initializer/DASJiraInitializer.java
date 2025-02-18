package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategy;
import com.rawlabs.das.jira.initializer.auth.DASJiraAuthStrategyFactory;
import java.util.Map;

public class DASJiraInitializer {

  public static com.rawlabs.das.jira.rest.platform.ApiClient initializePlatform(
      Map<String, String> options) {
    if (options.get("base_url") == null) {
      throw new IllegalArgumentException("base_url is required");
    }
    com.rawlabs.das.jira.rest.platform.ApiClient apiClient =
        new com.rawlabs.das.jira.rest.platform.ApiClient();
    apiClient.setBasePath(options.get("base_url"));
    DASJiraAuthStrategy auth = DASJiraAuthStrategyFactory.createAuthStrategy(options);
    auth.setupPlatformAuthentication(apiClient, options);
    return apiClient;
  }

  public static com.rawlabs.das.jira.rest.software.ApiClient initializeSoftware(
      Map<String, String> options) {
    if (options.get("base_url") == null) {
      throw new IllegalArgumentException("base_url is required");
    }
    com.rawlabs.das.jira.rest.software.ApiClient apiClient =
        new com.rawlabs.das.jira.rest.software.ApiClient();
    apiClient.setBasePath(options.get("base_url"));
    DASJiraAuthStrategy auth = DASJiraAuthStrategyFactory.createAuthStrategy(options);
    auth.setupSoftwareAuthentication(apiClient, options);
    return apiClient;
  }
}
