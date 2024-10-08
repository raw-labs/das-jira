package com.rawlabs.das.jira.tables.page;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.Row;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

public abstract class DASJiraPagedResult<T> implements DASExecuteResult {

  private long currentCount = 0;
  private long totalCount = 0;
  private Iterator<T> currentPage = null;

  Function<Long, DASJiraPage<T>> fetchPage;

  public DASJiraPagedResult(Function<Long, DASJiraPage<T>> fetchPage) {
    this.fetchPage = fetchPage;
  }

  private boolean isPageExhausted() {
    return (currentPage == null || !currentPage.hasNext());
  }

  public T getNext() {
    if (hasNext()) {
      return currentPage.next();
    } else throw new IllegalStateException("No more elements");
  }

  @Override
  public boolean hasNext() {
    while (isPageExhausted()) {
      DASJiraPage<T> result = fetchPage.apply(currentCount);
      currentCount += result.result().size();
      totalCount = result.total();
      currentPage = result.result().iterator();
    }
    return currentPage != null && currentPage.hasNext() && currentCount <= totalCount;
  }

  @Override
  public void close() throws IOException {}
}
