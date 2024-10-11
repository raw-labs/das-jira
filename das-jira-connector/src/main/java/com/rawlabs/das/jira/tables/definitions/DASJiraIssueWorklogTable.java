package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
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

public class DASJiraIssueWorklogTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_worklog";

  private IssueWorklogsApi issueWorklogsApi = new IssueWorklogsApi();

  public DASJiraIssueWorklogTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "Jira worklog is a feature within the Jira software that allows users to record the amount of time they have spent working on various tasks or issues.");
  }

  /** Constructor for mocks */
  DASJiraIssueWorklogTable(Map<String, String> options, IssueWorklogsApi issueWorklogsApi) {
    this(options);
    this.issueWorklogsApi = issueWorklogsApi;
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
