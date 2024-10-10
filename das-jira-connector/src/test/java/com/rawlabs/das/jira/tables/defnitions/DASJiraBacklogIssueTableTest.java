package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.software.ApiException;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.SearchResults;
import com.rawlabs.das.jira.tables.definitions.DASJiraBacklogIssueTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        dasJiraBacklogIssueTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
      assertEquals("10001", row.getDataMap().get("id").getString().getV());
      assertEquals(84L, row.getDataMap().get("board_id").getLong().getV());
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertEquals("10001", row.getDataMap().get("id").getString().getV());
      assertEquals(92L, row.getDataMap().get("board_id").getLong().getV());
      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Should not throw an exception");
    }
  }
}
