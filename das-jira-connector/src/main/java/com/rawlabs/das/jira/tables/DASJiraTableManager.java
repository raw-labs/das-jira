package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.tables.definitions.*;
import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraBoardTable;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.List;
import java.util.Map;

public class DASJiraTableManager {
  private final List<DASJiraBaseTable> tables;
  private final List<TableDefinition> tableDefinitions;

  public DASJiraTableManager(Map<String, String> options) {
    tables =
        List.of(
            new DASJiraAdvancedSettingsTable(options),
            new DASJiraBacklogIssueTable(options),
            new DASJiraBoardTable(options),
            new DASJiraComponentTable(options),
            new DASJiraDashboardTable(options),
            new DASJiraEpicTable(options),
            new DASJiraGlobalSettingTable(options),
            new DASJiraIssueTable(options),
            new DASJiraIssueTypeTable(options),
            new DASJiraIssueWorklogTable(options),
            new DASJiraPriorityTable(options),
            new DASJiraProjectRoleTable(options),
            new DASJiraProjectTable(options),
            new DASJiraSprintTable(options),
            new DASJiraUserTable(options),
            new DASJiraWorkflowTable(options));
    tableDefinitions = tables.stream().map(DASJiraBaseTable::getTableDefinition).toList();
  }

  public List<DASJiraBaseTable> getTables() {
    return tables;
  }

  public List<TableDefinition> getTableDefinitions() {
    return tableDefinitions;
  }

  public DASJiraBaseTable getTable(String tableName) {
    return tables.stream()
        .filter(table -> table.getTableName().equals(tableName))
        .findFirst()
        .orElse(null);
  }
}
