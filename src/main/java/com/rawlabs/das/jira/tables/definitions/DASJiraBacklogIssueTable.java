package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.utils.TableFactory;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.das.TableId;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.TypeFactory.*;

public class DASJiraBacklogIssueTable extends DASJiraTable {
  public DASJiraBacklogIssueTable(Map<String, String> options) {
    super("", options);
  }

  @Override
  protected TableDefinition buildTableDefinition() {
    return TableFactory.createTable(
        this.getTableName(),
        "The backlog contains incomplete issues that are not assigned to any future or active sprint.",
        List.of(
            createColumn("id", "The ID of the issue.", createStringType()),
            createColumn("key", "The key of the issue.", createStringType()),
            createColumn("self", "The URL of the issue details.", createStringType()),
            createColumn(
                "board_name", "The name of the board the issue belongs to.", createStringType()),
            createColumn("board_id", "The ID of the board the issue belongs to.", createIntType()),
            createColumn(
                "project_key", "A friendly key that identifies the project.", createStringType()),
            createColumn(
                "project_id", "A friendly key that identifies the project.", createStringType()),
            createColumn(
                "status",
                "The status of the issue. Eg: To Do, In Progress, Done.",
                createStringType()),
            createColumn(
                "assignee_account_id",
                "Account Id the user/application that the issue is assigned to work.",
                createStringType()),
            createColumn(
                "assignee_display_name",
                "Display name the user/application that the issue is assigned to work.",
                createStringType()),
            createColumn("created", "Time when the issue was created.", createTimestampType()),
            createColumn(
                "creator_account_id",
                "Account Id of the user/application that created the issue.",
                createStringType()),
            createColumn(
                "creator_display_name",
                "Display name of the user/application that created the issue.",
                createStringType()),
            createColumn("description", "Description of the issue.", createStringType()),
            createColumn(
                "due_date",
                "Time by which the issue is expected to be completed.",
                createTimestampType()),
            createColumn(
                "epic_key", "The key of the epic to which issue belongs.", createStringType()),
            createColumn("priority", "Priority assigned to the issue.", createStringType()),
            createColumn(
                "project_name", "Name of the project to that issue belongs.", createStringType()),
            createColumn(
                "reporter_account_id",
                "Account Id of the user/application issue is reported.",
                createStringType()),
            createColumn(
                "reporter_display_name",
                "Display name of the user/application issue is reported.",
                createStringType()),
            createColumn(
                "summary",
                "Details of the user/application that created the issue.",
                createStringType()),
            createColumn("type", "The name of the issue type.", createStringType()),
            createColumn("updated", "Time when the issue was last updated.", createTimestampType()),
            createColumn(
                "components",
                "List of components associated with the issue.",
                createListType(createStringType())),
            createColumn(
                "fields",
                "Json object containing important subfields of the issue.",
                createAnyType()),
            createColumn("labels", "A list of labels applied to the issue.", createAnyType()),
            createColumn(
                "tags",
                "A map of label names associated with this issue, in Steampipe standard format.",
                createAnyType()),
            createColumn("title", "The key of the issue.", createStringType())));
  }
}
