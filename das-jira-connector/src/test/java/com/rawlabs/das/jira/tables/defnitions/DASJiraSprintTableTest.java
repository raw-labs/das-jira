package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.SprintSearchResult;
import com.rawlabs.das.jira.tables.definitions.DASJiraSprintTable;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Sprint Table Test")
public class DASJiraSprintTableTest extends BaseMockTest {
  @Mock static BoardApi boardApi;

  @InjectMocks DASJiraSprintTable dasJiraSprintTable;

  private static SprintSearchResult sprintSearchResult;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("sprints.json");
    sprintSearchResult = SprintSearchResult.fromJson(node.toString());

    DASJiraBoardTableTest.configBeforeAll();
  }

  @BeforeEach
  void setUp() throws ApiException, com.rawlabs.das.jira.rest.software.ApiException {
    DASJiraBoardTableTest.configBeforeEach(boardApi);
    when(boardApi.getAllSprints(any(), any(), any(), any())).thenReturn(sprintSearchResult);
  }

  @Test
  @DisplayName("Get all sprints")
  void testGetAllSprints() throws IOException {
    try (var result = dasJiraSprintTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals(37, extractValueFactory.extractValue(row, "id"));
      assertEquals("sprint 1", extractValueFactory.extractValue(row, "name"));
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertFalse(result.hasNext());
    }
  }
}
