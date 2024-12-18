package com.rawlabs.das.jira.initializer.auth;

import java.util.Map;

public interface DASJiraAuthStrategy {
  void setupPlatformAuthentication(com.rawlabs.das.jira.rest.platform.ApiClient apiClient, Map<String, String> options);
  void setupSoftwareAuthentication(com.rawlabs.das.jira.rest.software.ApiClient apiClient, Map<String, String> options);
}
