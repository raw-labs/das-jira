package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.jira.rest.platform.ApiClient;
import com.rawlabs.das.jira.rest.platform.Configuration;
import com.rawlabs.das.jira.rest.platform.auth.OAuth;

import java.util.Map;

public class DASJiraOAuth2AuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "OAuth2";

  @Override
  public void setupAuthentication(Map<String, String> options) {
    setupPlatformAuthentication(options);
    setupSoftwareAuthentication(options);
  }

  private void setupPlatformAuthentication(Map<String, String> options) {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    OAuth oauth = (OAuth) apiClient.getAuthentication(NAME);
    oauth.setAccessToken(options.get("personal_access_token"));
  }

  private void setupSoftwareAuthentication(Map<String, String> options) {
    com.rawlabs.das.jira.rest.software.ApiClient apiClient =
        com.rawlabs.das.jira.rest.software.Configuration.getDefaultApiClient();
    com.rawlabs.das.jira.rest.software.auth.OAuth oauth =
        (com.rawlabs.das.jira.rest.software.auth.OAuth) apiClient.getAuthentication(NAME);
    oauth.setAccessToken(options.get("personal_access_token"));
  }
}
