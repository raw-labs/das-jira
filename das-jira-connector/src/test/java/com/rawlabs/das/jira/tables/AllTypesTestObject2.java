package com.rawlabs.das.jira.tables;

import java.util.List;

public class AllTypesTestObject2 {
  public AllTypesTestObject2() {}

  private String stringField = "string2";
  private int intField = 1;
  private boolean booleanField = true;
  private List<String> listField = List.of("a2", "b2", "c2");

  public String getStringField() {
    return stringField;
  }

  public int getIntField() {
    return intField;
  }

  public boolean getBooleanField() {
    return booleanField;
  }

  public List<String> getListField() {
    return listField;
  }

  public void setStringField(String stringField) {
    this.stringField = stringField;
  }

  public void setIntField(int intField) {
    this.intField = intField;
  }

  public void setBooleanField(boolean booleanField) {
    this.booleanField = booleanField;
  }

  public void setListField(List<String> listField) {
    this.listField = listField;
  }
}
