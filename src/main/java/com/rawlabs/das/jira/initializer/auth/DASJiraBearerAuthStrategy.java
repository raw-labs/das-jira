package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.rest.jira.ApiClient;
import com.rawlabs.das.rest.jira.auth.HttpBearerAuth;

import java.util.Map;

public class DASJiraBearerAuthStrategy implements DASJiraAuthStrategy {
  @Override
  public void setupAuthentication(ApiClient apiClient, Map<String, String> options) {
    HttpBearerAuth bearerAuth = (HttpBearerAuth) apiClient.getAuthentication("OAuth2");
    bearerAuth.setBearerToken(options.get("personal_access_token"));
  }
}
