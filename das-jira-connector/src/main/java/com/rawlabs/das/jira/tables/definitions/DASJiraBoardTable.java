package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.*;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.jira.tables.page.DASJiraPage;
import com.rawlabs.das.jira.tables.page.DASJiraPagedResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;

import javax.annotation.Nullable;
import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEq;
import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBoardTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_board";

  private BoardApi boardApi = new BoardApi();

  public DASJiraBoardTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "A board displays issues from one or more projects, giving you a flexible way of viewing, managing, and reporting on work in progress.");
  }

  public DASJiraBoardTable(Map<String, String> options, BoardApi boardApi) {
    this(options);
    this.boardApi = boardApi;
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    return sortKeys.stream()
        .filter(sortKey -> sortKey.getName().equals("name") || sortKey.getName().equals("title"))
        .toList();
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1));
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public Row insertRow(Row row) {
    String name = (String) extractValueFactory.extractValue(row.getDataMap().get("name"));
    String type = (String) extractValueFactory.extractValue(row.getDataMap().get("type"));
    Long filter_id = (Long) extractValueFactory.extractValue(row.getDataMap().get("filter_id"));

    CreateBoardRequest board = new CreateBoardRequest();
    board.setName(name);
    board.setFilterId(filter_id);
    board.setType(CreateBoardRequest.TypeEnum.fromValue(type));
    try {
      GetAllBoards200ResponseValuesInner result = boardApi.createBoard(board);
      return toRow(result, boardApi.getConfiguration(result.getId()));
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      boardApi.deleteBoard((Long) extractValueFactory.extractValue(rowId));
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
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
    try {
      Long id = (Long) extractEq(quals, "id");

      if (id != null) {
        GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner =
            boardApi.getBoard(id);
        GetConfiguration200Response config = boardApi.getConfiguration(id);
        return fromRowIterator(
            List.of(toRow(getAllBoards200ResponseValuesInner, config)).iterator());
      } else {
        int maxResults = withMaxResult(limit);
        String type = (String) extractEq(quals, "type");
        String name = (String) extractEq(quals, "name");
        name = name == null ? (String) extractEq(quals, "title") : name;
        Long filterId = (Long) extractEq(quals, "filter_id");

        String orderBy;
        if (sortKeys != null) {
          orderBy =
              sortKeys.stream()
                  .filter(
                      sortKey ->
                          sortKey.getName().equals("name") || sortKey.getName().equals("title"))
                  .findFirst()
                  .map(sortKey -> sortKey.getIsReversed() ? "-name" : "+name")
                  .orElse(null);
        } else {
          orderBy = null;
        }

        String finalName = name;
        return new DASJiraPagedResult<>(
            (startAt) -> {
              try {
                GetAllBoards200Response getAllBoards200ResponsePage =
                    boardApi.getAllBoards(
                        startAt,
                        maxResults,
                        type,
                        finalName,
                        null,
                        null,
                        null,
                        null,
                        null,
                        orderBy,
                        null,
                        null,
                        filterId);
                return new DASJiraPage<>(
                    getAllBoards200ResponsePage.getValues(),
                    getAllBoards200ResponsePage.getTotal());
              } catch (ApiException e) {
                throw new DASSdkApiException("Failed to fetch boards", e);
              }
            }) {

          @Override
          public Row next() {
            GetAllBoards200ResponseValuesInner next = this.getNext();
            try {
              GetConfiguration200Response config = boardApi.getConfiguration(next.getId());
              return toRow(next, config);
            } catch (ApiException e) {
              throw new DASSdkApiException("Failed to fetch board configuration", e);
            }
          }
        };
      }
    } catch (ApiException e) {
      throw new DASSdkApiException("Failed to fetch advanced settings", e);
    }
  }

  private Row toRow(
      GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner,
      GetConfiguration200Response config) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, getAllBoards200ResponseValuesInner.getId());
    addToRow("name", rowBuilder, getAllBoards200ResponseValuesInner.getName());
    String self =
        Optional.ofNullable(getAllBoards200ResponseValuesInner.getSelf())
            .map(Object::toString)
            .orElse(null);
    addToRow("self", rowBuilder, self);
    addToRow("type", rowBuilder, getAllBoards200ResponseValuesInner.getType());
    Long filterId =
        Optional.ofNullable(config.getFilter())
            .map(GetConfiguration200ResponseColumnConfigColumnsInnerStatusesInner::getId)
            .map(id -> id.isEmpty() ? null : Long.parseLong(id))
            .orElse(null);
    addToRow("filter_id", rowBuilder, filterId);
    String subQuery =
        Optional.ofNullable(config.getSubQuery())
            .map(GetConfiguration200ResponseSubQuery::getQuery)
            .orElse(null);
    addToRow("sub_query", rowBuilder, subQuery);
    addToRow("title", rowBuilder, getAllBoards200ResponseValuesInner.getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    return Map.of(
        "id",
        createColumn("id", "The ID of the board.", createLongType()),
        "name",
        createColumn("name", "The name of the board.", createStringType()),
        "self",
        createColumn("self", "The URL of the board details.", createStringType()),
        "type",
        createColumn(
            "type",
            "The board type of the board. Valid values are simple, scrum and kanban.",
            createStringType()),
        "filter_id",
        createColumn("filter_id", "Filter id of the board.", createLongType()),
        "sub_query",
        createColumn(
            "sub_query",
            "JQL subquery used by the given board - (Kanban only).",
            createStringType()),
        "title",
        createColumn("title", TITLE_DESC, createStringType()));
  }
}
