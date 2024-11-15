package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueTypesApi;
import com.rawlabs.das.jira.rest.platform.model.*;
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

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraIssueTypeTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue_type";

  private IssueTypesApi issueTypesApi = new IssueTypesApi();

  public DASJiraIssueTypeTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "Issue types distinguish different types of work in unique ways, and help you identify, categorize, and report on your team’s work across your Jira site.");
  }

  /** Constructor for mocks */
  DASJiraIssueTypeTable(Map<String, String> options, IssueTypesApi issueTypesApi) {
    this(options);
    this.issueTypesApi = issueTypesApi;
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

  @Override
  public Row insertRow(Row row) {
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
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      String id = (String) extractValueFactory.extractValue(rowId);
      IssueTypeUpdateBean issueTypeUpdateBean = new IssueTypeUpdateBean();
      var result = issueTypesApi.updateIssueType(id, issueTypeUpdateBean);
      return toRow(result, List.of());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    String id = (String) extractValueFactory.extractValue(rowId);
    try {
      issueTypesApi.deleteIssueType(id, null);
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
