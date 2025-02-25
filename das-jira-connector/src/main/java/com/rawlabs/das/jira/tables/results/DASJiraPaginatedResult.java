package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.DASExecuteResult;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;

public abstract class DASJiraPaginatedResult<T> implements DASExecuteResult {

  /**
   * A map holding the inverse of the "expand" query parameters. (e.g. maps "assignee" to
   * "fields.customfield_10000", taking the first mapping if multiple exist)
   */
  private Map<String, String> names;

  protected Iterator<T> currentPage;
  protected long currentCount = 0;
  protected Long totalCount;
  protected final Long limit;

  /**
   * In the constructor we immediately fetch the first page. This way, if there’s a problem (e.g.
   * wrong parameters or permissions) an exception is thrown right away.
   *
   * @param limit an optional upper bound on the number of items to fetch
   */
  public DASJiraPaginatedResult(@Nullable Long limit) {
    this.limit = limit;
    DASJiraPage<T> firstPage = fetchPage(0);
    if (isResultEmpty(firstPage)) {
      currentPage = Collections.emptyIterator();
    } else {
      currentCount = firstPage.result().size();
      totalCount = firstPage.total();
      currentPage = firstPage.result().iterator();
      if (firstPage.names() != null && names == null) {
        names = new HashMap<>();
        firstPage
            .names()
            .forEach(
                (k, v) -> {
                  if (!names.containsKey(v)) {
                    names.put(v, k);
                  }
                });
      }
    }
  }

  /**
   * Fetches a page starting at the given offset. Implementations must provide the logic for
   * fetching a single page and throw a SDK exception.
   */
  public abstract DASJiraPage<T> fetchPage(long offset);

  /**
   * Iterates lazily over the pages. If the current page is exhausted and the limit or total count
   * hasn't been reached, this method will fetch the next page.
   */
  @Override
  public boolean hasNext() {
    while (!currentPage.hasNext()) {
      if (totalReached() || limitReached()) {
        return false;
      }
      DASJiraPage<T> nextPage = fetchPage(currentCount);
      if (isResultEmpty(nextPage)) {
        return false;
      }
      currentCount += nextPage.result().size();
      totalCount = nextPage.total();
      currentPage = nextPage.result().iterator();
      if (nextPage.names() != null && names == null) {
        names = new HashMap<>();
        nextPage
            .names()
            .forEach(
                (k, v) -> {
                  if (!names.containsKey(v)) {
                    names.put(v, k);
                  }
                });
      }
    }
    return true;
  }

  public T getNext() {
    if (hasNext()) {
      return currentPage.next();
    }
    throw new IllegalStateException("No more elements");
  }

  protected boolean isResultEmpty(DASJiraPage<T> result) {
    return result.result().isEmpty();
  }

  protected boolean totalReached() {
    return totalCount != null && currentCount >= totalCount;
  }

  protected boolean limitReached() {
    return limit != null && currentCount >= limit;
  }

  public Map<String, String> names() {
    return names;
  }

  @Override
  public void close() {
    // Implement any cleanup if needed
  }
}
