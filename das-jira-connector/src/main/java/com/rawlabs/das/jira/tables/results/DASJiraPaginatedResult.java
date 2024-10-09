package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.java.DASExecuteResult;

import java.util.Iterator;

public abstract class DASJiraPaginatedResult<T> implements DASExecuteResult {

  private long currentCount = 0;
  private long totalCount = 0;
  private Iterator<T> currentPage = null;

  public DASJiraPaginatedResult() {}

  private boolean isPageExhausted() {
    return (currentPage == null || !currentPage.hasNext());
  }

  public T getNext() {
    if (hasNext()) {
      return currentPage.next();
    } else throw new IllegalStateException("No more elements");
  }

  public abstract DASJiraPage<T> fetchPage(long offset);

  @Override
  public boolean hasNext() {
    while (isPageExhausted()) {
      DASJiraPage<T> result = fetchPage(currentCount);
      currentCount += result.result().size();
      totalCount = result.total();
      currentPage = result.result().iterator();
    }
    return currentPage != null && currentPage.hasNext() && currentCount <= totalCount;
  }

  @Override
  public void close() {}
}
