//package com.rawlabs.das.jira.tables;
//
//import com.rawlabs.protocol.das.Row;
//
//import java.util.List;
//
//public class DASJiraTableDefinitionChunk<T> {
//  List<DASJiraNormalColumnDefinition<T>> columnDefinitions;
//
//  public DASJiraTableDefinitionChunk(List<DASJiraNormalColumnDefinition<T>> columnDefinitions) {
//    this.columnDefinitions = columnDefinitions;
//  }
//
//  public List<DASJiraNormalColumnDefinition<T>> getColumnDefinitions() {
//    return columnDefinitions;
//  }
//
//  public void updateRow(Row.Builder rowBuilder, T value) {
//    columnDefinitions.forEach((columnDefinition) -> columnDefinition.putToRow(value, rowBuilder));
//  }
//}
