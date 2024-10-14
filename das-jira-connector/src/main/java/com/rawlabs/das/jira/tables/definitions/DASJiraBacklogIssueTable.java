package com.rawlabs.das.jira.tables.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.IssueBean;
import com.rawlabs.das.jira.rest.software.model.MoveIssuesToBacklogForBoardRequest;
import com.rawlabs.das.jira.rest.software.model.SearchResults;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.jira.tables.results.DASJiraParentedResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.das.sdk.java.exceptions.DASSdkUnsupportedException;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBacklogIssueTable extends DASJiraTable {

  private static final String TABLE_NAME = "jira_backlog_issue";

  DASTable parentTable;

  private BoardApi boardApi = new BoardApi();

  public DASJiraBacklogIssueTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "The backlog contains incomplete issues that are not assigned to any future or active sprint.");
    parentTable = new DASJiraBoardTable(options);
  }

  /** Constructor for mocks */
  DASJiraBacklogIssueTable(Map<String, String> options, BoardApi boardApi) {
    this(options);
    this.boardApi = boardApi;
    parentTable = new DASJiraBoardTable(options, boardApi);
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1));
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    Long boardId = (Long) extractValueFactory.extractValue(newValues.getDataMap().get("board_id"));
    String issueId = (String) extractValueFactory.extractValue(rowId);

    if (boardId == null || issueId == null) {
      throw new DASSdkUnsupportedException(
          "The only update operation allowed is moving issues to backlog.");
    }

    MoveIssuesToBacklogForBoardRequest moveIssuesToBacklogForBoardRequest =
        new MoveIssuesToBacklogForBoardRequest();
    moveIssuesToBacklogForBoardRequest.setIssues(List.of(issueId));
    try {
      boardApi.moveIssuesToBoard(boardId, moveIssuesToBacklogForBoardRequest);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
    return newValues.toBuilder()
        .putData(
            "board_id", valueFactory.createValue(new ValueTypeTuple(boardId, createLongType())))
        .build();
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      return new DASJiraParentedResult(
          this.parentTable,
          withParentJoin(quals, "board_id", "id"),
          List.of("id", "name"),
          sortKeys,
          limit) {

        @Override
        public DASExecuteResult fetchChildResult(Row parentRow) {
          return new DASJiraPaginatedResult<IssueBean>() {

            @Override
            public Row next() {
              Long boardId = (Long) extractValueFactory.extractValue(parentRow, "id");
              String boardName = (String) extractValueFactory.extractValue(parentRow, "name");

              return toRow(boardId, boardName, this.getNext());
            }

            @Override
            public DASJiraPage<IssueBean> fetchPage(long offset) {
              try {
                Long id = (Long) extractValueFactory.extractValue(parentRow, "id");
                SearchResults searchResults =
                    boardApi.getIssuesForBacklog(
                        id, offset, withMaxResultOrLimit(limit), null, null, null, "names");

                return new DASJiraPage<>(
                    searchResults.getIssues(),
                    Long.valueOf(Objects.requireNonNullElse(searchResults.getTotal(), 0)));
              } catch (ApiException e) {
                throw new DASSdkApiException(e.getMessage(), e);
              }
            }
          };
        }
      };

    } catch (Exception e) {
      throw new DASSdkException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private Row toRow(Long boardId, String boardName, IssueBean issueBean) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);

    addToRow("id", rowBuilder, issueBean.getId());
    addToRow("key", rowBuilder, issueBean.getKey());

    Optional.ofNullable(issueBean.getSelf())
        .ifPresent(s -> addToRow("self", rowBuilder, s.toString()));

    addToRow("board_name", rowBuilder, boardName);

    addToRow("board_id", rowBuilder, boardId);
    Map<String, Object> fields = issueBean.getFields();

    Optional.ofNullable(fields)
        .ifPresent(
            f -> {
              Optional.ofNullable(f.get("project"))
                  .ifPresent(
                      p -> {
                        addToRow("project_key", rowBuilder, ((Map<String, Object>) p).get("key"));
                        addToRow("project_id", rowBuilder, ((Map<String, Object>) p).get("id"));
                      });
              Optional.ofNullable(f.get("status"))
                  .ifPresent(
                      s -> addToRow("status", rowBuilder, ((Map<String, Object>) s).get("name")));
              Optional.ofNullable(f.get("assignee"))
                  .ifPresent(
                      a -> {
                        addToRow(
                            "assignee_account_id",
                            rowBuilder,
                            ((Map<String, Object>) a).get("accountId"));
                        addToRow(
                            "assignee_display_name",
                            rowBuilder,
                            ((Map<String, Object>) a).get("displayName"));
                      });
              addToRow("created", rowBuilder, f.get("created"));
              Optional.ofNullable(f.get("creator"))
                  .ifPresent(
                      c -> {
                        addToRow(
                            "creator_account_id",
                            rowBuilder,
                            ((Map<String, Object>) c).get("accountId"));
                        addToRow(
                            "creator_display_name",
                            rowBuilder,
                            ((Map<String, Object>) c).get("displayName"));
                      });
              addToRow("description", rowBuilder, f.get("description"));
              addToRow("due_date", rowBuilder, f.get("duedate"));
              Optional.ofNullable(f.get("epic"))
                  .ifPresent(
                      e -> {
                        Map<String, Object> epic = (Map<String, Object>) e;
                        addToRow("key", rowBuilder, epic.getOrDefault("key", null));
                      });
              Optional.ofNullable(f.get("priority"))
                  .ifPresent(
                      p -> addToRow("priority", rowBuilder, ((Map<String, Object>) p).get("name")));
              Optional.ofNullable(f.get("reporter"))
                  .ifPresent(
                      r -> {
                        addToRow(
                            "reporter_account_id",
                            rowBuilder,
                            ((Map<String, Object>) r).get("accountId"));
                        addToRow(
                            "reporter_display_name",
                            rowBuilder,
                            ((Map<String, Object>) r).get("displayName"));
                      });
              addToRow("summary", rowBuilder, f.get("summary"));
              Optional.ofNullable(f.get("type"))
                  .ifPresent(
                      t -> addToRow("type", rowBuilder, ((Map<String, Object>) t).get("name")));
              addToRow("updated", rowBuilder, f.get("updated"));
              addToRow("components", rowBuilder, f.get("components"));
              try {
                addToRow("fields", rowBuilder, objectMapper.writeValueAsString(f));
              } catch (JsonProcessingException e) {
                throw new DASSdkUnsupportedException("Error converting fields to json");
              }
              addToRow("labels", rowBuilder, f.get("labels"));
              Optional.ofNullable(f.get("labels"))
                  .ifPresent(
                      l -> {
                        List<String> labels = (List<String>) l;
                        Map<String, Boolean> tags = new HashMap<>();
                        labels.forEach(label -> tags.put(label, true));
                        addToRow("tags", rowBuilder, tags);
                      });
            });

    addToRow("title", rowBuilder, issueBean.getKey());

    return rowBuilder.build();
  }

  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columnDefinitions = new HashMap<>();
    columnDefinitions.put("id", createColumn("id", "The ID of the issue.", createStringType()));
    columnDefinitions.put(
        "board_name",
        createColumn(
            "board_name", "The name of the board the issue belongs to.", createStringType()));
    columnDefinitions.put(
        "board_id",
        createColumn("board_id", "The ID of the board the issue belongs to.", createLongType()));
    columnDefinitions.put("key", createColumn("key", "The key of the issue.", createStringType()));
    columnDefinitions.put(
        "self", createColumn("self", "The URL of the issue details.", createStringType()));
    columnDefinitions.put(
        "project_key",
        createColumn(
            "project_key", "A friendly key that identifies the project.", createStringType()));
    columnDefinitions.put(
        "project_id",
        createColumn(
            "project_id", "A friendly key that identifies the project.", createStringType()));
    columnDefinitions.put(
        "status",
        createColumn(
            "status", "he status of the issue. Eg: To Do, In Progress, Done.", createStringType()));
    columnDefinitions.put(
        "assignee_account_id",
        createColumn(
            "assignee_account_id",
            "Account Id the user/application that the issue is assigned to work.",
            createStringType()));
    columnDefinitions.put(
        "assignee_display_name",
        createColumn(
            "assignee_display_name",
            "Display name the user/application that the issue is assigned to work.",
            createStringType()));
    columnDefinitions.put(
        "created",
        createColumn("created", "Time when the issue was created.", createTimestampType()));
    columnDefinitions.put(
        "creator_account_id",
        createColumn(
            "creator_account_id",
            "Account Id of the user/application that created the issue.",
            createStringType()));
    columnDefinitions.put(
        "creator_display_name",
        createColumn(
            "creator_display_name",
            "Display name of the user/application that created the issue.",
            createStringType()));
    columnDefinitions.put(
        "description",
        createColumn("description", "Description of the issue.", createStringType()));
    columnDefinitions.put(
        "due_date",
        createColumn(
            "due_date",
            "Time by which the issue is expected to be completed.",
            createTimestampType()));
    columnDefinitions.put(
        "epic_key",
        createColumn(
            "epic_key", "The key of the epic to which issue belongs.", createStringType()));
    columnDefinitions.put(
        "priority",
        createColumn("priority", "Priority assigned to the issue.", createStringType()));
    columnDefinitions.put(
        "project_name",
        createColumn(
            "project_name", "Name of the project to that issue belongs.", createStringType()));
    columnDefinitions.put(
        "reporter_account_id",
        createColumn(
            "reporter_account_id",
            "Account Id of the user/application issue is reported.",
            createStringType()));
    columnDefinitions.put(
        "reporter_display_name",
        createColumn(
            "reporter_display_name",
            "Display name of the user/application issue is reported.",
            createStringType()));
    columnDefinitions.put(
        "summary",
        createColumn(
            "summary",
            "Details of the user/application that created the issue.",
            createStringType()));
    columnDefinitions.put(
        "type", createColumn("type", "The name of the issue type.", createStringType()));
    columnDefinitions.put(
        "updated",
        createColumn("updated", "Time when the issue was last updated.", createTimestampType()));
    columnDefinitions.put(
        "components",
        createColumn(
            "components", "List of components associated with the issue.", createAnyType()));
    columnDefinitions.put(
        "fields",
        createColumn(
            "fields", "Json object containing important subfields of the issue.", createAnyType()));
    columnDefinitions.put(
        "labels",
        createColumn(
            "labels",
            "A list of labels applied to the issue.",
            createListType(createStringType())));
    columnDefinitions.put(
        "tags",
        createColumn("tags", "A map of label names associated with this issue", createAnyType()));
    columnDefinitions.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columnDefinitions;
  }
}
