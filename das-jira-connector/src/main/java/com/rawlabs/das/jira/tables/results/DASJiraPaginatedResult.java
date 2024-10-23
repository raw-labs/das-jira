package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.java.DASExecuteResult;

import javax.annotation.Nullable;
import java.util.Iterator;

public abstract class DASJiraPaginatedResult<T> implements DASExecuteResult {

  protected long currentCount = 0;
  protected long totalCount = 0;
  protected Iterator<T> currentPage = null;
  protected final Long limit;

  public DASJiraPaginatedResult(@Nullable Long limit) {
    this.limit = limit;
  }

  protected boolean isPageExhausted() {
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
    return currentPageHasNext() && totalNotReached() && limitNotReached();
  }

  protected boolean currentPageHasNext() {
    return currentPage != null && currentPage.hasNext();
  }

  protected boolean totalNotReached() {
    return currentCount <= totalCount;
  }

  protected boolean limitNotReached() {
    if (limit == null) {
      return true;
    }
    return currentCount <= limit;
  }

  @Override
  public void close() {}
}
