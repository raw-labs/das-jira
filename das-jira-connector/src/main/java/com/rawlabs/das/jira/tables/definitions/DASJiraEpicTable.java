package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.jira.utils.ExceptionHandling.makeSdkException;
import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.EpicApi;
import com.rawlabs.das.jira.rest.software.model.Epic;
import com.rawlabs.das.jira.rest.software.model.EpicSearchResult;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class DASJiraEpicTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_epic";

  private final EpicApi epicApi;

  public DASJiraEpicTable(Map<String, String> options, EpicApi epicApi) {
    super(
        options,
        TABLE_NAME,
        "An epic is essentially a large user story that can be broken down into a number of smaller stories. An epic can span more than one project.");
    this.epicApi = epicApi;
  }

  public String uniqueColumn() {
    return "id";
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    try {
      EpicSearchResult result = epicApi.searchPaginatedEpics(0, withMaxResultOrLimit(limit));
      return fromRowIterator(result.getValues().stream().map(v -> toRow(v, columns)).iterator());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  private Row toRow(Epic epic, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, epic.getId(), columns);
    addToRow("name", rowBuilder, epic.getName(), columns);
    addToRow("key", rowBuilder, epic.getKey(), columns);
    addToRow("done", rowBuilder, epic.getDone(), columns);
    addToRow("self", rowBuilder, epic.getSelf().toString(), columns);
    addToRow("summary", rowBuilder, epic.getSummary(), columns);
    addToRow("color", rowBuilder, epic.getColor().getKey(), columns);
    addToRow("title", rowBuilder, epic.getName(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
