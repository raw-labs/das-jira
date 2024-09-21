package com.rawlabs.das.jira;

import com.rawlabs.das.sdk.java.DASFunction;
import com.rawlabs.das.sdk.java.DASSdk;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.protocol.das.FunctionDefinition;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.List;
import java.util.Map;

public class DASJira implements DASSdk {

  private final Map<String, String> options;

  protected DASJira(Map<String, String> options) {
    this.options = options;
  }

  @Override
  public List<TableDefinition> getTableDefinitions() {
    return List.of();
  }

  @Override
  public List<FunctionDefinition> getFunctionDefinitions() {
    return List.of();
  }

  @Override
  public DASTable getTable(String s) {
    return null;
  }

  @Override
  public DASFunction getFunction(String s) {
    return null;
  }
}
