package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.Map;

public class DASJiraPriorityTable extends DASJiraTable {
  public DASJiraPriorityTable(Map<String, String> options) {
    super("", options);
  }

  @Override
  protected TableDefinition buildTableDefinition() {
    return null;
  }
}
