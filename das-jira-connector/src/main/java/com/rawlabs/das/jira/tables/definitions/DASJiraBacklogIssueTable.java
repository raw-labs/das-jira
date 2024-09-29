package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.api.BoardApi;
import com.rawlabs.das.jira.rest.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.model.IssueBean;
import com.rawlabs.das.jira.tables.DASJiraColumnDefinition;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.DASJiraTableDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.HashMap;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

// TO DO
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

  @SuppressWarnings("unchecked")
  private Row toRow(GetAllBoards200ResponseValuesInner board, IssueBean issueBean) {
    Row.Builder rowBuilder = Row.newBuilder();
    dasJiraTableDefinition.updateRow("border_name", rowBuilder, board.getName());
    dasJiraTableDefinition.updateRow("border_id", rowBuilder, board.getId());
    dasJiraTableDefinition.updateRow("id", rowBuilder, issueBean.getId());
    dasJiraTableDefinition.updateRow("key", rowBuilder, issueBean.getKey());
    dasJiraTableDefinition.updateRow("self", rowBuilder, issueBean.getSelf());
    assert issueBean.getFields() != null;
    Map<String, Object> project = (Map<String, Object>) issueBean.getFields().get("project");
    dasJiraTableDefinition.updateRow("project_key", rowBuilder, project.get("key"));
    dasJiraTableDefinition.updateRow("project_id", rowBuilder, project.get("id"));
    dasJiraTableDefinition.updateRow("status", rowBuilder, issueBean.getFields().get("status"));
    return rowBuilder.build();
  }

  public DASJiraBacklogIssueTable(Map<String, String> options) {
    super(options);
    HashMap<String, DASJiraColumnDefinition> columns = new HashMap<>();
    columns.put(
        "border_name",
        new DASJiraColumnDefinition(
            "border_name", "The name of the board the issue belongs to.", createStringType()));
    columns.put(
        "border_id",
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
            "components",
            "List of components associated with the issue.",
            createListType(createStringType())));
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
