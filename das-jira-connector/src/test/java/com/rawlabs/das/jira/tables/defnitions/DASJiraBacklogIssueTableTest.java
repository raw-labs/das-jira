package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.SearchResults;
import com.rawlabs.das.jira.tables.definitions.DASJiraBacklogIssueTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.tables.Row;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Backlog Issue Table Test")
public class DASJiraBacklogIssueTableTest extends BaseMockTest {

  @Mock static BoardApi boardApi;

  @InjectMocks DASJiraBacklogIssueTable dasJiraBacklogIssueTable;

  private static SearchResults searchResults;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("backlog-issues.json");
    searchResults = SearchResults.fromJson(node.toString());

    DASJiraBoardTableTest.configBeforeAll();
  }

  @BeforeEach
  void setUp() throws ApiException {
    DASJiraBoardTableTest.configBeforeEach(boardApi);
    when(boardApi.getIssuesForBacklog(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(searchResults);
  }

  @Test
  @DisplayName("Get issues for backlog")
  void testGetIssuesForBacklog() {
    try (DASExecuteResult result =
        dasJiraBacklogIssueTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
      assertEquals("10001", extractValueFactory.extractValue(row, "id"));
      assertEquals(84L, extractValueFactory.extractValue(row, "board_id"));
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertEquals("10001", extractValueFactory.extractValue(row, "id"));
      assertEquals(92L, extractValueFactory.extractValue(row, "board_id"));
      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Should not throw an exception");
    }
  }
}
