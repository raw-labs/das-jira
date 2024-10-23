package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.*;
import com.rawlabs.das.jira.rest.platform.model.Comment;
import com.rawlabs.das.jira.rest.platform.model.UserDetails;
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
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraIssueCommentTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_comment";

  private DASTable parentTable;

  private IssueCommentsApi issueCommentsApi = new IssueCommentsApi();

  public DASJiraIssueCommentTable(Map<String, String> options) {
    super(options, TABLE_NAME, "Comments that provided in issue.");
    parentTable = new DASJiraIssueTable(options);
  }

  /** Constructor for mocks */
  DASJiraIssueCommentTable(Map<String, String> options, IssueCommentsApi issueCommentsApi) {
    this(options);
    this.issueCommentsApi = issueCommentsApi;
  }

  public DASJiraIssueCommentTable(
      Map<String, String> options,
      IssueCommentsApi issueCommentsApi,
      IssueSearchApi issueSearchApi) {
    this(options, issueCommentsApi);
    this.parentTable = new DASJiraIssueTable(options, issueSearchApi);
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    return sortKeys.stream().filter(sortKey -> sortKey.getName().equals("created")).toList();
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  private Comment extractComment(Row row) {
    try {
      return new Comment(
          UserDetails.fromJson(extractValueFactory.extractValue(row, "author").toString()),
          (OffsetDateTime) extractValueFactory.extractValue(row, "created"),
          null,
          null,
          (Boolean) extractValueFactory.extractValue(row, "jsd_public"),
          extractValueFactory.extractValue(row, "body").toString(),
          null,
          UserDetails.fromJson(extractValueFactory.extractValue(row, "update_author").toString()),
          (OffsetDateTime) extractValueFactory.extractValue(row, "updated"));
    } catch (IOException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
  }

  @Override
  public Row insertRow(Row row) {
    try {
      String issueIdOrKey = extractValueFactory.extractValue(row, "issue_id").toString();
      Comment resultComment = issueCommentsApi.addComment(issueIdOrKey, extractComment(row), null);
      return toRow(resultComment, issueIdOrKey);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      String issueIdOrKey = extractValueFactory.extractValue(newValues, "issue_id").toString();
      Comment comment =
          issueCommentsApi.updateComment(
              issueIdOrKey,
              extractValueFactory.extractValue(rowId).toString(),
              extractComment(newValues),
              null,
              null,
              null);
      return toRow(comment, issueIdOrKey);
    } catch (ApiException e) {
      throw new RuntimeException(e);
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
        return new DASJiraPaginatedResult<Comment>(limit) {

          final String issueId = extractValueFactory.extractValue(parentRow, "id").toString();

          @Override
          public Row next() {
            return toRow(getNext(), issueId);
          }

          @Override
          public DASJiraPage<Comment> fetchPage(long offset) {
            try {
              var result =
                  issueCommentsApi.getComments(
                      issueId, offset, withMaxResultOrLimit(limit), withOrderBy(sortKeys), null);
              return new DASJiraPage<>(result.getComments(), result.getTotal());
            } catch (ApiException e) {
              throw new RuntimeException(e);
            }
          }
        };
      }
    };
  }

  private Row toRow(Comment comment, String issueId) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, comment.getId());
    addToRow("issue_id", rowBuilder, issueId);
    addToRow("self", rowBuilder, comment.getSelf());
    Optional.ofNullable(comment.getBody())
        .ifPresent(body -> addToRow("body", rowBuilder, body.toString()));
    Optional.ofNullable(comment.getCreated())
        .ifPresent(created -> addToRow("created", rowBuilder, created.toString()));
    Optional.ofNullable(comment.getUpdated())
        .ifPresent(updated -> addToRow("updated", rowBuilder, updated.toString()));
    addToRow("jsd_public", rowBuilder, comment.getJsdPublic());
    Optional.ofNullable(comment.getAuthor())
        .ifPresent(author -> addToRow("author", rowBuilder, author.toJson()));
    Optional.ofNullable(comment.getUpdateAuthor())
        .ifPresent(updateAuthor -> addToRow("update_author", rowBuilder, updateAuthor.toJson()));
    addToRow("title", rowBuilder, comment.getId());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The ID of the issue comment.", createStringType()));
    columns.put("issue_id", createColumn("issue_id", "The ID of the issue.", createStringType()));
    columns.put("self", createColumn("self", "The URL of the issue comment.", createStringType()));
    columns.put(
        "body", createColumn("body", "The content of the issue comment.", createStringType()));
    columns.put(
        "created",
        createColumn("created", "Time when the issue comment was created.", createTimestampType()));
    columns.put(
        "updated",
        createColumn(
            "updated", "Time when the issue comment was last updated.", createTimestampType()));
    columns.put(
        "jsd_public",
        createColumn(
            "jsd_public",
            "JsdPublic set to false does not hide comments in Service Desk projects.",
            createBoolType()));
    columns.put(
        "author",
        createColumn(
            "author", "The user information who added the issue comment.", createAnyType()));
    columns.put(
        "update_author",
        createColumn(
            "update_author",
            "The user information who updated the issue comment.",
            createAnyType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
