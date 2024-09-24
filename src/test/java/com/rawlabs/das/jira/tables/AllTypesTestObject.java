package com.rawlabs.das.jira.tables;

import java.util.List;

public class AllTypesTestObject {

  public AllTypesTestObject() {}

  public String getStringField() {
    return "string";
  }

  public int getIntField() {
    return 1;
  }

  public boolean getBooleanField() {
    return true;
  }

  public List<String> getListField() {
    return List.of("a", "b", "c");
  }
}
