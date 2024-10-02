package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.*;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEq;
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

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    return sortKeys.stream()
        .filter(sortKey -> sortKey.getName().equals("name") || sortKey.getName().equals("title"))
        .toList();
  }

  public final DASJiraTableDefinition<GetAllBoards200ResponseValuesInner> dasJiraTableDefinition =
      new DASJiraTableDefinition<>(
          TABLE_NAME,
          "A board displays issues from one or more projects, giving you a flexible way of viewing, managing, and reporting on work in progress.",
          List.of(
              new DASJiraParentColumnDefinition<>(
                  "id",
                  "The ID of the board.",
                  createIntType(),
                  GetAllBoards200ResponseValuesInner::getId,
                  new DASJiraTableDefinition<>(
                      "board_config",
                      "configuration of the board",
                      List.of(
                          new DASJiraNormalColumnDefinition<>(
                              "type",
                              "The board type of the board. Valid values are simple, scrum and kanban.",
                              createStringType(),
                              GetConfiguration200Response::getType),
                          new DASJiraNormalColumnDefinition<>(
                              "filter_id",
                              "Filter id of the board.",
                              createStringType(),
                              (GetConfiguration200Response configuration) -> {
                                GetConfiguration200ResponseColumnConfigColumnsInnerStatusesInner
                                    filter = configuration.getFilter();
                                return filter != null ? filter.getId() : null;
                              }),
                          new DASJiraNormalColumnDefinition<>(
                              "sub_query",
                              "JQL subquery used by the given board - (Kanban only).",
                              createStringType(),
                              (GetConfiguration200Response configuration) -> {
                                GetConfiguration200ResponseSubQuery subQuery =
                                    configuration.getSubQuery();
                                return subQuery != null ? subQuery.getQuery() : null;
                              })),
                      (quals, _, _, _) -> {
                        try {
                          assert quals != null;
                          long id = (Integer) extractEq(quals, "id");
                          GetConfiguration200Response config = boardApi.getConfiguration(id);
                          return List.of(config).iterator();
                        } catch (ApiException e) {
                          throw new DASSdkException("Failed to fetch advanced settings", e);
                        }
                      })),
              new DASJiraNormalColumnDefinition<>(
                  "name",
                  "The name of the board.",
                  createStringType(),
                  GetAllBoards200ResponseValuesInner::getName),
              new DASJiraNormalColumnDefinition<>(
                  "self",
                  "The URL of the board details.",
                  createStringType(),
                  GetAllBoards200ResponseValuesInner::getSelf),
              new DASJiraNormalColumnDefinition<>(
                  "type",
                  "The board type of the board. Valid values are simple, scrum and kanban.",
                  createStringType(),
                  GetAllBoards200ResponseValuesInner::getType),
              new DASJiraNormalColumnDefinition<>(
                  "title",
                  TITLE_DESC,
                  createStringType(),
                  GetAllBoards200ResponseValuesInner::getName)),
          (quals, columns, sortKeys, limit) -> {
            try {
              Long id = (Long) extractEq(quals, "id");

              if (id != null) {
                GetAllBoards200ResponseValuesInner getAllBoards200ResponseValuesInner =
                    boardApi.getBoard(id);
                return List.of(getAllBoards200ResponseValuesInner).iterator();
              } else {
                int callLimit = 1000;
                int maxResults =
                    limit == null ? callLimit : Math.min(callLimit, Math.toIntExact(limit));
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
                                  sortKey.getName().equals("name")
                                      || sortKey.getName().equals("title"))
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
                List<GetAllBoards200ResponseValuesInner> values =
                    getAllBoards200Response.getValues();
                return values == null ? Collections.emptyIterator() : values.iterator();
              }
            } catch (ApiException e) {
              throw new DASSdkException("Failed to fetch advanced settings", e);
            }
          });
}
