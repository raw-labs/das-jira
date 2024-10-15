package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.api.SprintApi;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createBoolType;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

public class DASJiraSprintTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_sprint";

  private SprintApi sprintApi = new SprintApi();

  public DASJiraSprintTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "Sprint is a short period in which the development team implements and delivers a discrete and potentially shippable application increment.");
  }

  /** Constructor for mocks */
  DASJiraSprintTable(Map<String, String> options, SprintApi sprintApi) {
    this(options);
    this.sprintApi = sprintApi;
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
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }

  private Row toRow() {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The ID of the sprint.", createStringType()));
    columns.put("name", createColumn("name", "The name of the sprint.", createStringType()));
    columns.put(
        "board_id",
        createColumn(
            "board_id", "The ID of the board the sprint belongs to.z", createStringType()));
    columns.put("self", createColumn("self", "The URL of the sprint details.", createStringType()));
    columns.put("state", createColumn("state", "Status of the sprint.", createStringType()));
    columns.put(
        "start_date",
        createColumn("start_date", "The start timestamp of the sprint.", createStringType()));
    columns.put(
        "end_date",
        createColumn(
            "end_date", "The projected time of completion of the sprint.", createStringType()));
    columns.put(
        "complete_date",
        createColumn(
            "complete_date", "Date the sprint was marked as complete.", createStringType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
