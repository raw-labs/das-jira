package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.DASJiraUnexpectedError;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.*;
import com.rawlabs.das.jira.rest.platform.model.Comment;
import com.rawlabs.das.jira.rest.platform.model.UserDetails;
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
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraIssueCommentTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_comment";

  private final DASJiraTable parentTable;

  private final IssueCommentsApi issueCommentsApi;

  public DASJiraIssueCommentTable(
      Map<String, String> options,
      IssueCommentsApi issueCommentsApi,
      IssueSearchApi issueSearchApi,
      IssuesApi issuesApi) {
    super(options, TABLE_NAME, "Comments that provided in issue.");
    this.issueCommentsApi = issueCommentsApi;
    this.parentTable = new DASJiraIssueTable(options, issueSearchApi, issuesApi);
  }

  public List<SortKey> getTableSortOrders(List<SortKey> sortKeys) {
    return sortKeys.stream().filter(sortKey -> sortKey.getName().equals("created")).toList();
  }

  public String uniqueColumn() {
    return "id";
  }

  private Comment extractComment(Row row) {
    try {
      return new Comment(
          UserDetails.fromJson(extractValueFactory.extractValue(row, "author").toString()),
          extractValueFactory.extractValue(row, "created").toString(),
          null,
          null,
          (Boolean) extractValueFactory.extractValue(row, "jsd_public"),
          extractValueFactory.extractValue(row, "body").toString(),
          null,
          UserDetails.fromJson(extractValueFactory.extractValue(row, "update_author").toString()),
          extractValueFactory.extractValue(row, "updated").toString());
    } catch (IOException e) {
      throw new DASJiraUnexpectedError(e);
    }
  }

  public Row insert(Row row) {
    try {
      String issueIdOrKey = extractValueFactory.extractValue(row, "issue_id").toString();
      Comment resultComment = issueCommentsApi.addComment(issueIdOrKey, extractComment(row), null);
      return toRow(resultComment, issueIdOrKey, List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  public Row update(Value rowId, Row newValues) {
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
      return toRow(comment, issueIdOrKey, List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    return new DASJiraWithParentTableResult(parentTable, quals, columns, sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        return new DASJiraPaginatedResult<Comment>(limit) {

          final String issueId = extractValueFactory.extractValue(parentRow, "id").toString();

          @Override
          public Row next() {
            return toRow(getNext(), issueId, columns);
          }

          @Override
          public DASJiraPage<Comment> fetchPage(long offset) {
            try {
              var result =
                  issueCommentsApi.getComments(
                      issueId, offset, withMaxResultOrLimit(limit), withOrderBy(sortKeys), null);
              return new DASJiraPage<>(result.getComments(), result.getTotal());
            } catch (ApiException e) {
              throw makeSdkException(e);
            }
          }
        };
      }
    };
  }

  private Row toRow(Comment comment, String issueId, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, comment.getId(), columns);
    addToRow("issue_id", rowBuilder, issueId, columns);
    addToRow("self", rowBuilder, comment.getSelf(), columns);
    var body = Optional.ofNullable(comment.getBody()).map(Object::toString).orElse(null);
    addToRow("body", rowBuilder, body, columns);

    var created = Optional.ofNullable(comment.getCreated()).map(Object::toString).orElse(null);
    addToRow("created", rowBuilder, created, columns);

    var updated = Optional.ofNullable(comment.getUpdated()).map(Object::toString).orElse(null);
    addToRow("updated", rowBuilder, updated, columns);

    addToRow("jsd_public", rowBuilder, comment.getJsdPublic(), columns);

    var author = Optional.ofNullable(comment.getAuthor()).map(UserDetails::toJson).orElse(null);
    addToRow("author", rowBuilder, author, columns);

    var updateAuthor =
        Optional.ofNullable(comment.getUpdateAuthor()).map(UserDetails::toJson).orElse(null);
    addToRow("update_author", rowBuilder, updateAuthor, columns);

    addToRow("title", rowBuilder, comment.getId(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
