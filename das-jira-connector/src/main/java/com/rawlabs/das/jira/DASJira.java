package com.rawlabs.das.jira;

import com.rawlabs.das.jira.initializer.DASJiraInitializer;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.sdk.java.DASFunction;
import com.rawlabs.das.sdk.java.DASSdk;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.protocol.das.FunctionDefinition;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.List;
import java.util.Map;

public class DASJira implements DASSdk {

  private final Map<String, String> options;
  private final DASJiraTableManager tableManager;

  protected DASJira(Map<String, String> options) {
    DASJiraInitializer.initialize(options);
    tableManager = new DASJiraTableManager(options);
    this.options = options;
  }

  @Override
  public List<TableDefinition> getTableDefinitions() {
    return tableManager.getTableDefinitions();
  }

  @Override
  public List<FunctionDefinition> getFunctionDefinitions() {
    return List.of();
  }

  @Override
  public DASTable getTable(String name) {
    return tableManager.getTable(name);
  }

  @Override
  public DASFunction getFunction(String name) {
    return null;
  }
}
