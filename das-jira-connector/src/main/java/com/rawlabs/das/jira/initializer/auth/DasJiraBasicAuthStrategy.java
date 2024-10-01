package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.jira.rest.platform.ApiClient;
import com.rawlabs.das.jira.rest.platform.Configuration;
import com.rawlabs.das.jira.rest.platform.auth.HttpBasicAuth;

import java.util.Map;

public class DasJiraBasicAuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "basicAuth";

  @Override
  public void setupAuthentication(Map<String, String> options) {
    setupPlatformAuthentication(options);
    setupSoftwareAuthentication(options);
  }

  private void setupPlatformAuthentication(Map<String, String> options) {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    HttpBasicAuth basicAuth = (HttpBasicAuth) apiClient.getAuthentication(NAME);
    basicAuth.setUsername(options.get("username"));
    basicAuth.setPassword(options.get("token"));
  }

  private void setupSoftwareAuthentication(Map<String, String> options) {
    com.rawlabs.das.jira.rest.software.ApiClient apiClient =
        com.rawlabs.das.jira.rest.software.Configuration.getDefaultApiClient();
    com.rawlabs.das.jira.rest.software.auth.HttpBasicAuth basicAuth =
        (com.rawlabs.das.jira.rest.software.auth.HttpBasicAuth) apiClient.getAuthentication(NAME);
    basicAuth.setUsername(options.get("username"));
    basicAuth.setPassword(options.get("token"));
  }
}
