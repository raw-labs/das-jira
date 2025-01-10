package com.rawlabs.das.jira;

import com.rawlabs.das.sdk.DASSdk;
import com.rawlabs.das.sdk.DASSdkBuilder;
import com.rawlabs.das.sdk.DASSettings;
import java.util.Map;

public class DASJiraBuilder implements DASSdkBuilder {

  @Override
  public String getDasType() {
    return "jira";
  }

  public DASSdk build(Map<String, String> options, DASSettings rawSettings) {
    return new DASJira(options);
  }
}
