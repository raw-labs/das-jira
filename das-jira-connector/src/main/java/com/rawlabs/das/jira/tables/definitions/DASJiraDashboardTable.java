package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DASJiraDashboardTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_dashboard";

  public DASJiraDashboardTable(Map<String, String> options) {
    super(
        options, TABLE_NAME, "Your dashboard is the main display you see when you log in to Jira.");
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    return Map.of();
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }
}
