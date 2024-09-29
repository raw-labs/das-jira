package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;

public class DASJiraTableDefinition {
  Map<String, DASJiraColumnDefinition> columnDefinitions;

  TableDefinition tableDefinition;

  public DASJiraTableDefinition(
      String name, String description, Map<String, DASJiraColumnDefinition> columnDefinitions) {
    tableDefinition =
        createTable(
            name,
            description,
            columnDefinitions.values().stream()
                .map(DASJiraColumnDefinition::getColumnDefinition)
                .toList());
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  public void updateRow(String name, Row.Builder rowBuilder, Object value) {
    columnDefinitions.get(name).putToRow(value, rowBuilder);
  }
}
