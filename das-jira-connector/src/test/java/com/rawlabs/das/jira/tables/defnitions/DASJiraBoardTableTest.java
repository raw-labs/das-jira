package com.rawlabs.das.jira.tables.defnitions;

import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.createEq;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createLongType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200Response;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.software.model.GetConfiguration200Response;
import com.rawlabs.das.jira.tables.definitions.DASJiraBoardTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.v1.tables.Column;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Board Table Test")
public class DASJiraBoardTableTest extends BaseMockTest {
  @Mock static BoardApi boardApi;

  @InjectMocks DASJiraBoardTable dasJiraBoardTable;

  private static final ValueFactory valueFactory = new DefaultValueFactory();

  public static GetAllBoards200Response allBoards;
  public static GetAllBoards200ResponseValuesInner oneBoard;
  public static GetConfiguration200Response boardConfig;

  public static void configBeforeAll() throws IOException {
    JsonNode allBoardsJson = loadJson("all-boards.json");
    allBoards = GetAllBoards200Response.fromJson(allBoardsJson.toString());

    JsonNode oneBoardJson = loadJson("one-board.json");
    oneBoard = GetAllBoards200ResponseValuesInner.fromJson(oneBoardJson.toString());

    JsonNode boardConfigJson = loadJson("board-config.json");
    boardConfig = GetConfiguration200Response.fromJson(boardConfigJson.toString());
  }

  public static void configBeforeEach(BoardApi api) {
    try {
      when(api.getBoard(84L)).thenReturn(oneBoard);
      when(api.getAllBoards(
              any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
              any()))
          .thenReturn(allBoards);
      when(api.getConfiguration(anyLong())).thenReturn(boardConfig);
      when(api.getBoard(1L)).thenThrow(new ApiException("couldn't fetch data"));
    } catch (ApiException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @BeforeAll
  static void beforeAll() throws IOException {
    configBeforeAll();
  }

  @BeforeEach
  void setUp() {
    configBeforeEach(boardApi);
  }

  @Test
  @DisplayName("Get one board")
  void testGetBoard() {
    try (DASExecuteResult result =
        dasJiraBoardTable.execute(
            List.of(
                createEq(
                    valueFactory.createValue(new ValueTypeTuple(84L, createLongType())), "id")),
            List.of(),
            List.of(),
            null)) {
      assertTrue(result.hasNext());
      Row row = result.next();
      assertEquals(84L, getByKey(row, "id").getLong().getV());
      assertEquals("scrum board", getByKey(row, "name").getString().getV());
      assertEquals(
          "https://your-domain.atlassian.net/rest/agile/1.0/board/84",
          getByKey(row, "self").getString().getV());
      assertEquals(1001L, getByKey(row, "filter_id").getLong().getV());
      assertEquals("scrum", getByKey(row, "type").getString().getV());
      assertEquals("", getByKey(row, "sub_query").getString().getV());
      assertFalse(result.hasNext());

    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get all boards")
  void testGetBoards() {
    try (DASExecuteResult result = dasJiraBoardTable.execute(List.of(), null, List.of(), null)) {
      assertTrue(result.hasNext());
      result.next();
      assertTrue(result.hasNext());
      Row row = result.next();
      assertEquals(92L, getByKey(row, "id").getLong().getV());
      assertEquals("kanban board", getByKey(row, "name").getString().getV());
      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get with error")
  void testGetWithError() {
    assertThrows(
        DASSdkException.class,
        () -> {
          try (DASExecuteResult result =
              dasJiraBoardTable.execute(
                  List.of(
                      createEq(
                          valueFactory.createValue(new ValueTypeTuple(1L, createLongType())),
                          "id")),
                  null,
                  List.of(),
                  null)) {
            fail("Exception expected");
          }
        });
  }

  private Value getByKey(Row row, String key) {
    return row.getColumnsList().stream()
        .filter(c -> c.getName().equals(key))
        .map(Column::getData)
        .findFirst()
        .get();
  }
}
