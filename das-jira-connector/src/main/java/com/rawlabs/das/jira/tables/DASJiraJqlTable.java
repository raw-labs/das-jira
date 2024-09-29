package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.TableDefinition;

import java.util.Map;

public abstract class DASJiraJqlTable extends DASJiraTable {

  protected DASJiraJqlTable(Map<String, String> options) {
    super(options);
  }
}
