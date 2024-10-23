package com.rawlabs.das.jira.tables.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
import com.rawlabs.das.jira.rest.platform.model.JsonNode;
import com.rawlabs.das.jira.rest.platform.model.UserDetails;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;

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
      Map<String, String> options,
      IssueWorklogsApi issueWorklogsApi,
      IssueSearchApi issueSearchApi) {
    this(options, issueWorklogsApi);
    this.parentTable = new DASJiraIssueTable(options, issueSearchApi);
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  private Worklog extractWorklog(Row row) {
    try {
      JsonNode userDetail = (JsonNode) extractValueFactory.extractValue(row, "author");
      JsonNode updateAuthor = (JsonNode) extractValueFactory.extractValue(row, "update_author");
      return new Worklog(
          UserDetails.fromJson(userDetail.toJson()),
          (OffsetDateTime) extractValueFactory.extractValue(row, "created"),
          extractValueFactory.extractValue(row, "id").toString(),
          extractValueFactory.extractValue(row, "issue_id").toString(),
          new URI(extractValueFactory.extractValue(row, "self").toString()),
          UserDetails.fromJson(updateAuthor.toJson()),
          (OffsetDateTime) extractValueFactory.extractValue(row, "updated"));
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
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

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return new DASJiraWithParentTableResult(parentTable, quals, columns, sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        return new DASJiraPaginatedResult<Worklog>(limit) {

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
              return new DASJiraPage<>(
                  result.getWorklogs(),
                  Long.valueOf(Objects.requireNonNullElse(result.getTotal(), 0)));
            } catch (ApiException e) {
              throw new RuntimeException(e);
            }
          }
        };
      }
    };
  }

  @SuppressWarnings("unchecked")
  private Row toRow(Worklog worklog) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, worklog.getId());
    addToRow("issue_id", rowBuilder, worklog.getIssueId());
    Optional.ofNullable(worklog.getSelf())
        .ifPresent(self -> addToRow("self", rowBuilder, self.toString()));
    Optional.ofNullable(worklog.getComment())
        .ifPresent(
            comment -> {
              addToRow("comment", rowBuilder, comment.toString());
            });
    Optional.ofNullable(worklog.getStarted())
        .ifPresent(started -> addToRow("started", rowBuilder, started.toString()));
    Optional.ofNullable(worklog.getCreated())
        .ifPresent(created -> addToRow("created", rowBuilder, created.toString()));
    Optional.ofNullable(worklog.getUpdated())
        .ifPresent(updated -> addToRow("updated", rowBuilder, updated.toString()));
    addToRow("time_spent", rowBuilder, worklog.getTimeSpent());
    Optional.ofNullable(worklog.getTimeSpentSeconds())
        .ifPresent(
            timeSpentSeconds ->
                addToRow("time_spent_seconds", rowBuilder, timeSpentSeconds.toString()));
    try {
      addToRow("properties", rowBuilder, objectMapper.writeValueAsString(worklog.getProperties()));
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException(e.getMessage());
    }
    Optional.ofNullable(worklog.getUpdateAuthor())
        .ifPresent(updateAuthor -> addToRow("update_author", rowBuilder, updateAuthor.toJson()));
    Optional.ofNullable(worklog.getAuthor())
        .ifPresent(author -> addToRow("author", rowBuilder, author.toJson()));
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
