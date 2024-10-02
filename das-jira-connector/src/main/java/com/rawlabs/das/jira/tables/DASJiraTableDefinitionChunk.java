package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.Row;

import java.util.List;

public class DASJiraTableDefinitionChunk<T> {
  List<DASJiraColumnDefinition<T>> columnDefinitions;

  public DASJiraTableDefinitionChunk(List<DASJiraColumnDefinition<T>> columnDefinitions) {
    this.columnDefinitions = columnDefinitions;
  }

  public List<DASJiraColumnDefinition<T>> getColumnDefinitions() {
    return columnDefinitions;
  }

  public void updateRow(Row.Builder rowBuilder, T value) {
    columnDefinitions.forEach((columnDefinition) -> columnDefinition.putToRow(value, rowBuilder));
  }
}
