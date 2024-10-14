package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.api.BoardApi;
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
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createIntType;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

public class DASJiraEpicTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_epic";

  private BoardApi boardApi = new BoardApi();

  public DASJiraEpicTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "An epic is essentially a large user story that can be broken down into a number of smaller stories. An epic can span more than one project.");
  }

  /** Constructor for mocks */
  DASJiraEpicTable(Map<String, String> options, BoardApi boardApi) {
    this(options);
    this.boardApi = boardApi;
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1), new KeyColumns(List.of("key"), 1));
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

  private Row toRow(Map<String, Object> epic) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, epic.get("id"));
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The id of the epic.", createIntType()));
    columns.put("name", createColumn("name", "The name of the epic.", createStringType()));
    columns.put("key", createColumn("key", "The key of the epic.", createStringType()));
    columns.put("done", createColumn("done", "Indicates the status of the epic.", createIntType()));
    columns.put("self", createColumn("self", "The URL of the epic details.", createStringType()));
    columns.put("summary", createColumn("summary", "Description of the epic.", createStringType()));
    columns.put(
        "color", createColumn("color", "Label colour details for the epic.", createStringType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
