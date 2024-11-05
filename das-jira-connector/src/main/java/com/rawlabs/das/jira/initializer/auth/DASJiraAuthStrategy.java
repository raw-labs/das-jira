package com.rawlabs.das.jira.initializer.auth;

import java.util.Map;

public interface DASJiraAuthStrategy {
  void setupAuthentication(Map<String, String> options);
}
