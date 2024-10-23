package com.rawlabs.das.jira.tables.results;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;

public abstract class DASJiraPaginatedResultWithNames<T> extends DASJiraPaginatedResult<T> {

  public DASJiraPaginatedResultWithNames(Long limit) {
    super(limit);
  }

  private BiMap<String, String> names;

  @Override
  public boolean hasNext() {
    while (isPageExhausted()) {
      DASJiraPage<T> result = fetchPage(currentCount);
      if (names == null) {
        names = HashBiMap.create(result.names()).inverse();
      }
      currentCount += result.result().size();
      totalCount = result.total();
      currentPage = result.result().iterator();
    }
    return currentPageHasNext() && totalNotReached() && limitNotReached();
  }

  public Map<String, String> names() {
    return names;
  }
}
