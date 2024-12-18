package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.jira.utils.ExceptionHandling.makeSdkException;
import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.DASJiraUnexpectedError;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.rest.platform.model.JsonNode;
import com.rawlabs.das.jira.rest.platform.model.UserDetails;
import com.rawlabs.das.jira.rest.platform.model.Worklog;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.jira.tables.results.DASJiraWithParentTableResult;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraIssueWorklogTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_worklog";

  private final DASJiraTable parentTable;

  private final IssueWorklogsApi issueWorklogsApi;

  public DASJiraIssueWorklogTable(
      Map<String, String> options,
      ZoneId zoneId,
      IssueWorklogsApi issueWorklogsApi,
      IssueSearchApi issueSearchApi,
      IssuesApi issuesApi) {
    super(
        options,
        TABLE_NAME,
        "Jira worklog is a feature within the Jira software that allows users to record the amount of time they have spent working on various tasks or issues.");
    this.issueWorklogsApi = issueWorklogsApi;
    parentTable = new DASJiraIssueTable(options, zoneId, issueSearchApi, issuesApi);
  }

  public String uniqueColumn() {
    return "id";
  }

  private Worklog extractWorklog(Row row) {
    try {
      JsonNode userDetail = (JsonNode) extractValueFactory.extractValue(row, "author");
      JsonNode updateAuthor = (JsonNode) extractValueFactory.extractValue(row, "update_author");
      return new Worklog(
          UserDetails.fromJson(userDetail.toJson()),
          extractValueFactory.extractValue(row, "created").toString(),
          extractValueFactory.extractValue(row, "id").toString(),
          extractValueFactory.extractValue(row, "issue_id").toString(),
          new URI(extractValueFactory.extractValue(row, "self").toString()),
          UserDetails.fromJson(updateAuthor.toJson()),
          extractValueFactory.extractValue(row, "updated").toString());
    } catch (IOException | URISyntaxException e) {
      throw new DASJiraUnexpectedError(e);
    }
  }

  public Row insert(Row row) {
    try {
      Worklog worklog = extractWorklog(row);
      Worklog resultWorklog =
          issueWorklogsApi.addWorklog(
              worklog.getIssueId(), worklog, null, null, null, null, null, null);
      return toRow(resultWorklog, List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  public Row update(Value rowId, Row newValues) {
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
      return toRow(resultWorklog, List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    return new DASJiraWithParentTableResult(parentTable, quals, columns, sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        return new DASJiraPaginatedResult<Worklog>(limit) {

          final String issueId = extractValueFactory.extractValue(parentRow, "id").toString();

          @Override
          public Row next() {
            return toRow(getNext(), columns);
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
              throw makeSdkException(e);
            }
          }
        };
      }
    };
  }

  private Row toRow(Worklog worklog, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, worklog.getId(), columns);
    addToRow("issue_id", rowBuilder, worklog.getIssueId(), columns);
    var self = Optional.ofNullable(worklog.getSelf()).map(URI::toString).orElse(null);
    addToRow("self", rowBuilder, self, columns);

    var comment = Optional.ofNullable(worklog.getComment()).map(Object::toString).orElse(null);
    addToRow("comment", rowBuilder, comment, columns);

    var started = Optional.ofNullable(worklog.getStarted()).map(Object::toString).orElse(null);
    addToRow("started", rowBuilder, started, columns);

    var created = Optional.ofNullable(worklog.getCreated()).map(Object::toString).orElse(null);
    addToRow("created", rowBuilder, created, columns);

    var updated = Optional.ofNullable(worklog.getUpdated()).map(Object::toString).orElse(null);
    addToRow("updated", rowBuilder, updated, columns);

    var timeSpent = Optional.ofNullable(worklog.getTimeSpent()).map(Object::toString).orElse(null);
    addToRow("time_spent", rowBuilder, timeSpent, columns);

    var timeSpentSeconds =
        Optional.ofNullable(worklog.getTimeSpentSeconds()).map(Object::toString).orElse(null);
    addToRow("time_spent_seconds", rowBuilder, timeSpentSeconds, columns);

    try {
      addToRow(
          "properties",
          rowBuilder,
          objectMapper.writeValueAsString(worklog.getProperties()),
          columns);
    } catch (JsonProcessingException e) {
      throw new DASJiraUnexpectedError(e);
    }

    var updateAuthor =
        Optional.ofNullable(worklog.getUpdateAuthor()).map(UserDetails::toJson).orElse(null);
    addToRow("update_author", rowBuilder, updateAuthor, columns);

    var author = Optional.ofNullable(worklog.getAuthor()).map(UserDetails::toJson).orElse(null);
    addToRow("author", rowBuilder, author, columns);

    addToRow("title", rowBuilder, worklog.getId(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
