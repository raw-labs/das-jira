package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.HydrateFunction;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;

public class DASJiraTableDefinition {
  List<DASJiraColumnDefinition> columnDefinitions;
  TableDefinition tableDefinition;
  HydrateFunction hydrateFunction;

  public DASJiraTableDefinition(
      String name,
      String description,
      List<DASJiraColumnDefinition> columnDefinitions,
      HydrateFunction hydrateFunction) {
    this.columnDefinitions = columnDefinitions;
    tableDefinition =
        createTable(
            name,
            description,
            columnDefinitions.stream().map(DASJiraColumnDefinition::getColumnDefinition).toList());
    this.hydrateFunction = hydrateFunction;
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  public void updateRow(Row.Builder rowBuilder, Object value) {
    columnDefinitions.forEach((columnDefinition) -> columnDefinition.putToRow(value, rowBuilder));
  }

  public Iterator<Object> hydrate(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return hydrateFunction.hydrate(quals, columns, sortKeys, limit);
  }
}
