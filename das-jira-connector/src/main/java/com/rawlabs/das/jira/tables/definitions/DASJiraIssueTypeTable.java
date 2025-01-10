package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueTypesApi;
import com.rawlabs.das.jira.rest.platform.model.*;
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

public class DASJiraIssueTypeTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_type";

  private final IssueTypesApi issueTypesApi;

  public DASJiraIssueTypeTable(Map<String, String> options, IssueTypesApi issueTypesApi) {
    super(
        options,
        TABLE_NAME,
        "Issue types distinguish different types of work in unique ways, and help you identify, categorize, and report on your team’s work across your Jira site.");
    this.issueTypesApi = issueTypesApi;
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

  public Row insert(Row row) {
    String description = (String) extractValueFactory.extractValue(row, "description");
    Integer hierarchyLevel = (Integer) extractValueFactory.extractValue(row, "hierarchy_level");
    String name = (String) extractValueFactory.extractValue(row, "name");
    Boolean subtask = (Boolean) extractValueFactory.extractValue(row, "subtask");
    try {
      IssueTypeCreateBean issueTypeCreateBean = new IssueTypeCreateBean();
      issueTypeCreateBean.setType(
          subtask ? IssueTypeCreateBean.TypeEnum.SUBTASK : IssueTypeCreateBean.TypeEnum.STANDARD);
      issueTypeCreateBean.setDescription(description);
      issueTypeCreateBean.setName(name);
      issueTypeCreateBean.setHierarchyLevel(hierarchyLevel);
      return toRow(issueTypesApi.createIssueType(issueTypeCreateBean), List.of());
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  public Row update(Value rowId, Row newValues) {
    try {
      String id = (String) extractValueFactory.extractValue(rowId);
      IssueTypeUpdateBean issueTypeUpdateBean = new IssueTypeUpdateBean();
      var result = issueTypesApi.updateIssueType(id, issueTypeUpdateBean);
      return toRow(result, List.of());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  public void delete(Value rowId) {
    String id = (String) extractValueFactory.extractValue(rowId);
    try {
      issueTypesApi.deleteIssueType(id, null);
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, @Nullable List<SortKey> sortKeys) {
    try {
      return fromRowIterator(
          issueTypesApi.getIssueAllTypes().stream().map(i -> toRow(i, columns)).iterator());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  private Row toRow(IssueTypeDetails issueTypeDetails, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, issueTypeDetails.getId(), columns);
    addToRow("name", rowBuilder, issueTypeDetails.getName(), columns);
    addToRow("self", rowBuilder, issueTypeDetails.getSelf(), columns);
    addToRow("description", rowBuilder, issueTypeDetails.getDescription(), columns);
    addToRow("avatar_id", rowBuilder, issueTypeDetails.getAvatarId(), columns);
    var entityId =
        Optional.ofNullable(issueTypeDetails.getEntityId()).map(Object::toString).orElse(null);
    addToRow("entity_id", rowBuilder, entityId, columns);
    addToRow("hierarchy_level", rowBuilder, issueTypeDetails.getHierarchyLevel(), columns);
    addToRow("icon_url", rowBuilder, issueTypeDetails.getIconUrl(), columns);
    addToRow("subtask", rowBuilder, issueTypeDetails.getSubtask(), columns);

    var scope = Optional.ofNullable(issueTypeDetails.getScope()).map(Scope::toJson).orElse(null);
    addToRow("scope", rowBuilder, scope, columns);
    addToRow("title", rowBuilder, issueTypeDetails.getName(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
    columns.put("id", createColumn("id", "The ID of the issue type.", createStringType()));
    columns.put("name", createColumn("name", "The name of the issue type.", createStringType()));
    columns.put(
        "self", createColumn("self", "The URL of the issue type details.", createStringType()));
    columns.put(
        "description",
        createColumn("description", "The description of the issue type.", createStringType()));
    columns.put(
        "avatar_id",
        createColumn("avatar_id", "The ID of the issue type's avatar.", createLongType()));
    columns.put(
        "entity_id",
        createColumn("entity_id", "Unique ID for next-gen projects.", createStringType()));
    columns.put(
        "hierarchy_level",
        createColumn("hierarchy_level", "Hierarchy level of the issue type.", createIntType()));
    columns.put(
        "icon_url",
        createColumn("icon_url", "The URL of the issue type's avatar.", createStringType()));
    columns.put(
        "subtask",
        createColumn(
            "subtask", "Whether this issue type is used to create subtasks.", createBoolType()));
    columns.put(
        "scope",
        createColumn(
            "scope",
            "Details of the next-gen projects the issue type is available in.",
            createAnyType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
