package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraBaseTable;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.Map;

public class DASJiraDashboardTable extends DASJiraBaseTable {
  public DASJiraDashboardTable(Map<String, String> options) {
    super(options);
  }

  @Override
  public String getTableName() {
    return "";
  }

  @Override
  public TableDefinition getTableDefinition() {
    return null;
  }
}
