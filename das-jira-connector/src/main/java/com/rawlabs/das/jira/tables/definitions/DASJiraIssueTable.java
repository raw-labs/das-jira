package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.rest.platform.api.UsersApi;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DASJiraIssueTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue";

  private IssuesApi issuesApi = new IssuesApi();

  public DASJiraIssueTable(Map<String, String> options) {
    super(
        options, TABLE_NAME, "Issues help manage code, estimate workload, and keep track of team.");
  }

  /** Constructor for mocks */
  DASJiraIssueTable(Map<String, String> options, IssuesApi issuesApi) {
    this(options);
    this.issuesApi = issuesApi;
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1));
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    return Map.of();
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }
}
