package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.rest.jira.ApiClient;

import java.util.Map;

public interface DASJiraAuthStrategy {
  void setupAuthentication(ApiClient apiClient, Map<String, String> options);
}
