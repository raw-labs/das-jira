package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.EpicApi;
import com.rawlabs.das.jira.rest.software.model.Epic;
import com.rawlabs.das.jira.rest.software.model.EpicSearchResult;
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

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraEpicTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_epic";

  private EpicApi epicApi = new EpicApi();

  public DASJiraEpicTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "An epic is essentially a large user story that can be broken down into a number of smaller stories. An epic can span more than one project.");
  }

  /** Constructor for mocks */
  DASJiraEpicTable(Map<String, String> options, EpicApi epicApi) {
    this(options);
    this.epicApi = epicApi;
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      EpicSearchResult result = epicApi.searchPaginatedEpics(0, withMaxResultOrLimit(limit));
      return fromRowIterator(result.getValues().stream().map(this::toRow).iterator());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  private Row toRow(Epic epic) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, epic.getId());
    addToRow("name", rowBuilder, epic.getName());
    addToRow("key", rowBuilder, epic.getKey());
    addToRow("done", rowBuilder, epic.getDone());
    addToRow("self", rowBuilder, epic.getSelf().toString());
    addToRow("summary", rowBuilder, epic.getSummary());
    addToRow("color", rowBuilder, epic.getColor().getKey());
    addToRow("title", rowBuilder, epic.getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The id of the epic.", createIntType()));
    columns.put("name", createColumn("name", "The name of the epic.", createStringType()));
    columns.put("key", createColumn("key", "The key of the epic.", createStringType()));
    columns.put(
        "done", createColumn("done", "Indicates the status of the epic.", createBoolType()));
    columns.put("self", createColumn("self", "The URL of the epic details.", createStringType()));
    columns.put("summary", createColumn("summary", "Description of the epic.", createStringType()));
    columns.put(
        "color", createColumn("color", "Label colour details for the epic.", createStringType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
