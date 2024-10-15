package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.api.ProjectRolesApi;
import com.rawlabs.das.jira.rest.platform.model.CreateUpdateRoleRequestBean;
import com.rawlabs.das.jira.rest.platform.model.ProjectRole;
import com.rawlabs.das.jira.rest.platform.model.RoleActor;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraProjectRoleTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_project_role";

  private ProjectRolesApi projectRolesApi = new ProjectRolesApi();

  public DASJiraProjectRoleTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "Project Roles are a flexible way to associate users and/or groups with particular projects.");
  }

  /** Constructor for mocks */
  DASJiraProjectRoleTable(Map<String, String> options, ProjectRolesApi projectRolesApi) {
    this(options);
    this.projectRolesApi = projectRolesApi;
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
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  private CreateUpdateRoleRequestBean createUpdateRoleRequestBean(Row row) {
    CreateUpdateRoleRequestBean createUpdateRoleRequestBean = new CreateUpdateRoleRequestBean();
    createUpdateRoleRequestBean.setName(extractValueFactory.extractValue(row, "name").toString());
    createUpdateRoleRequestBean.setDescription(
        extractValueFactory.extractValue(row, "description").toString());
    return createUpdateRoleRequestBean;
  }

  @Override
  public Row insertRow(Row row) {
    try {
      return toRow(projectRolesApi.createProjectRole(createUpdateRoleRequestBean(row)));
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      return toRow(
          projectRolesApi.fullyUpdateProjectRole(
              (Long) extractValueFactory.extractValue(rowId),
              createUpdateRoleRequestBean(newValues)));
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      projectRolesApi.deleteProjectRole((Long) extractValueFactory.extractValue(rowId), null);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      List<ProjectRole> result = projectRolesApi.getAllProjectRoles();
      return fromRowIterator(result.stream().map(this::toRow).iterator());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  private Row toRow(ProjectRole projectRoles) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, projectRoles.getId());
    addToRow("name", rowBuilder, projectRoles.getName());
    Optional.ofNullable(projectRoles.getSelf())
        .ifPresent(self -> addToRow("self", rowBuilder, self.toString()));
    addToRow("description", rowBuilder, projectRoles.getDescription());
    Optional.ofNullable(projectRoles.getActors())
        .ifPresent(
            actors -> {
              addToRow("actor_names", rowBuilder, actors.stream().map(RoleActor::getName).toList());
              addToRow(
                  "actor_account_ids", rowBuilder, actors.stream().map(RoleActor::getId).toList());
            });
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The ID of the project role.", createLongType()));
    columns.put("name", createColumn("name", "The name of the project role.", createStringType()));
    columns.put(
        "self", createColumn("self", "The URL the project role details.", createStringType()));
    columns.put(
        "description",
        createColumn("description", "The description of the project role.", createStringType()));
    columns.put(
        "actor_account_ids",
        createColumn(
            "actor_account_ids",
            "The list of user ids who act in this role.",
            createListType(createLongType())));
    columns.put(
        "actor_names",
        createColumn(
            "actor_names",
            "The list of user ids who act in this role.",
            createListType(createStringType())));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
