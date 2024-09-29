//package com.rawlabs.das.jira.tables;
//
//import com.rawlabs.protocol.das.Row;
//import com.rawlabs.protocol.das.TableDefinition;
//
//import java.util.List;
//
//import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;
//
//public class DASJiraTableSubDefinition<T> {
//
//  private final List<DASJiraColumnDefinition<T>> columns;
//
//  private final TableDefinition tableDefinition;
//
//  public DASJiraTableSubDefinition(
//      String tableName, String description, List<DASJiraColumnDefinition<T>> columns) {
//    this.columns = columns;
//    tableDefinition =
//        createTable(
//            tableName,
//            description,
//            columns.stream().map(DASJiraColumnDefinition::getColumnDefinition).toList());
//  }
//
//  public TableDefinition getTableDefinition() {
//    return tableDefinition;
//  }
//
//  public List<DASJiraColumnDefinition<T>> getColumns() {
//    return columns;
//  }
//
//  public void updateRow(T object, Row.Builder rowBuilder) {
//    for (DASJiraColumnDefinition<T> column : columns) {
//      rowBuilder.putData(column.getName(), column.getValue(object));
//    }
//  }
//}
