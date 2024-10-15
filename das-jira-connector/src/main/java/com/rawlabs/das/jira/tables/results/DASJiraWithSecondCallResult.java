package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.Row;

public abstract class DASJiraWithSecondCallResult implements DASExecuteResult {

  private final DASExecuteResult parentResult;
  private DASExecuteResult childResult;

  public DASJiraWithSecondCallResult(DASExecuteResult parentResult) {
    this.parentResult = parentResult;
  }

  public abstract DASExecuteResult fetchChildResult(Row parentRow);

  @Override
  public void close() {}

  @Override
  public boolean hasNext() {
    if (childResult != null && childResult.hasNext()) {
      return true;
    }
    while (parentResult.hasNext()) {
      Row currentParentRow = parentResult.next();
      childResult = fetchChildResult(currentParentRow);
      if (childResult.hasNext()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Row next() {
    return childResult.next();
  }
}
