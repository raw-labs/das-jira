package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectComponentsApi;
import com.rawlabs.das.jira.rest.platform.model.ComponentIssuesCount;
import com.rawlabs.das.jira.rest.platform.model.ProjectComponent;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

public class DASJiraComponentTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_component";

  private final DASJiraTable parentTable;

  private ProjectComponentsApi projectComponentsApi = new ProjectComponentsApi();

  public DASJiraComponentTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "This resource represents project components. Use it to get, create, update, and delete project components. Also get components for project and get a count of issues by component.");
    this.parentTable = new DASJiraProjectTable(options);
  }

  public DASJiraComponentTable(
      Map<String, String> options, ProjectComponentsApi projectComponentsApi) {
    this(options);
    this.projectComponentsApi = projectComponentsApi;
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }

  public Row toRow(ProjectComponent projectComponent) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, projectComponent.getId());
    addToRow("name", rowBuilder, projectComponent.getName());
    addToRow("description", rowBuilder, projectComponent.getDescription());
    addToRow("self", rowBuilder, projectComponent.getSelf());
    addToRow("project", rowBuilder, projectComponent.getProject());

    Optional.ofNullable(projectComponent.getAssignee())
        .ifPresent(
            assignee -> {
              addToRow("assignee_account_id", rowBuilder, assignee.getAccountId());
              addToRow("assignee_display_name", rowBuilder, assignee.getDisplayName());
              Optional.ofNullable(assignee.getAccountType())
                  .ifPresent(
                      accountType -> addToRow("assignee_type", rowBuilder, accountType.getValue()));
            });

    addToRow("is_assignee_type_valid", rowBuilder, projectComponent.getIsAssigneeTypeValid());
    try {
      ComponentIssuesCount issuesCount =
          projectComponentsApi.getComponentRelatedIssues(projectComponent.getId());
      addToRow("issue_count", rowBuilder, issuesCount.getIssueCount());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
    Optional.ofNullable(projectComponent.getLead())
        .ifPresent(
            lead -> {
              addToRow("lead_account_id", rowBuilder, lead.getAccountId());
              addToRow("lead_display_name", rowBuilder, lead.getDisplayName());
            });
    addToRow("project_id", rowBuilder, projectComponent.getProjectId());
    Optional.ofNullable(projectComponent.getRealAssignee())
        .ifPresent(
            realAssignee -> {
              addToRow("real_assignee_account_id", rowBuilder, realAssignee.getAccountId());
              addToRow("real_assignee_display_name", rowBuilder, realAssignee.getDisplayName());
              Optional.ofNullable(realAssignee.getAccountType())
                  .ifPresent(
                      accountType ->
                          addToRow("real_assignee_type", rowBuilder, accountType.getValue()));
            });
    addToRow("title", rowBuilder, projectComponent.getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put(
        "id", createColumn("id", "The unique identifier for the component.", createStringType()));
    columns.put("name", createColumn("name", "The name for the component.", createStringType()));
    columns.put(
        "description",
        createColumn("description", "The description for the component.", createStringType()));
    columns.put(
        "self",
        createColumn(
            "self",
            "The URL for this count of the issues contained in the component.",
            createStringType()));
    columns.put(
        "project",
        createColumn(
            "project",
            "The key of the project to which the component is assigned.",
            createStringType()));
    columns.put(
        "assignee_account_id",
        createColumn(
            "assignee_account_id",
            "The account id of the user associated with assigneeType, if any.",
            createStringType()));
    columns.put(
        "assignee_display_name",
        createColumn(
            "assignee_display_name",
            "The display name of the user associated with assigneeType, if any.",
            createStringType()));
    columns.put(
        "assignee_type",
        createColumn(
            "assignee_type",
            "The nominal user type used to determine the assignee for issues created with this component.",
            createStringType()));
    columns.put(
        "is_assignee_type_valid",
        createColumn(
            "is_assignee_type_valid",
            "Whether a user is associated with assigneeType.",
            createBoolType()));
    columns.put(
        "issue_count",
        createColumn("issue_count", "The count of issues for the component.", createIntType()));
    columns.put(
        "lead_account_id",
        createColumn(
            "lead_account_id",
            "The account id for the component's lead user.",
            createStringType()));
    columns.put(
        "lead_display_name",
        createColumn(
            "lead_display_name",
            "The display name for the component's lead user.",
            createStringType()));
    columns.put(
        "project_id",
        createColumn(
            "project_id", "The ID of the project the component belongs to.", createIntType()));
    columns.put(
        "real_assignee_account_id",
        createColumn(
            "real_assignee_account_id",
            "The account id of the user assigned to issues created with this component, when assigneeType does not identify a valid assignee.",
            createStringType()));
    columns.put(
        "real_assignee_display_name",
        createColumn(
            "real_assignee_display_name",
            "The display name of the user assigned to issues created with this component, when assigneeType does not identify a valid assignee.",
            createStringType()));
    columns.put(
        "real_assignee_type",
        createColumn(
            "real_assignee_type",
            "The type of the assignee that is assigned to issues created with this component, when an assignee cannot be set from the assigneeType.",
            createStringType()));
    columns.put("title", createColumn("title", "The name for the component.", createStringType()));

    return columns;
  }
}
