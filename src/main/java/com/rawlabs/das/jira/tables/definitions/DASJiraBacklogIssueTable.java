package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.das.TableId;

import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.TypeFactory.*;

public class DASJiraBacklogIssueTable extends DASJiraTable {
  public DASJiraBacklogIssueTable(Map<String, String> options) {
    super("", options);
  }

  @Override
  protected TableDefinition buildTableDefinition() {
    return TableDefinition.newBuilder()
        .setTableId(TableId.newBuilder().setName(this.getTableName()))
        .setDescription(
            "The application properties that are accessible on the Advanced Settings page.")
        .addColumns(createColumn("id", "The ID of the issue.", createStringType()))
        .addColumns(createColumn("key", "TThe key of the issue.", createStringType()))
        .addColumns(createColumn("self", "The URL of the issue details.", createStringType()))
        .addColumns(
            createColumn(
                "board_name", "The name of the board the issue belongs to.", createStringType()))
        .addColumns(
            createColumn("board_id", "The ID of the board the issue belongs to.", createIntType()))
        .addColumns(
            createColumn(
                "project_key", "A friendly key that identifies the project.", createStringType()))
        .addColumns(
            createColumn(
                "project_id", "A friendly key that identifies the project.", createStringType()))
        .addColumns(
            createColumn(
                "status",
                "The status of the issue. Eg: To Do, In Progress, Done.",
                createStringType()))
        .addColumns(
            createColumn(
                "assignee_account_id",
                "Account Id the user/application that the issue is assigned to work.",
                createStringType()))
        .addColumns(
            createColumn(
                "assignee_display_name",
                "Display name the user/application that the issue is assigned to work.",
                createStringType()))
        .addColumns(createColumn("created", "Time when the issue was created.", createStringType()))
        .addColumns(
            createColumn(
                "creator_account_id",
                "Account Id of the user/application that created the issue.",
                createStringType()))
        .addColumns(
            createColumn(
                "creator_display_name",
                "Display name of the user/application that created the issue.",
                createStringType()))
        .addColumns(createColumn("description", "Description of the issue.", createStringType()))
        .addColumns(
            createColumn(
                "due_date",
                "Time by which the issue is expected to be completed.",
                createStringType()))
        .addColumns(
            createColumn(
                "epic_key", "The key of the epic to which issue belongs.", createStringType()))
        .addColumns(createColumn("priority", "Priority assigned to the issue.", createStringType()))
        .addColumns(
            createColumn(
                "project_name", "Name of the project to that issue belongs.", createStringType()))
        .addColumns(
            createColumn(
                "reporter_account_id",
                "Account Id of the user/application issue is reported.",
                createStringType()))
        .addColumns(
            createColumn(
                "reporter_display_name",
                "Display name of the user/application issue is reported.",
                createStringType()))
        .addColumns(
            createColumn(
                "summary",
                "Details of the user/application that created the issue.",
                createStringType()))
        .addColumns(createColumn("type", "The name of the issue type.", createStringType()))
        .addColumns(
            createColumn("updated", "Time when the issue was last updated.", createStringType()))
        .addColumns(
            createColumn(
                "components", "List of components associated with the issue.", createStringType()))
        .addColumns(
            createColumn(
                "fields",
                "Json object containing important subfields of the issue.",
                createStringType()))
        .addColumns(
            createColumn("labels", "A list of labels applied to the issue.", createStringType()))
        .addColumns(
            createColumn(
                "tags",
                "A map of label names associated with this issue, in Steampipe standard format.",
                createStringType()))
        .addColumns(createColumn("title", "The title of the issue.", createStringType()))
        .build();
  }

  //  Columns: commonColumns([]*plugin.Column{
  //    // top fields
  //    {
  //      Name:        "id",
  //              Description: "The ID of the issue.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromGo(),
  //    },
  //    {
  //      Name:        "key",
  //              Description: "The key of the issue.",
  //            Type:        proto.ColumnType_STRING,
  //    },
  //    {
  //      Name:        "self",
  //              Description: "The URL of the issue details.",
  //            Type:        proto.ColumnType_STRING,
  //    },
  //    {
  //      Name:        "board_name",
  //              Description: "The name of the board the issue belongs to.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("BoardName"),
  //    },
  //    {
  //      Name:        "board_id",
  //              Description: "The ID of the board the issue belongs to.",
  //            Type:        proto.ColumnType_INT,
  //            Transform:   transform.FromField("BoardId"),
  //    },
  //    {
  //      Name:        "project_key",
  //              Description: "A friendly key that identifies the project.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Project.Key"),
  //    },
  //    {
  //      Name:        "project_id",
  //              Description: "A friendly key that identifies the project.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Project.ID"),
  //    },
  //    {
  //      Name:        "status",
  //              Description: "The status of the issue. Eg: To Do, In Progress, Done.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Status.Name"),
  //    },
  //
  //    // other important fields
  //    {
  //      Name:        "assignee_account_id",
  //              Description: "Account Id the user/application that the issue is assigned to
  // work.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Assignee.AccountID"),
  //    },
  //    {
  //      Name:        "assignee_display_name",
  //              Description: "Display name the user/application that the issue is assigned to
  // work.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Assignee.DisplayName"),
  //    },
  //    {
  //      Name:        "created",
  //              Description: "Time when the issue was created.",
  //            Type:        proto.ColumnType_TIMESTAMP,
  //            Transform:   transform.FromField("Fields.Created").Transform(convertJiraTime),
  //    },
  //    {
  //      Name:        "creator_account_id",
  //              Description: "Account Id of the user/application that created the issue.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Creator.AccountID"),
  //    },
  //    {
  //      Name:        "creator_display_name",
  //              Description: "Display name of the user/application that created the issue.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Creator.DisplayName"),
  //    },
  //    {
  //      Name:        "description",
  //              Description: "Description of the issue.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Description"),
  //    },
  //    {
  //      Name:        "due_date",
  //              Description: "Time by which the issue is expected to be completed.",
  //            Type:        proto.ColumnType_TIMESTAMP,
  //            Transform:
  // transform.FromField("Fields.Duedate").NullIfZero().Transform(convertJiraDate),
  //    },
  //    {
  //      Name:        "epic_key",
  //              Description: "The key of the epic to which issue belongs.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromP(extractBacklogIssueRequiredField, "epic"),
  //    },
  //    {
  //      Name:        "priority",
  //              Description: "Priority assigned to the issue.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Priority.Name"),
  //    },
  //    {
  //      Name:        "project_name",
  //              Description: "Name of the project to that issue belongs.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Project.Name"),
  //    },
  //    {
  //      Name:        "reporter_account_id",
  //              Description: "Account Id of the user/application issue is reported.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Reporter.AccountID"),
  //    },
  //    {
  //      Name:        "reporter_display_name",
  //              Description: "Display name of the user/application issue is reported.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Reporter.DisplayName"),
  //    },
  //    {
  //      Name:        "summary",
  //              Description: "Details of the user/application that created the issue.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Summary"),
  //    },
  //    {
  //      Name:        "type",
  //              Description: "The name of the issue type.",
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Fields.Type.Name"),
  //    },
  //    {
  //      Name:        "updated",
  //              Description: "Time when the issue was last updated.",
  //            Type:        proto.ColumnType_TIMESTAMP,
  //            Transform:   transform.FromField("Fields.Updated").Transform(convertJiraTime),
  //    },
  //
  //    // JSON fields
  //    {
  //      Name:        "components",
  //              Description: "List of components associated with the issue.",
  //            Type:        proto.ColumnType_JSON,
  //            Transform:
  // transform.FromField("Fields.Components").Transform(extractComponentIds),
  //    },
  //    {
  //      Name:        "fields",
  //              Description: "Json object containing important subfields of the issue.",
  //            Type:        proto.ColumnType_JSON,
  //    },
  //    {
  //      Name:        "labels",
  //              Description: "A list of labels applied to the issue.",
  //            Type:        proto.ColumnType_JSON,
  //            Transform:   transform.FromField("Fields.Labels"),
  //    },
  //
  //    // Standard columns
  //    {
  //      Name:        "tags",
  //              Type:        proto.ColumnType_JSON,
  //            Description: "A map of label names associated with this issue, in Steampipe standard
  // format.",
  //            Transform:   transform.From(getBacklogIssueTags),
  //    },
  //    {
  //      Name:        "title",
  //              Description: ColumnDescriptionTitle,
  //            Type:        proto.ColumnType_STRING,
  //            Transform:   transform.FromField("Key"),
  //    },
  //  }),
}
