package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;

import javax.annotation.Nullable;
import java.util.List;

public interface DASJiraColumnDefinition<T> {
  void updateRow(Row.Builder rowBuilder, T object);

  ColumnDefinition getColumnDefinition();

  String getName();

  List<ColumnDefinition> getChildColumns();
}
