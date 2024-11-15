package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;

import java.io.IOException;
import java.util.List;

public abstract class DASJiraWithParentTableResult implements DASExecuteResult {

  private final DASExecuteResult parentResult;
  private DASExecuteResult childResult;
  private final Long limit;
  private long currentCount = 0;

  public DASJiraWithParentTableResult(
      DASTable parentTable,
      List<Qual> quals,
      List<String> columns,
      List<SortKey> sortKeys,
      Long limit) {
    this.limit = limit;
    try (DASExecuteResult parentResult = parentTable.execute(quals, columns, sortKeys, null)) {
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
    if (limitReached()) {
      return false;
    }
    if (childResult != null && childResult.hasNext()) {
      return true;
    }
    while (parentResult.hasNext()) {
      Row currentParentRow = parentResult.next();
      try {
        childResult = fetchChildResult(currentParentRow);
        if (childResult.hasNext()) {
          return true;
        }
      } catch (DASSdkApiException _) {
      }
    }
    return false;
  }

  @Override
  public Row next() {
    currentCount++;
    return childResult.next();
  }

  protected boolean limitReached() {
    if (limit == null) {
      return false;
    }
    return currentCount >= limit;
  }
}
