package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.software.model.GetConfiguration200Response;
import com.rawlabs.das.jira.rest.software.model.GetConfiguration200ResponseColumnConfigColumnsInnerStatusesInner;
import com.rawlabs.das.jira.rest.software.model.GetConfiguration200ResponseSubQuery;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBoardTable extends DASJiraBaseTable {

  public String TABLE_NAME = "jira_board";

  private BoardApi boardApi;

  public DASJiraBoardTable(Map<String, String> options) {
    super(options);
  }

  public DASJiraBoardTable(Map<String, String> options, BoardApi boardApi) {
    this(options);
    this.boardApi = boardApi;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public TableDefinition getTableDefinition() {
    return dasJiraTableDefinition.getTableDefinition();
  }

  public final DASJiraTableDefinition dasJiraTableDefinition =
      new DASJiraTableDefinition(
          TABLE_NAME,
          "A board displays issues from one or more projects, giving you a flexible way of viewing, managing, and reporting on work in progress.",
          List.of(
              new DASJiraColumnDefinition(
                  "id",
                  "The ID of the board.",
                  createStringType(),
                  (Object board) -> ((GetAllBoards200ResponseValuesInner) board).getId(),
                  new DASJiraTableDefinition(
                      "board_config",
                      "configuration of the board",
                      List.of(
                          new DASJiraColumnDefinition(
                              "type",
                              "The board type of the board. Valid values are simple, scrum and kanban.",
                              createStringType(),
                              (Object configuration) ->
                                  ((GetConfiguration200Response) configuration).getType()),
                          new DASJiraColumnDefinition(
                              "filter_id",
                              "Filter id of the board.",
                              createStringType(),
                              (Object configuration) -> {
                                GetConfiguration200ResponseColumnConfigColumnsInnerStatusesInner
                                    filter =
                                        ((GetConfiguration200Response) configuration).getFilter();
                                return filter != null ? filter.getId() : null;
                              }),
                          new DASJiraColumnDefinition(
                              "sub_query",
                              "JQL subquery used by the given board - (Kanban only).",
                              createStringType(),
                              (Object configuration) -> {
                                GetConfiguration200ResponseSubQuery subQuery =
                                    ((GetConfiguration200Response) configuration).getSubQuery();
                                return subQuery != null ? subQuery.getQuery() : null;
                              })),
                      (quals, columns, sortKeys, limit) -> {
                        // to do
                        return List.of(1, 2, 3).iterator();
                      })),
              new DASJiraColumnDefinition(
                  "name",
                  "The name of the board.",
                  createStringType(),
                  (Object board) -> ((GetAllBoards200ResponseValuesInner) board).getName()),
              new DASJiraColumnDefinition(
                  "self",
                  "The URL of the board details.",
                  createStringType(),
                  (Object board) -> ((GetAllBoards200ResponseValuesInner) board).getSelf()),
              new DASJiraColumnDefinition(
                  "title",
                  TITLE_DESC,
                  createStringType(),
                  (Object board) -> ((GetAllBoards200ResponseValuesInner) board).getName())),
          (quals, columns, sortKeys, limit) -> {
            return List.of(1, 2, 3).iterator();
          });
  //
  //  public final DASJiraChunkedTableDefinition dasJiraBoardTableDefinition =
  //      new DASJiraChunkedTableDefinition() {
  //        private final TableDefinition tableDefinition =
  //            createTable(
  //                TABLE_NAME,
  //                "A board displays issues from one or more projects, giving you a flexible way of
  // viewing, managing, and reporting on work in progress.",
  //                Stream.concat(
  //                        dasJiraBoardTableChunk.getColumnDefinitions().stream(),
  //                        dasJiraBoardConfigurationTableChunk.getColumnDefinitions().stream())
  //                    .map(DASJiraColumnDefinition::getColumnDefinition)
  //                    .toList());
  //
  //        @Override
  //        public TableDefinition getTableDefinition() {
  //          return tableDefinition;
  //        }
  //
  //        @Override
  //        public String getName() {
  //          return TABLE_NAME;
  //        }
  //
  //        public Row toRow(
  //            GetAllBoards200ResponseValuesInner board, GetConfiguration200Response configuration)
  // {
  //          Row.Builder rowBuilder = Row.newBuilder();
  //          dasJiraBoardTableChunk.updateRow(rowBuilder, board);
  //          dasJiraBoardConfigurationTableChunk.updateRow(rowBuilder, configuration);
  //          return rowBuilder.build();
  //        }
  //      };

  //        new DASJiraTableDefinition(
  //            TABLE_NAME,
  //          "The application properties that are accessible on the Advanced Settings page.",
  //            List.of(
  //                    new DASJiraColumnDefinition(
  //                    "id",
  //                  "The unique identifier of the property.",
  //            createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getId()),
  //            new DASJiraColumnDefinition(
  //                  "name",
  //                          "The name of the application property.",
  //                  createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getName()),
  //            new DASJiraColumnDefinition(
  //                  "description",
  //                          "The description of the application property.",
  //                  createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getDesc()),
  //            new DASJiraColumnDefinition(
  //                  "key",
  //                          "The key of the application property.",
  //                  createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getKey()),
  //            new DASJiraColumnDefinition(
  //                  "type",
  //                          "The data type of the application property.",
  //                  createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getType()),
  //            new DASJiraColumnDefinition(
  //                  "value",
  //                          "The new value.",
  //                  createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getValue()),
  //            new DASJiraColumnDefinition(
  //                  "allowed_values",
  //                          "The allowed values, if applicable.",
  //                  createListType(createStringType()),
  //            (Object ap) -> ((ApplicationProperty) ap).getAllowedValues()),
  //            new DASJiraColumnDefinition(
  //                  "title",
  //                  TITLE_DESC,
  //                  createStringType(),
  //                  (Object ap) -> ((ApplicationProperty) ap).getName())));

  //  public final DASJiraTableDefinitionChunk<GetAllBoards200ResponseValuesInner>
  //      dasJiraBoardTableChunk =
  //          new DASJiraTableDefinitionChunk<>(
  //              List.of(
  //                  new DASJiraColumnDefinition<>(
  //                      "id",
  //                      "The ID of the board.",
  //                      createStringType(),
  //                      GetAllBoards200ResponseValuesInner::getId),
  //                  new DASJiraColumnDefinition<>(
  //                      "name",
  //                      "The name of the board.",
  //                      createStringType(),
  //                      GetAllBoards200ResponseValuesInner::getName),
  //                  new DASJiraColumnDefinition<>(
  //                      "self",
  //                      "The URL of the board details.",
  //                      createStringType(),
  //                      GetAllBoards200ResponseValuesInner::getSelf),
  //                  new DASJiraColumnDefinition<>(
  //                      "title",
  //                      TITLE_DESC,
  //                      createStringType(),
  //                      GetAllBoards200ResponseValuesInner::getName)));

  //  public final DASExecuteResult getBoardResult(@Nullable Long limit) {
  //
  //    this.boardApi = new BoardApi();
  //
  //    return new DASExecuteResult() {
  //      @Override
  //      public void close() throws IOException {}
  //
  //      @Override
  //      public boolean hasNext() {
  //        return false;
  //      }
  //
  //      @Override
  //      public Row next() {
  //        return null;
  //      }
  //    };
  //  }
  //
  //  public final DASJiraTableDefinitionChunk<GetConfiguration200Response>
  //      dasJiraBoardConfigurationTableChunk =
  //          new DASJiraTableDefinitionChunk<>(
  //              List.of(
  //                  new DASJiraColumnDefinition<>(
  //                      "type",
  //                      "The board type of the board. Valid values are simple, scrum and kanban.",
  //                      createStringType(),
  //                      GetConfiguration200Response::getType),
  //                  new DASJiraColumnDefinition<>(
  //                      "filter_id",
  //                      "Filter id of the board.",
  //                      createStringType(),
  //                      (GetConfiguration200Response response) -> {
  //                        GetConfiguration200ResponseColumnConfigColumnsInnerStatusesInner filter
  // =
  //                            response.getFilter();
  //                        return filter != null ? filter.getId() : null;
  //                      }),
  //                  new DASJiraColumnDefinition<>(
  //                      "sub_query",
  //                      "JQL subquery used by the given board - (Kanban only).",
  //                      createStringType(),
  //                      (GetConfiguration200Response response) -> {
  //                        GetConfiguration200ResponseSubQuery subQuery = response.getSubQuery();
  //                        return subQuery != null ? subQuery.getQuery() : null;
  //                      })));
}
