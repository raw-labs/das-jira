package com.rawlabs.das.jira.initializer.auth;

import com.rawlabs.das.sdk.DASSdkInvalidArgumentException;

import java.util.Map;

public class DASJiraAuthStrategyFactory {
  public static DASJiraAuthStrategy createAuthStrategy(Map<String, String> options) {
    if (isBearerAuth(options)) return new DASJiraOAuth2AuthStrategy();
    else if (isBasicAuth(options)) return new DasJiraBasicAuthStrategy();
    else throw new DASSdkInvalidArgumentException("Invalid authentication option");
  }

  private static boolean isBearerAuth(Map<String, String> options) {
    return options.containsKey("personal_access_token");
  }

  private static boolean isBasicAuth(Map<String, String> options) {
    return options.containsKey("username") && options.containsKey("token");
  }
}
