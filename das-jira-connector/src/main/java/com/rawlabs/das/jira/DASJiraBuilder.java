package com.rawlabs.das.jira;

import com.rawlabs.das.sdk.java.DASSdk;
import com.rawlabs.das.sdk.java.DASSdkBuilder;
import com.rawlabs.utils.core.RawSettings;

import java.util.Map;

public class DASJiraBuilder implements DASSdkBuilder {

  @Override
  public String getDasType() {
    return "jira";
  }

  @Override
  public DASSdk build(Map<String, String> options, RawSettings rawSettings) {
    return new DASJira(options);
  }
}
