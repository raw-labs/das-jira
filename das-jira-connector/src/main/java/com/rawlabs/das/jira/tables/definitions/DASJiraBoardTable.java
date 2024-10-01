package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.software.model.GetConfiguration200Response;
import com.rawlabs.das.jira.tables.DASJiraColumnDefinition;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.DASJiraTableDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBoardTable extends DASJiraTable {

  public String TABLE_NAME = "jira_board";

  private BoardApi api;

  public DASJiraBoardTable(Map<String, String> options) {
    super(options);
  }

  public DASJiraBoardTable(Map<String, String> options, BoardApi boardApi) {
    this(options);
    this.api = boardApi;
  }

  @Override
  public String getTableName() {
    //    var a= api.getAllBoards();
    //    a.
    return "";
  }

  @Override
  public TableDefinition getTableDefinition() {
    return null;
  }

  private Row toRow(
      GetAllBoards200ResponseValuesInner board, GetConfiguration200Response configuration) {
    Row.Builder rowBuilder = Row.newBuilder();
    dasJiraTableDefinition.updateRow("id", rowBuilder, board.getId());
    dasJiraTableDefinition.updateRow("name", rowBuilder, board.getName());
    dasJiraTableDefinition.updateRow("self", rowBuilder, board.getSelf());
    dasJiraTableDefinition.updateRow("type", rowBuilder, board.getType());
    assert configuration.getFilter() != null;
    dasJiraTableDefinition.updateRow("filter_id", rowBuilder, configuration.getFilter().getId());
    assert configuration.getSubQuery() != null;
    dasJiraTableDefinition.updateRow(
        "sub_query", rowBuilder, configuration.getSubQuery().getQuery());
    return rowBuilder.build();
  }

  DASJiraTableDefinition dasJiraTableDefinition =
      new DASJiraTableDefinition(
          TABLE_NAME,
          "The application properties that are accessible on the Advanced Settings page.",
          Map.of(
              "id",
              new DASJiraColumnDefinition("id", "The ID of the board.", createStringType()),
              "name",
              new DASJiraColumnDefinition("name", "The name of the board.", createStringType()),
              "self",
              new DASJiraColumnDefinition(
                  "self", "The URL of the board details.", createStringType()),
              "type",
              new DASJiraColumnDefinition(
                  "type",
                  "The board type of the board. Valid values are simple, scrum and kanban.",
                  createStringType()),
              "filter_id",
              new DASJiraColumnDefinition(
                  "filter_id", "Filter id of the board.", createStringType()),
              "sub_query",
              new DASJiraColumnDefinition(
                  "sub_query",
                  "JQL subquery used by the given board - (Kanban only).",
                  createStringType()),
              "title",
              new DASJiraColumnDefinition("title", TITLE_DESC, createStringType())));
}
