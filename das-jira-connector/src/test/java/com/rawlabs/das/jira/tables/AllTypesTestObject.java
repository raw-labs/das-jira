package com.rawlabs.das.jira.tables;

import java.util.List;

public class AllTypesTestObject {

  public AllTypesTestObject(int id) {
    this.stringField = "string" + id;
    this.intField = id;
    this.listField = List.of("a" + id, "b" + id, "c" + id);
  }

  private String stringField;
  private int intField;
  private boolean booleanField = true;
  private List<String> listField;

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
