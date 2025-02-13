package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectComponentsApi;
import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.rest.platform.model.*;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.jira.tables.results.DASJiraWithParentTableResult;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.protocol.das.v1.query.PathKey;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraComponentTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_component";

  private final DASJiraTable parentTable;

  private final ProjectComponentsApi projectComponentsApi;

  public DASJiraComponentTable(
      Map<String, String> options,
      ProjectComponentsApi projectComponentsApi,
      ProjectsApi projectsApi) {
    super(
        options,
        TABLE_NAME,
        "This resource represents project components. Use it to get, create, update, and delete project components. Also get components for project and get a count of issues by component.");
    this.projectComponentsApi = projectComponentsApi;
    this.parentTable = new DASJiraProjectTable(options, projectsApi);
  }

  public String uniqueColumn() {
    return "id";
  }

  public List<PathKey> getTablePathKeys() {
    return List.of(PathKey.newBuilder().addKeyColumns("id").build());
  }

  public List<SortKey> getTableSortOrders(List<SortKey> sortKeys) {
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

  public Row insert(Row row) {
    try {
      ProjectComponent inserted =
          this.projectComponentsApi.createComponent(createProjectComponent(row));
      return toRow(toComponentWithCount(inserted), List.of());
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  public List<Row> insert(List<Row> rows) {
    return rows.stream().map(this::insert).toList();
  }

  public Row update(Value rowId, Row newValues) {
    try {
      projectComponentsApi.updateComponent(
          (String) extractValueFactory.extractValue(rowId), createProjectComponent(newValues));
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
    return super.update(rowId, newValues);
  }

  public void delete(Value rowId) {
    try {
      projectComponentsApi.deleteComponent((String) extractValueFactory.extractValue(rowId), null);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {

    return new DASJiraWithParentTableResult(
        parentTable, withParentJoin(quals, "project_id", "id"), List.of("id"), sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        return new DASJiraPaginatedResult<ComponentWithIssueCount>(limit) {
          @Override
          public Row next() {
            return toRow(this.getNext(), columns);
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
              throw new DASSdkException(
                  "Error fetching components: %s".formatted(e.getResponseBody()), e);
            }
          }
        };
      }
    };
  }

  public Row toRow(ComponentWithIssueCount componentWithIssueCount, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();

    addToRow("id", rowBuilder, componentWithIssueCount.getId(), columns);
    addToRow("name", rowBuilder, componentWithIssueCount.getName(), columns);
    addToRow("description", rowBuilder, componentWithIssueCount.getDescription(), columns);

    var self =
        Optional.ofNullable(componentWithIssueCount.getSelf()).map(Object::toString).orElse(null);
    addToRow("self", rowBuilder, self, columns);

    addToRow("project", rowBuilder, componentWithIssueCount.getProject(), columns);

    var maybeAssignee = Optional.ofNullable(componentWithIssueCount.getAssignee());

    addToRow(
        "assignee_account_id",
        rowBuilder,
        maybeAssignee.map(User::getAccountId).orElse(null),
        columns);

    addToRow(
        "assignee_display_name",
        rowBuilder,
        maybeAssignee.map(User::getDisplayName).orElse(null),
        columns);

    addToRow(
        "assignee_type",
        rowBuilder,
        maybeAssignee.map(User::getAccountType).map(User.AccountTypeEnum::getValue).orElse(null),
        columns);

    addToRow(
        "is_assignee_type_valid",
        rowBuilder,
        componentWithIssueCount.getIsAssigneeTypeValid(),
        columns);

    var issueCount =
        Optional.ofNullable(componentWithIssueCount.getIssueCount())
            .map(Math::toIntExact)
            .orElse(null);
    addToRow("issue_count", rowBuilder, issueCount, columns);

    var maybeLead = Optional.ofNullable(componentWithIssueCount.getLead());
    addToRow(
        "lead_account_id", rowBuilder, maybeLead.map(User::getAccountId).orElse(null), columns);

    addToRow(
        "lead_display_name", rowBuilder, maybeLead.map(User::getDisplayName).orElse(null), columns);

    var projectId =
        Optional.ofNullable(componentWithIssueCount.getProjectId())
            .map(Math::toIntExact)
            .orElse(null);

    addToRow("project_id", rowBuilder, projectId, columns);

    var maybeRealAssignee = Optional.ofNullable(componentWithIssueCount.getRealAssignee());
    addToRow(
        "real_assignee_account_id",
        rowBuilder,
        maybeRealAssignee.map(User::getAccountId).orElse(null),
        columns);

    addToRow(
        "real_assignee_display_name",
        rowBuilder,
        maybeRealAssignee.map(User::getDisplayName).orElse(null),
        columns);

    addToRow(
        "real_assignee_type",
        rowBuilder,
        maybeRealAssignee
            .map(User::getAccountType)
            .map(User.AccountTypeEnum::getValue)
            .orElse(null),
        columns);

    addToRow("title", rowBuilder, componentWithIssueCount.getName(), columns);
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
      throw new DASSdkException(e.getMessage(), e);
    }
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
