package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;

import java.io.IOException;
import java.util.List;

public abstract class DASJiraWithParentTableResult implements DASExecuteResult {

  private final DASExecuteResult parentResult;
  private DASExecuteResult childResult;

    public DASJiraWithParentTableResult(
      DASTable parentTable,
      List<Qual> quals,
      List<String> columns,
      List<SortKey> sortKeys,
      Long limit) {
    try (DASExecuteResult parentResult = parentTable.execute(quals, columns, sortKeys, limit)) {
      this.parentResult = parentResult;
    } catch (IOException e) {
      throw new DASSdkException("Failed to execute parent table", e);
    }
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
