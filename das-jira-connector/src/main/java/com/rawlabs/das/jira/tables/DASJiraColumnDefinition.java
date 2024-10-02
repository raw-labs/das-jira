package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;

import java.util.List;

public interface DASJiraColumnDefinition<T> {
  void putToRow(T object, Row.Builder rowBuilder);

  ColumnDefinition getColumnDefinition();

  List<ColumnDefinition> getChildColumns();
}
