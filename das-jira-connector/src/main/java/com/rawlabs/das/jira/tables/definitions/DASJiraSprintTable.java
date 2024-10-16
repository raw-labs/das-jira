package com.rawlabs.das.jira.tables.definitions;

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
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
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
import java.util.Optional;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraSprintTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_sprint";

  private DASTable parentTable;

  private BoardApi boardApi = new BoardApi();
  private SprintApi sprintApi = new SprintApi();

  public DASJiraSprintTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "Sprint is a short period in which the development team implements and delivers a discrete and potentially shippable application increment.");
    parentTable = new DASJiraBoardTable(options);
  }

  /** Constructor for mocks */
  DASJiraSprintTable(Map<String, String> options, BoardApi boardApi) {
    this(options);
    this.boardApi = boardApi;
    parentTable = new DASJiraBoardTable(options, boardApi);
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public Row insertRow(Row row) {
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
      return toRow(sprint, sprint.getOriginBoardId());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
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
      return toRow(sprint, sprint.getOriginBoardId());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      sprintApi.deleteSprint((Long) extractValueFactory.extractValue(rowId));
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return new DASJiraWithParentTableResult(parentTable, quals, columns, sortKeys, limit) {
      @Override
      public DASExecuteResult fetchChildResult(Row parentRow) {
        final Long boardId = (Long) extractValueFactory.extractValue(parentRow, "id");
        return new DASJiraPaginatedResult<Sprint>() {

          @Override
          public Row next() {
            return toRow(getNext(), boardId);
          }

          @Override
          public DASJiraPage<Sprint> fetchPage(long offset) {
            try {
              var result =
                  boardApi.getAllSprints(boardId, offset, withMaxResultOrLimit(limit), null);
              return new DASJiraPage<>(result.getValues(), Long.valueOf(result.getTotal()));
            } catch (ApiException e) {
              throw new RuntimeException(e);
            }
          }
        };
      }
    };
  }

  private Row toRow(Sprint sprint, Long boardId) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, sprint.getId());
    addToRow("name", rowBuilder, sprint.getName());
    addToRow("board_id", rowBuilder, boardId);
    addToRow("self", rowBuilder, sprint.getSelf().toString());
    addToRow("state", rowBuilder, sprint.getState());
    Optional.ofNullable(sprint.getStartDate())
        .ifPresent(startDate -> addToRow("start_date", rowBuilder, startDate.toString()));
    Optional.ofNullable(sprint.getEndDate())
        .ifPresent(endDate -> addToRow("end_date", rowBuilder, endDate.toString()));
    Optional.ofNullable(sprint.getCompleteDate())
        .ifPresent(completeDate -> addToRow("complete_date", rowBuilder, completeDate.toString()));
    addToRow("title", rowBuilder, sprint.getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
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
