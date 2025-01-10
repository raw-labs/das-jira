package com.rawlabs.das.jira;

import com.rawlabs.das.jira.initializer.DASJiraInitializer;
import com.rawlabs.das.jira.tables.DASJiraTableManager;
import com.rawlabs.das.sdk.DASFunction;
import com.rawlabs.das.sdk.DASSdk;
import com.rawlabs.das.sdk.DASTable;
import com.rawlabs.protocol.das.v1.functions.FunctionDefinition;
import com.rawlabs.protocol.das.v1.tables.TableDefinition;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DASJira implements DASSdk {

  private final Map<String, String> options;
  private final DASJiraTableManager tableManager;

  protected DASJira(Map<String, String> options) {
    var apiClientPlatform = DASJiraInitializer.initializePlatform(options);
    var apiClientSoftware = DASJiraInitializer.initializeSoftware(options);
    tableManager = new DASJiraTableManager(options, apiClientPlatform, apiClientSoftware);
    this.options = options;
  }

  public List<TableDefinition> getTableDefinitions() {
    return tableManager.getTableDefinitions();
  }

  public List<FunctionDefinition> getFunctionDefinitions() {
    return List.of();
  }

  public Optional<DASTable> getTable(String name) {
    return tableManager.getTable(name).map(table -> (DASTable) table);
  }

  public Optional<DASFunction> getFunction(String name) {
    return Optional.empty();
  }
}
