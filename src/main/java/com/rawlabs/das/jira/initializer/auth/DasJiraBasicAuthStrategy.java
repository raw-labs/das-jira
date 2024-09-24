package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.rest.jira.ApiClient;
import com.rawlabs.das.rest.jira.auth.HttpBasicAuth;

import java.util.Map;

public class DasJiraBasicAuthStrategy implements DASJiraAuthStrategy {
  @Override
  public void setupAuthentication(ApiClient apiClient, Map<String, String> options) {
    HttpBasicAuth basicAuth = (HttpBasicAuth) apiClient.getAuthentication("basicAuth");
    basicAuth.setUsername(options.get("username"));
    basicAuth.setPassword(options.get("token"));
  }
}
