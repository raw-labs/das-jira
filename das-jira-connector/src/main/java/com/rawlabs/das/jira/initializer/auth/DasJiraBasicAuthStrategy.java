package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.jira.rest.ApiClient;
import com.rawlabs.das.jira.rest.Configuration;
import com.rawlabs.das.jira.rest.auth.HttpBasicAuth;

import java.util.Map;

public class DasJiraBasicAuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "basicAuth";

  @Override
  public void setupAuthentication(Map<String, String> options) {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    HttpBasicAuth basicAuth = (HttpBasicAuth) apiClient.getAuthentication(NAME);
    basicAuth.setUsername(options.get("username"));
    basicAuth.setPassword(options.get("token"));
  }
}
