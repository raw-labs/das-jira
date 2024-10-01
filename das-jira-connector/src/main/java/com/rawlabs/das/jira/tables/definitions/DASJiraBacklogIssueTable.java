package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.model.IssueCommentListRequestBean;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.software.model.IssueBean;
import com.rawlabs.das.jira.tables.DASJiraColumnDefinition;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.DASJiraTableDefinition;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBacklogIssueTable extends DASJiraTable {

  private static final String TABLE_NAME = "jira_backlog_issue";

  private final DASJiraTableDefinition dasJiraTableDefinition;

  private BoardApi api = new BoardApi();

  @Override
  public String getTableName() {
    return dasJiraTableDefinition.getTableDefinition().getTableId().getName();
  }

  @Override
  public TableDefinition getTableDefinition() {
    return dasJiraTableDefinition.getTableDefinition();
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      GetAllBoards200ResponseValuesInner result = api.getBoard(1L);
    } catch (Exception e) {
      throw new DASSdkException(e.getMessage(), e);
    }
    return super.execute(quals, columns, sortKeys, limit);
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    return super.canSort(sortKeys);
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return super.getPathKeys();
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public Row insertRow(Row row) {
    return super.insertRow(row);
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return super.insertRows(rows);
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    return super.updateRow(rowId, newValues);
  }

  @Override
  public void deleteRow(Value rowId) {
    super.deleteRow(rowId);
  }

  @SuppressWarnings("unchecked")
  private Row toRow(GetAllBoards200ResponseValuesInner board, IssueBean issueBean) {
    Row.Builder rowBuilder = Row.newBuilder();
    dasJiraTableDefinition.updateRow("board_name", rowBuilder, board.getName());
    dasJiraTableDefinition.updateRow("board_id", rowBuilder, board.getId());
    dasJiraTableDefinition.updateRow("id", rowBuilder, issueBean.getId());
    dasJiraTableDefinition.updateRow("key", rowBuilder, issueBean.getKey());
    dasJiraTableDefinition.updateRow("self", rowBuilder, issueBean.getSelf());
    assert issueBean.getFields() != null;
    Map<String, Object> project =
        (Map<String, Object>) issueBean.getFields().getOrDefault("project", null);
    dasJiraTableDefinition.updateRow(
        "project_key", rowBuilder, project == null ? null : project.getOrDefault("key", null));
    dasJiraTableDefinition.updateRow(
        "project_id", rowBuilder, project == null ? null : project.getOrDefault("id", null));
    dasJiraTableDefinition.updateRow(
        "project_name", rowBuilder, project == null ? null : project.getOrDefault("name", null));
    Map<String, Object> status =
        (Map<String, Object>) issueBean.getFields().getOrDefault("status", null);
    dasJiraTableDefinition.updateRow("status", rowBuilder, status.getOrDefault("name", null));
    Map<String, Object> assignee =
        (Map<String, Object>) issueBean.getFields().getOrDefault("assignee", null);
    dasJiraTableDefinition.updateRow(
        "assignee_account_id",
        rowBuilder,
        assignee == null ? null : assignee.getOrDefault("accountId", null));
    dasJiraTableDefinition.updateRow(
        "assignee_display_name",
        rowBuilder,
        assignee == null ? null : assignee.getOrDefault("displayName", null));
    dasJiraTableDefinition.updateRow(
        "created", rowBuilder, issueBean.getFields().getOrDefault("created", null));
    Map<String, Object> creator =
        (Map<String, Object>) issueBean.getFields().getOrDefault("creator", null);
    dasJiraTableDefinition.updateRow(
        "creator_account_id",
        rowBuilder,
        creator == null ? null : creator.getOrDefault("accountId", null));
    dasJiraTableDefinition.updateRow(
        "creator_display_name",
        rowBuilder,
        creator == null ? null : creator.getOrDefault("displayName", null));
    dasJiraTableDefinition.updateRow(
        "description", rowBuilder, issueBean.getFields().getOrDefault("description", null));
    dasJiraTableDefinition.updateRow(
        "due_date", rowBuilder, issueBean.getFields().getOrDefault("duedate", null));
    Map<String, Object> epic =
        (Map<String, Object>) issueBean.getFields().getOrDefault("epic", null);
    dasJiraTableDefinition.updateRow(
        "epic_key", rowBuilder, epic == null ? null : epic.getOrDefault("key", null));
    Map<String, Object> priority =
        (Map<String, Object>) issueBean.getFields().getOrDefault("priority", null);
    dasJiraTableDefinition.updateRow("priority", rowBuilder, priority.getOrDefault("name", null));
    Map<String, Object> reporter =
        (Map<String, Object>) issueBean.getFields().getOrDefault("reporter", null);
    dasJiraTableDefinition.updateRow(
        "reporter_account_id",
        rowBuilder,
        reporter == null ? null : reporter.getOrDefault("accountId", null));
    dasJiraTableDefinition.updateRow(
        "reporter_display_name",
        rowBuilder,
        reporter == null ? null : reporter.getOrDefault("displayName", null));
    dasJiraTableDefinition.updateRow(
        "summary", rowBuilder, issueBean.getFields().getOrDefault("summary", null));
    Map<String, Object> issueType =
        (Map<String, Object>) issueBean.getFields().getOrDefault("type", null);
    dasJiraTableDefinition.updateRow("type", rowBuilder, issueType.getOrDefault("name", null));
    dasJiraTableDefinition.updateRow(
        "updated", rowBuilder, issueBean.getFields().getOrDefault("updated", null));
    List<Map<String, Object>> components =
        (List<Map<String, Object>>) issueBean.getFields().getOrDefault("components", null);
    dasJiraTableDefinition.updateRow(
        "components",
        rowBuilder,
        components == null ? null : components.stream().map(c -> c.get("id")).toList());
    dasJiraTableDefinition.updateRow("fields", rowBuilder, issueBean.getFields());
    List<String> labels = (List<String>) issueBean.getFields().getOrDefault("labels", null);
    dasJiraTableDefinition.updateRow("labels", rowBuilder, labels);
    Map<String, Boolean> tags = new HashMap<>();
    labels.forEach(l -> tags.put(l, true));
    dasJiraTableDefinition.updateRow("tags", rowBuilder, tags);
    return rowBuilder.build();
  }

  public DASJiraBacklogIssueTable(Map<String, String> options) {
    super(options);
    HashMap<String, DASJiraColumnDefinition> columns = new HashMap<>();
    columns.put(
        "board_name",
        new DASJiraColumnDefinition(
            "border_name", "The name of the board the issue belongs to.", createStringType()));
    columns.put(
        "board_id",
        new DASJiraColumnDefinition(
            "border_id", "The ID of the board the issue belongs to.", createIntType()));
    columns.put(
        "id", new DASJiraColumnDefinition("id", "The ID of the issue.", createStringType()));
    columns.put(
        "key", new DASJiraColumnDefinition("key", "The key of the issue.", createStringType()));
    columns.put(
        "self",
        new DASJiraColumnDefinition("self", "The URL of the issue details.", createStringType()));
    columns.put(
        "project_key",
        new DASJiraColumnDefinition(
            "project_key", "A friendly key that identifies the project.", createStringType()));
    columns.put(
        "project_id",
        new DASJiraColumnDefinition(
            "project_id", "A friendly key that identifies the project.", createStringType()));
    columns.put(
        "status",
        new DASJiraColumnDefinition(
            "status",
            "The status of the issue. Eg: To Do, In Progress, Done.",
            createStringType()));
    columns.put(
        "assignee_account_id",
        new DASJiraColumnDefinition(
            "assignee_account_id",
            "Account Id the user/application that the issue is assigned to work.",
            createStringType()));
    columns.put(
        "assignee_display_name",
        new DASJiraColumnDefinition(
            "assignee_display_name",
            "Display name the user/application that the issue is assigned to work.",
            createStringType()));
    columns.put(
        "created",
        new DASJiraColumnDefinition(
            "created", "Time when the issue was created.", createTimestampType()));
    columns.put(
        "creator_account_id",
        new DASJiraColumnDefinition(
            "creator_account_id",
            "Account Id of the user/application that created the issue.",
            createStringType()));
    columns.put(
        "creator_display_name",
        new DASJiraColumnDefinition(
            "creator_display_name",
            "Display name of the user/application that created the issue.",
            createStringType()));
    columns.put(
        "description",
        new DASJiraColumnDefinition(
            "description", "Description of the issue.", createStringType()));
    columns.put(
        "due_date",
        new DASJiraColumnDefinition(
            "due_date",
            "Time by which the issue is expected to be completed.",
            createTimestampType()));
    columns.put(
        "epic_key",
        new DASJiraColumnDefinition(
            "epic_key", "The key of the epic to which issue belongs.", createStringType()));
    columns.put(
        "priority",
        new DASJiraColumnDefinition(
            "priority", "Priority assigned to the issue.", createStringType()));
    columns.put(
        "project_name",
        new DASJiraColumnDefinition(
            "project_name", "Name of the project to that issue belongs.", createStringType()));
    columns.put(
        "reporter_account_id",
        new DASJiraColumnDefinition(
            "reporter_account_id",
            "Account Id of the user/application issue is reported.",
            createStringType()));
    columns.put(
        "reporter_display_name",
        new DASJiraColumnDefinition(
            "reporter_display_name",
            "Display name of the user/application issue is reported.",
            createStringType()));
    columns.put(
        "summary",
        new DASJiraColumnDefinition(
            "summary",
            "Details of the user/application that created the issue.",
            createStringType()));
    columns.put(
        "type",
        new DASJiraColumnDefinition("type", "The name of the issue type.", createStringType()));
    columns.put(
        "updated",
        new DASJiraColumnDefinition(
            "updated", "Time when the issue was last updated.", createTimestampType()));
    columns.put(
        "components",
        new DASJiraColumnDefinition(
            "components", "List of components associated with the issue.", createAnyType()));
    columns.put(
        "fields",
        new DASJiraColumnDefinition(
            "fields", "Json object containing important subfields of the issue.", createAnyType()));
    columns.put(
        "labels",
        new DASJiraColumnDefinition(
            "labels", "A list of labels applied to the issue.", createAnyType()));
    columns.put(
        "tags",
        new DASJiraColumnDefinition(
            "tags", "A map of label names associated with this issue.", createAnyType()));
    columns.put("title", new DASJiraColumnDefinition("title", TITLE_DESC, createStringType()));

    dasJiraTableDefinition =
        new DASJiraTableDefinition(
            TABLE_NAME,
            "The backlog contains incomplete issues that are not assigned to any future or active sprint.",
            columns);
  }

  /** Constructor for mocks */
  DASJiraBacklogIssueTable(Map<String, String> options, BoardApi api) {
    this(options);
    this.api = api;
  }
}
