package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.*;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEq;
import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBoardTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_board";

  private BoardApi boardApi;

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
        int callLimit = 1000;
        int maxResults = limit == null ? callLimit : Math.min(callLimit, Math.toIntExact(limit));
        long startAt = 0;
        String type = (String) extractEq(quals, "type");
        String name = (String) extractEq(quals, "name");
        name = name == null ? (String) extractEq(quals, "title") : name;

        String orderBy = null;
        if (sortKeys != null) {
          orderBy =
              sortKeys.stream()
                  .filter(
                      sortKey ->
                          sortKey.getName().equals("name") || sortKey.getName().equals("title"))
                  .findFirst()
                  .map(sortKey -> sortKey.getIsReversed() ? "-name" : "+name")
                  .orElse(null);
        }

        GetAllBoards200Response getAllBoards200Response =
            boardApi.getAllBoards(
                startAt,
                maxResults,
                type,
                name,
                null,
                null,
                null,
                null,
                null,
                orderBy,
                null,
                null,
                null);
        assert getAllBoards200Response.getValues() != null;
        return new DASExecuteResult() {

          private final Iterator<GetAllBoards200ResponseValuesInner> values =
              getAllBoards200Response.getValues().iterator();

          @Override
          public void close() {}

          @Override
          public boolean hasNext() {
            return values.hasNext();
          }

          @Override
          public Row next() {
            GetAllBoards200ResponseValuesInner next = values.next();
            try {
              GetConfiguration200Response config = boardApi.getConfiguration(next.getId());
              return toRow(next, config);
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
      GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner,
      GetConfiguration200Response config) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, getAllBoards200ResponseValuesInner);
    addToRow("name", rowBuilder, getAllBoards200ResponseValuesInner);
    addToRow("self", rowBuilder, getAllBoards200ResponseValuesInner);
    addToRow("type", rowBuilder, getAllBoards200ResponseValuesInner);
    addToRow("filter_id", rowBuilder, config);
    addToRow("sub_query", rowBuilder, config);
    addToRow("title", rowBuilder, getAllBoards200ResponseValuesInner);
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    return Map.of(
        "id",
        createColumn("id", "The ID of the board.", createStringType()),
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
        createColumn("filter_id", "Filter id of the board.", createStringType()),
        "sub_query",
        createColumn(
            "sub_query",
            "JQL subquery used by the given board - (Kanban only).",
            createStringType()),
        "title",
        createColumn("title", TITLE_DESC, createStringType()));
  }
}
