package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.rest.jira.ApiClient;
import com.rawlabs.das.rest.jira.auth.OAuth;

import java.util.Map;

public class DASJiraOAuth2AuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "OAuth2";

  @Override
  public void setupAuthentication(ApiClient apiClient, Map<String, String> options) {
    OAuth oauth = (OAuth) apiClient.getAuthentication(NAME);
    oauth.setAccessToken(options.get("personal_access_token"));
  }
}
