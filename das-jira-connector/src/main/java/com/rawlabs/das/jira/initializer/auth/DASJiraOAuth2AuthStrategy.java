package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.jira.rest.ApiClient;
import com.rawlabs.das.jira.rest.Configuration;
import com.rawlabs.das.jira.rest.auth.OAuth;

import java.util.Map;

public class DASJiraOAuth2AuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "OAuth2";

  @Override
  public void setupAuthentication(Map<String, String> options) {
    ApiClient apiClient = Configuration.getDefaultApiClient();
    OAuth oauth = (OAuth) apiClient.getAuthentication(NAME);
    oauth.setAccessToken(options.get("personal_access_token"));
  }
}
