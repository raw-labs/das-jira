package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.DASExecuteResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;

public abstract class DASJiraPaginatedResult<T> implements DASExecuteResult {

  /**
   * These are the names returned by "expand" query params. They are a map from the field to its
   * name. The inverse of it allows to get the field name from the name. (E.g. "assignee" ->
   * "fields.customfield_10000") It is a multimap because the same name can be used for different
   * fields. We use always the first one.
   */
  private Map<String, String> names;

  protected Iterator<T> currentPage = null;

  protected long currentCount = 0;
  protected Long totalCount;
  protected final Long limit;

  public DASJiraPaginatedResult(@Nullable Long limit) {
    this.limit = limit;
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
      if (totalReached() || limitReached()) {
        return false;
      }
      DASJiraPage<T> result = fetchPage(currentCount);
      if (isResultEmpty(result)) {
        return false;
      }
      if (names == null && result.names() != null) {
        names = new HashMap<>();
        result
            .names()
            .forEach(
                (k, v) -> {
                  if (!names.containsKey(v)) {
                    names.put(v, k);
                  }
                });
      }
      currentCount += result.result().size();
      totalCount = result.total();
      currentPage = result.result().iterator();
    }
    return true;
  }

  protected boolean isResultEmpty(DASJiraPage<T> result) {
    return result.result().isEmpty();
  }

  protected boolean isPageExhausted() {
    return (currentPage == null || !currentPage.hasNext());
  }

  protected boolean totalReached() {
    return totalCount != null && currentCount >= totalCount;
  }

  protected boolean limitReached() {
    if (limit == null) {
      return false;
    }
    return currentCount >= limit;
  }

  public Map<String, String> names() {
    return names;
  }

  @Override
  public void close() {}
}
