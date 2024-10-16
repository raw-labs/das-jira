package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.rest.platform.model.Worklog;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.jira.tables.results.DASJiraWithParentTableResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraIssueWorklogTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_worklog";

  private DASTable parentTable;

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

  public DASJiraIssueWorklogTable(
      Map<String, String> options, IssueWorklogsApi issueWorklogsApi, IssuesApi issuesApi) {
    this(options, issueWorklogsApi);
    this.parentTable = new DASJiraIssueTable(options, issuesApi);
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  private Worklog extractWorklog(Row row) {
    String issueId = (String) extractValueFactory.extractValue(row, "issue_id");
    Worklog worklog = new Worklog();
  }

  @Override
  public Row insertRow(Row row) {
    try {
      Worklog worklog = extractWorklog(row);
      Worklog resultWorklog =
          issueWorklogsApi.addWorklog(
              worklog.getIssueId(), worklog, null, null, null, null, null, null);
      return toRow(resultWorklog);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      Worklog worklog = extractWorklog(newValues);
      Worklog resultWorklog =
          issueWorklogsApi.updateWorklog(
              worklog.getIssueId(),
              extractValueFactory.extractValue(rowId).toString(),
              worklog,
              null,
              null,
              null,
              null,
              null);
      return toRow(resultWorklog);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  //  @Override
  //  public void deleteRow(Value rowId) {
  //    issueWorklogsApi.deleteWorklog(issueId, (String) extractValueFactory.extractValue(rowId));
  //  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return new DASJiraWithParentTableResult(parentTable, quals, columns, sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        return new DASJiraPaginatedResult<Worklog>() {

          final String issueId = extractValueFactory.extractValue(parentRow, "id").toString();

          @Override
          public Row next() {
            return toRow(getNext());
          }

          @Override
          public DASJiraPage<Worklog> fetchPage(long offset) {
            try {
              var result =
                  issueWorklogsApi.getIssueWorklog(
                      issueId, offset, withMaxResultOrLimit(limit), null, null, null);
              var total = result.getTotal() == null ? 0 : Long.valueOf(result.getTotal());
              return new DASJiraPage<>(result.getWorklogs(), total);
            } catch (ApiException e) {
              throw new RuntimeException(e);
            }
          }
        };
      }
    };
  }

  private Row toRow(Worklog worklog) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, worklog.getId());
    addToRow("issue_id", rowBuilder, worklog.getIssueId());
    addToRow("self", rowBuilder, worklog.getSelf());
    addToRow("comment", rowBuilder, worklog.getComment());
    addToRow("started", rowBuilder, worklog.getStarted());
    addToRow("created", rowBuilder, worklog.getCreated());
    addToRow("updated", rowBuilder, worklog.getUpdated());
    addToRow("time_spent", rowBuilder, worklog.getTimeSpent());
    addToRow("time_spent_seconds", rowBuilder, worklog.getTimeSpentSeconds());
    addToRow("properties", rowBuilder, worklog.getProperties());
    addToRow("author", rowBuilder, worklog.getAuthor());
    addToRow("update_author", rowBuilder, worklog.getUpdateAuthor());
    addToRow("title", rowBuilder, worklog.getId());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put(
        "id", createColumn("id", "A unique identifier for the worklog entry.", createStringType()));
    columns.put("issue_id", createColumn("issue_id", "The ID of the issue.", createStringType()));
    columns.put("self", createColumn("self", "The URL of the worklogs.", createStringType()));
    columns.put(
        "comment",
        createColumn(
            "comment",
            "Any comments or descriptions added to the worklog entry.",
            createStringType()));
    columns.put(
        "started",
        createColumn(
            "started",
            "The date and time when the worklog activity started.",
            createTimestampType()));
    columns.put(
        "created",
        createColumn(
            "created",
            "The date and time when the worklog entry was created.",
            createTimestampType()));
    columns.put(
        "updated",
        createColumn(
            "updated",
            "The date and time when the worklog entry was last updated.",
            createTimestampType()));
    columns.put(
        "time_spent",
        createColumn(
            "time_spent",
            "The duration of time logged for the task, often in hours or minutes.",
            createStringType()));
    columns.put(
        "time_spent_seconds",
        createColumn(
            "time_spent_seconds", "The duration of time logged in seconds.", createStringType()));
    columns.put(
        "properties",
        createColumn("properties", "The properties of each worklog.", createAnyType()));
    columns.put(
        "author",
        createColumn(
            "author",
            "Information about the user who created the worklog entry, often including their username, display name, and user account details.",
            createAnyType()));
    columns.put(
        "update_author",
        createColumn(
            "update_author",
            "Details of the user who last updated the worklog entry, similar to the author information.",
            createAnyType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
