package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200Response;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.software.model.GetConfiguration200Response;
import com.rawlabs.das.jira.tables.definitions.DASJiraBoardTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.createEq;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createLongType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

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
            null,
            null,
            null)) {
      assertTrue(result.hasNext());
      Row row = result.next();
      assertTrue(row.getDataMap().containsKey("id"));
      assertEquals(84L, row.getDataMap().get("id").getLong().getV());
      assertTrue(row.getDataMap().containsKey("name"));
      assertEquals("scrum board", row.getDataMap().get("name").getString().getV());
      assertTrue(row.getDataMap().containsKey("self"));
      assertEquals(
          "https://your-domain.atlassian.net/rest/agile/1.0/board/84",
          row.getDataMap().get("self").getString().getV());
      assertTrue(row.getDataMap().containsKey("filter_id"));
      assertEquals(1001L, row.getDataMap().get("filter_id").getLong().getV());
      assertTrue(row.getDataMap().containsKey("type"));
      assertEquals("scrum", row.getDataMap().get("type").getString().getV());
      assertTrue(row.getDataMap().containsKey("sub_query"));
      assertEquals("", row.getDataMap().get("sub_query").getString().getV());
      assertFalse(result.hasNext());

    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get all boards")
  void testGetBoards() {
    try (DASExecuteResult result = dasJiraBoardTable.execute(List.of(), null, null, null)) {
      assertTrue(result.hasNext());
      result.next();
      assertTrue(result.hasNext());
      Row row = result.next();
      assertTrue(row.getDataMap().containsKey("id"));
      assertEquals(92L, row.getDataMap().get("id").getLong().getV());
      assertTrue(row.getDataMap().containsKey("name"));
      assertEquals("kanban board", row.getDataMap().get("name").getString().getV());
      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get with error")
  void testGetWithError() {
    assertThrows(
        DASSdkApiException.class,
        () -> {
          try (DASExecuteResult result =
              dasJiraBoardTable.execute(
                  List.of(
                      createEq(
                          valueFactory.createValue(new ValueTypeTuple(1L, createLongType())),
                          "id")),
                  null,
                  null,
                  null)) {
            fail("Exception expected");
          }
        });
  }
}
