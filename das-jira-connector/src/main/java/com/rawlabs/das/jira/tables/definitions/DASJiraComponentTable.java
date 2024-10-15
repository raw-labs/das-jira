package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectComponentsApi;
import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.rest.platform.model.ComponentIssuesCount;
import com.rawlabs.das.jira.rest.platform.model.ComponentWithIssueCount;
import com.rawlabs.das.jira.rest.platform.model.PageBeanComponentWithIssueCount;
import com.rawlabs.das.jira.rest.platform.model.ProjectComponent;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.jira.tables.results.DASJiraWithParentTableResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

public class DASJiraComponentTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_component";

  private DASJiraTable parentTable;

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

  public DASJiraComponentTable(
      Map<String, String> options,
      ProjectComponentsApi projectComponentsApi,
      ProjectsApi projectsApi) {
    this(options, projectComponentsApi);
    this.parentTable = new DASJiraProjectTable(options, projectsApi);
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1));
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    List<String> availableForSorting = List.of("name", "description", "title");
    return sortKeys.stream()
        .filter(sortKey -> availableForSorting.contains(sortKey.getName()))
        .toList();
  }

  private ProjectComponent createProjectComponent(Row newValues) {
    ProjectComponent projectComponent = new ProjectComponent();
    projectComponent.setDescription(
        (String) extractValueFactory.extractValue(newValues, "description"));
    projectComponent.setName((String) extractValueFactory.extractValue(newValues, "name"));
    projectComponent.setProject((String) extractValueFactory.extractValue(newValues, "project"));
    projectComponent.setAssigneeType(
        ProjectComponent.AssigneeTypeEnum.fromValue(
            (String) extractValueFactory.extractValue(newValues, "assignee_type")));
    projectComponent.setLeadAccountId(
        (String) extractValueFactory.extractValue(newValues, "lead_account_id"));
    projectComponent.setLeadUserName(
        (String) extractValueFactory.extractValue(newValues, "lead_display_name"));
    return projectComponent;
  }

  @Override
  public Row insertRow(Row row) {
    try {
      ProjectComponent inserted =
          this.projectComponentsApi.createComponent(createProjectComponent(row));
      return toRow(toComponentWithCount(inserted));
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      projectComponentsApi.updateComponent(
          (String) extractValueFactory.extractValue(rowId), createProjectComponent(newValues));
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
    return super.updateRow(rowId, newValues);
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      projectComponentsApi.deleteComponent((String) extractValueFactory.extractValue(rowId), null);
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

    return new DASJiraWithParentTableResult(
        parentTable, withParentJoin(quals, "project_id", "id"), List.of("id"), sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        return new DASJiraPaginatedResult<ComponentWithIssueCount>() {
          @Override
          public Row next() {
            return toRow(this.getNext());
          }

          @Override
          public DASJiraPage<ComponentWithIssueCount> fetchPage(long offset) {
            try {
              String projectId = (String) extractValueFactory.extractValue(parentRow, "id");
              PageBeanComponentWithIssueCount components =
                  projectComponentsApi.getProjectComponentsPaginated(
                      projectId,
                      offset,
                      withMaxResultOrLimit(limit),
                      withOrderBy(sortKeys),
                      null,
                      null);
              return new DASJiraPage<>(components.getValues(), components.getTotal());
            } catch (ApiException e) {
              throw new RuntimeException(e);
            }
          }
        };
      }
    };
  }

  public Row toRow(ComponentWithIssueCount componentWithIssueCount) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, componentWithIssueCount.getId());
    addToRow("name", rowBuilder, componentWithIssueCount.getName());
    addToRow("description", rowBuilder, componentWithIssueCount.getDescription());

    Optional.ofNullable(componentWithIssueCount.getSelf())
        .ifPresent(self -> addToRow("self", rowBuilder, self.toString()));

    addToRow("project", rowBuilder, componentWithIssueCount.getProject());

    Optional.ofNullable(componentWithIssueCount.getAssignee())
        .ifPresent(
            assignee -> {
              addToRow("assignee_account_id", rowBuilder, assignee.getAccountId());
              addToRow("assignee_display_name", rowBuilder, assignee.getDisplayName());
              Optional.ofNullable(assignee.getAccountType())
                  .ifPresent(
                      accountType -> addToRow("assignee_type", rowBuilder, accountType.getValue()));
            });

    addToRow(
        "is_assignee_type_valid", rowBuilder, componentWithIssueCount.getIsAssigneeTypeValid());

    Optional.ofNullable(componentWithIssueCount.getIssueCount())
        .ifPresent(issueCount -> addToRow("issue_count", rowBuilder, Math.toIntExact(issueCount)));

    Optional.ofNullable(componentWithIssueCount.getLead())
        .ifPresent(
            lead -> {
              addToRow("lead_account_id", rowBuilder, lead.getAccountId());
              addToRow("lead_display_name", rowBuilder, lead.getDisplayName());
            });

    Optional.ofNullable(componentWithIssueCount.getProjectId())
        .ifPresent(projectId -> addToRow("project_id", rowBuilder, Math.toIntExact(projectId)));

    Optional.ofNullable(componentWithIssueCount.getRealAssignee())
        .ifPresent(
            realAssignee -> {
              addToRow("real_assignee_account_id", rowBuilder, realAssignee.getAccountId());
              addToRow("real_assignee_display_name", rowBuilder, realAssignee.getDisplayName());
              Optional.ofNullable(realAssignee.getAccountType())
                  .ifPresent(
                      accountType ->
                          addToRow("real_assignee_type", rowBuilder, accountType.getValue()));
            });
    addToRow("title", rowBuilder, componentWithIssueCount.getName());
    return rowBuilder.build();
  }

  public ComponentWithIssueCount toComponentWithCount(ProjectComponent projectComponent) {
    try {
      ComponentWithIssueCount.AssigneeTypeEnum assigneeType =
          Optional.ofNullable(projectComponent.getAssigneeType())
              .flatMap(
                  assigneeTypeEnum ->
                      Optional.ofNullable(assigneeTypeEnum.getValue())
                          .map(ComponentWithIssueCount.AssigneeTypeEnum::fromValue))
              .orElse(null);

      ComponentWithIssueCount.RealAssigneeTypeEnum realAssigneeType =
          Optional.ofNullable(projectComponent.getRealAssigneeType())
              .flatMap(
                  realAssigneeTypeEnum ->
                      Optional.ofNullable(realAssigneeTypeEnum.getValue())
                          .map(ComponentWithIssueCount.RealAssigneeTypeEnum::fromValue))
              .orElse(null);
      ComponentIssuesCount issuesCount =
          projectComponentsApi.getComponentRelatedIssues(projectComponent.getId());

      return new ComponentWithIssueCount(
          assigneeType,
          projectComponent.getDescription(),
          projectComponent.getId(),
          projectComponent.getIsAssigneeTypeValid(),
          issuesCount.getIssueCount(),
          projectComponent.getName(),
          projectComponent.getProject(),
          projectComponent.getProjectId(),
          realAssigneeType,
          projectComponent.getSelf());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
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
