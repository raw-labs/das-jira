package com.rawlabs.das.jira.tables.results;

import java.util.List;
import java.util.Map;

public class DASJiraPage<T> {

  private final List<T> result;
  private final Long total;
  private final Map<String, String> names;

  public DASJiraPage(List<T> result, Long total, Map<String, String> names) {
    this.result = result;
    this.total = total;
    this.names = names;
  }

  public DASJiraPage(List<T> result, Long total) {
    this(result, total, null);
  }

  public List<T> result() {
    return result;
  }

  public Long total() {
    return total;
  }

  public Map<String, String> names() {
    return names;
  }
}
