package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.jira.rest.platform.ApiClient;
import com.rawlabs.das.jira.rest.platform.Configuration;
import com.rawlabs.das.jira.rest.platform.auth.OAuth;

import java.util.Map;

public class DASJiraOAuth2AuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "OAuth2";

  public void setupPlatformAuthentication(
      com.rawlabs.das.jira.rest.platform.ApiClient apiClient, Map<String, String> options) {
    OAuth oauth = (OAuth) apiClient.getAuthentication(NAME);
    oauth.setAccessToken(options.get("personal_access_token"));
  }

  public void setupSoftwareAuthentication(
      com.rawlabs.das.jira.rest.software.ApiClient apiClient, Map<String, String> options) {
    com.rawlabs.das.jira.rest.software.auth.OAuth oauth =
        (com.rawlabs.das.jira.rest.software.auth.OAuth) apiClient.getAuthentication(NAME);
    oauth.setAccessToken(options.get("personal_access_token"));
  }
}
