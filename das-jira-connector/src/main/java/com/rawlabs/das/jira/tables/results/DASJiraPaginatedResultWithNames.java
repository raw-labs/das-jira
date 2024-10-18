package com.rawlabs.das.jira.tables.results;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;

public abstract class DASJiraPaginatedResultWithNames<T> extends DASJiraPaginatedResult<T> {
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
    return currentPage != null && currentPage.hasNext() && currentCount <= totalCount;
  }

  public Map<String, String> names() {
    return names;
  }
}
