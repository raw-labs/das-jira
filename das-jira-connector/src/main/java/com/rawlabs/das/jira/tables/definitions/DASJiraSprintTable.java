package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.api.SprintApi;
import com.rawlabs.das.jira.rest.software.model.CreateSprintRequest;
import com.rawlabs.das.jira.rest.software.model.Sprint;
import com.rawlabs.das.jira.rest.software.model.UpdateSprintRequest;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.jira.tables.results.DASJiraWithParentTableResult;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraSprintTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_sprint";

  private final DASJiraTable parentTable;

  private final BoardApi boardApi;
  private final SprintApi sprintApi;

  public DASJiraSprintTable(Map<String, String> options, SprintApi sprintApi, BoardApi boardApi) {
    super(
        options,
        TABLE_NAME,
        "Sprint is a short period in which the development team implements and delivers a discrete and potentially shippable application increment.");
    this.sprintApi = sprintApi;
    this.boardApi = boardApi;
    this.parentTable = new DASJiraBoardTable(options, boardApi);
  }

  public String uniqueColumn() {
    return "id";
  }

  @Override
  public Row insert(Row row) {
    try {
      CreateSprintRequest createSprintRequest = new CreateSprintRequest();
      createSprintRequest.setEndDate((String) extractValueFactory.extractValue(row, "end_date"));
      createSprintRequest.setName((String) extractValueFactory.extractValue(row, "name"));
      createSprintRequest.setStartDate(
          (String) extractValueFactory.extractValue(row, "start_date"));
      createSprintRequest.setOriginBoardId(
          (Long) extractValueFactory.extractValue(row, "board_id"));
      createSprintRequest.setGoal((String) extractValueFactory.extractValue(row, "goal"));
      Sprint sprint = sprintApi.createSprint(createSprintRequest);
      return toRow(sprint, sprint.getOriginBoardId(), List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  @Override
  public Row update(Value rowId, Row newValues) {
    try {
      UpdateSprintRequest updateSprintRequest = new UpdateSprintRequest();
      updateSprintRequest.setEndDate(
          (String) extractValueFactory.extractValue(newValues, "end_date"));
      updateSprintRequest.setName((String) extractValueFactory.extractValue(newValues, "name"));
      updateSprintRequest.setStartDate(
          (String) extractValueFactory.extractValue(newValues, "start_date"));
      updateSprintRequest.setOriginBoardId(
          (Long) extractValueFactory.extractValue(newValues, "board_id"));
      updateSprintRequest.setGoal((String) extractValueFactory.extractValue(newValues, "goal"));
      Sprint sprint =
          sprintApi.updateSprint(
              (Long) extractValueFactory.extractValue(rowId), updateSprintRequest);
      return toRow(sprint, sprint.getOriginBoardId(), List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  @Override
  public void delete(Value rowId) {
    try {
      sprintApi.deleteSprint((Long) extractValueFactory.extractValue(rowId));
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    return new DASJiraWithParentTableResult(parentTable, quals, columns, sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        final Long boardId = (Long) extractValueFactory.extractValue(parentRow, "id");
        return new DASJiraPaginatedResult<Sprint>(limit) {

          @Override
          public Row next() {
            return toRow(getNext(), boardId, columns);
          }

          @Override
          public DASJiraPage<Sprint> fetchPage(long offset) {
            try {
              var result =
                  boardApi.getAllSprints(boardId, offset, withMaxResultOrLimit(limit), null);
              return new DASJiraPage<>(result.getValues(), result.getTotal());
            } catch (ApiException e) {
              throw makeSdkException(e);
            }
          }
        };
      }
    };
  }

  private Row toRow(Sprint sprint, Long boardId, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("id", rowBuilder, sprint.getId(), columns);
    addToRow("name", rowBuilder, sprint.getName(), columns);
    addToRow("board_id", rowBuilder, boardId, columns);

    var self = Optional.ofNullable(sprint.getSelf()).map(Object::toString).orElse(null);
    addToRow("self", rowBuilder, self, columns);
    addToRow("state", rowBuilder, sprint.getState(), columns);

    var startDate = Optional.ofNullable(sprint.getStartDate()).map(Object::toString).orElse(null);
    addToRow("start_date", rowBuilder, startDate, columns);

    var endDate = Optional.ofNullable(sprint.getEndDate()).map(Object::toString).orElse(null);
    addToRow("end_date", rowBuilder, endDate, columns);

    var completeDate =
        Optional.ofNullable(sprint.getCompleteDate()).map(Object::toString).orElse(null);
    addToRow("complete_date", rowBuilder, completeDate, columns);

    addToRow("title", rowBuilder, sprint.getName(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
    columns.put("id", createColumn("id", "The ID of the sprint.", createIntType()));
    columns.put("name", createColumn("name", "The name of the sprint.", createStringType()));
    columns.put(
        "board_id",
        createColumn("board_id", "The ID of the board the sprint belongs to.z", createLongType()));
    columns.put("self", createColumn("self", "The URL of the sprint details.", createStringType()));
    columns.put("state", createColumn("state", "Status of the sprint.", createStringType()));
    columns.put(
        "start_date",
        createColumn("start_date", "The start timestamp of the sprint.", createTimestampType()));
    columns.put(
        "end_date",
        createColumn(
            "end_date", "The projected time of completion of the sprint.", createTimestampType()));
    columns.put(
        "complete_date",
        createColumn(
            "complete_date", "Date the sprint was marked as complete.", createTimestampType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
