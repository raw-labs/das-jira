package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEqDistinct;
import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.*;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.protocol.das.v1.query.PathKey;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.util.*;
import javax.annotation.Nullable;

public class DASJiraBoardTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_board";

  private final BoardApi boardApi;

  public DASJiraBoardTable(Map<String, String> options, BoardApi boardApi) {
    super(
        options,
        TABLE_NAME,
        "A board displays issues from one or more projects, giving you a flexible way of viewing, managing, and reporting on work in progress.");
    this.boardApi = boardApi;
  }

  public List<SortKey> getTableSortOrders(List<SortKey> sortKeys) {
    return sortKeys.stream()
        .filter(sortKey -> sortKey.getName().equals("name") || sortKey.getName().equals("title"))
        .toList();
  }

  public List<PathKey> getTablePathKeys() {
    return List.of(PathKey.newBuilder().addKeyColumns("id").build());
  }

  public String uniqueColumn() {
    return "id";
  }

  public Row insert(Row row) {
    String name = (String) extractValueFactory.extractValue(row, "name");
    String type = (String) extractValueFactory.extractValue(row, "type");
    Long filter_id = (Long) extractValueFactory.extractValue(row, "filter_id");

    CreateBoardRequest board = new CreateBoardRequest();
    board.setName(name);
    board.setFilterId(filter_id);
    board.setType(CreateBoardRequest.TypeEnum.fromValue(type));
    try {
      GetAllBoards200ResponseValuesInner result = boardApi.createBoard(board);
      return toRow(result, null);
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage(), e);
    }
  }

  public void delete(Value rowId) {
    try {
      boardApi.deleteBoard((Long) extractValueFactory.extractValue(rowId));
    } catch (ApiException e) {
      throw new DASSdkException(e.getMessage(), e);
    }
  }

  public List<Row> bulkInsert(List<Row> rows) {
    return rows.stream().map(this::insert).toList();
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, @Nullable List<SortKey> sortKeys) {
    try {
      Long id = (Long) extractEqDistinct(quals, "id");

      if (id != null) {
        GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner =
            boardApi.getBoard(id);
        return fromRowIterator(
            List.of(toRow(getAllBoards200ResponseValuesInner, columns)).iterator());
      } else {
        int maxResults = withMaxResultOrLimit(null);
        String type = (String) extractEqDistinct(quals, "type");
        String name =
            Optional.ofNullable(extractEqDistinct(quals, "name"))
                .map(Object::toString)
                .orElse((String) extractEqDistinct(quals, "title"));
        Long filterId = (Long) extractEqDistinct(quals, "filter_id");

        return new DASJiraPaginatedResult<GetAllBoards200ResponseValuesInner>(null) {

          @Override
          public DASJiraPage<GetAllBoards200ResponseValuesInner> fetchPage(long offset) {
            try {
              GetAllBoards200Response getAllBoards200ResponsePage =
                  boardApi.getAllBoards(
                      offset,
                      maxResults,
                      type,
                      name,
                      null,
                      null,
                      null,
                      null,
                      null,
                      withOrderBy(sortKeys),
                      null,
                      null,
                      filterId);
              return new DASJiraPage<>(
                  getAllBoards200ResponsePage.getValues(), getAllBoards200ResponsePage.getTotal());
            } catch (ApiException e) {
              throw new DASSdkException(
                  "Failed to fetch boards: %s".formatted(e.getResponseBody()));
            }
          }

          @Override
          public Row next() {
            GetAllBoards200ResponseValuesInner next = this.getNext();
            try {
              return toRow(next, columns);
            } catch (ApiException e) {
              throw new DASSdkException("Failed to fetch board configuration", e);
            }
          }
        };
      }
    } catch (ApiException e) {
      throw new DASSdkException("Failed to fetch advanced settings", e);
    }
  }

  private Row toRow(
      GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner, List<String> columns)
      throws ApiException {
    try {
      Row row = getBoardsRow(getAllBoards200ResponseValuesInner, columns);
      if (columns == null
          || columns.isEmpty()
          || columns.contains("filter_id")
          || columns.contains("sub_query")) {
        GetConfiguration200Response config =
            boardApi.getConfiguration(getAllBoards200ResponseValuesInner.getId());
        row = addConfigToRow(row, config, columns);
      }
      return row;
    } catch (ApiException e) {
      throw new DASSdkException("Failed to fetch board configuration", e);
    }
  }

  private Row getBoardsRow(
      GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, getAllBoards200ResponseValuesInner.getId(), columns);
    addToRow("name", rowBuilder, getAllBoards200ResponseValuesInner.getName(), columns);
    String self =
        Optional.ofNullable(getAllBoards200ResponseValuesInner.getSelf())
            .map(Object::toString)
            .orElse(null);
    addToRow("self", rowBuilder, self, columns);
    addToRow("type", rowBuilder, getAllBoards200ResponseValuesInner.getType(), columns);
    addToRow("title", rowBuilder, getAllBoards200ResponseValuesInner.getName(), columns);
    return rowBuilder.build();
  }

  private Row addConfigToRow(Row row, GetConfiguration200Response config, List<String> columns) {
    Row.Builder rowBuilder = row.toBuilder();
    Long filterId =
        Optional.ofNullable(config.getFilter())
            .map(GetConfiguration200ResponseColumnConfigColumnsInnerStatusesInner::getId)
            .map(id -> id.isEmpty() ? null : Long.parseLong(id))
            .orElse(null);
    addToRow("filter_id", rowBuilder, filterId, columns);
    String subQuery =
        Optional.ofNullable(config.getSubQuery())
            .map(GetConfiguration200ResponseSubQuery::getQuery)
            .orElse(null);
    addToRow("sub_query", rowBuilder, subQuery, columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
    columns.put("id", createColumn("id", "The ID of the board.", createLongType()));
    columns.put("name", createColumn("name", "The name of the board.", createStringType()));
    columns.put("self", createColumn("self", "The URL of the board details.", createStringType()));
    columns.put(
        "type",
        createColumn(
            "type",
            "The board type of the board. Valid values are simple, scrum and kanban.",
            createStringType()));
    columns.put(
        "filter_id", createColumn("filter_id", "Filter id of the board.", createLongType()));
    columns.put(
        "sub_query",
        createColumn(
            "sub_query",
            "JQL subquery used by the given board - (Kanban only).",
            createStringType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
