package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.software.api.EpicApi;
import com.rawlabs.das.jira.rest.software.model.EpicSearchResult;
import com.rawlabs.das.jira.tables.definitions.DASJiraEpicTable;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Epic Table Test")
public class DASJiraEpicTableTest extends BaseMockTest {

  @Mock static EpicApi epicApi;

  @InjectMocks DASJiraEpicTable dasJiraEpicTable;

  private static EpicSearchResult epicsResult;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("epics.json");
    epicsResult = EpicSearchResult.fromJson(node.toString());
  }

  @BeforeEach
  void setUp() throws ApiException, com.rawlabs.das.jira.rest.software.ApiException {
    when(epicApi.searchPaginatedEpics(any(), any())).thenReturn(epicsResult);
  }

  @Test
  @DisplayName("Get all epics")
  void testGetAllEpics() throws IOException {
    try (var result = dasJiraEpicTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals(20890, extractValueFactory.extractValue(row, "id"));
      assertEquals("[Ph.2] Client-focused", extractValueFactory.extractValue(row, "name"));
      assertEquals("color_7", extractValueFactory.extractValue(row, "color"));

      assertFalse(result.hasNext());
    }
  }
}
