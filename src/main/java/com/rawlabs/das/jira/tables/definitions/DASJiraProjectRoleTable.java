package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.Map;

public class DASJiraProjectRoleTable extends DASJiraTable {
  public DASJiraProjectRoleTable(Map<String, String> options) {
    super("", options);
  }

  @Override
  protected TableDefinition buildTableDefinition() {
    return null;
  }
}
