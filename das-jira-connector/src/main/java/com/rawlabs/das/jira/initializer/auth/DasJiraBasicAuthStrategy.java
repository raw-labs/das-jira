package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.jira.rest.platform.auth.HttpBasicAuth;
import java.util.Base64;
import java.util.Map;

public class DasJiraBasicAuthStrategy implements DASJiraAuthStrategy {

  public static final String NAME = "basicAuth";

  public void setupPlatformAuthentication(
      com.rawlabs.das.jira.rest.platform.ApiClient apiClient, Map<String, String> options) {
    HttpBasicAuth basicAuth = (HttpBasicAuth) apiClient.getAuthentication(NAME);
    basicAuth.setUsername(options.get("username"));
    basicAuth.setPassword(options.get("token"));
  }

  public void setupSoftwareAuthentication(
      com.rawlabs.das.jira.rest.software.ApiClient apiClient, Map<String, String> options) {
    apiClient.addDefaultHeader(
        "Authorization",
        "Basic "
            + Base64.getEncoder()
                .encodeToString(
                    "%s:%s".formatted(options.get("username"), options.get("token")).getBytes()));
    //    com.rawlabs.das.jira.rest.software.auth.HttpBasicAuth basicAuth =
    //        (com.rawlabs.das.jira.rest.software.auth.HttpBasicAuth)
    // apiClient.getAuthentication(NAME);
    //    basicAuth.setUsername(options.get("username"));
    //    basicAuth.setPassword(options.get("token"));
  }
}
