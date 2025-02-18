package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectRolesApi;
import com.rawlabs.das.jira.rest.platform.model.CreateUpdateRoleRequestBean;
import com.rawlabs.das.jira.rest.platform.model.ProjectRole;
import com.rawlabs.das.jira.rest.platform.model.RoleActor;
import com.rawlabs.das.jira.tables.DASJiraTable;
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

public class DASJiraProjectRoleTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_project_role";

  private final ProjectRolesApi projectRolesApi;

  public DASJiraProjectRoleTable(Map<String, String> options, ProjectRolesApi projectRolesApi) {
    super(
        options,
        TABLE_NAME,
        "Project Roles are a flexible way to associate users and/or groups with particular projects.");
    this.projectRolesApi = projectRolesApi;
  }

  public String uniqueColumn() {
    return "id";
  }

  public List<PathKey> getTablePathKeys() {
    return List.of(PathKey.newBuilder().addKeyColumns("id").build());
  }

  public List<Row> bulkInsert(List<Row> rows) {
    return rows.stream().map(this::insert).toList();
  }

  private CreateUpdateRoleRequestBean createUpdateRoleRequestBean(Row row) {
    CreateUpdateRoleRequestBean createUpdateRoleRequestBean = new CreateUpdateRoleRequestBean();
    createUpdateRoleRequestBean.setName(extractValueFactory.extractValue(row, "name").toString());
    createUpdateRoleRequestBean.setDescription(
        extractValueFactory.extractValue(row, "description").toString());
    return createUpdateRoleRequestBean;
  }

  public Row insert(Row row) {
    try {
      return toRow(projectRolesApi.createProjectRole(createUpdateRoleRequestBean(row)), List.of());
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  public Row update(Value rowId, Row newValues) {
    try {
      return toRow(
          projectRolesApi.fullyUpdateProjectRole(
              (Long) extractValueFactory.extractValue(rowId),
              createUpdateRoleRequestBean(newValues)),
          List.of());
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  public void delete(Value rowId) {
    try {
      projectRolesApi.deleteProjectRole((Long) extractValueFactory.extractValue(rowId), null);
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    try {
      List<ProjectRole> result = projectRolesApi.getAllProjectRoles();
      return fromRowIterator(result.stream().map(r -> toRow(r, columns)).iterator());
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  private Row toRow(ProjectRole projectRoles, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, projectRoles.getId(), columns);
    addToRow("name", rowBuilder, projectRoles.getName(), columns);

    var self = Optional.ofNullable(projectRoles.getSelf()).map(Object::toString).orElse(null);
    addToRow("self", rowBuilder, self, columns);

    addToRow("description", rowBuilder, projectRoles.getDescription(), columns);

    var actors = Optional.ofNullable(projectRoles.getActors());
    addToRow(
        "actor_account_ids",
        rowBuilder,
        actors.map(a -> a.stream().map(RoleActor::getId).toList()).orElse(null),
        columns);

    addToRow(
        "actor_names",
        rowBuilder,
        actors.map(a -> a.stream().map(RoleActor::getName).toList()).orElse(null),
        columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
