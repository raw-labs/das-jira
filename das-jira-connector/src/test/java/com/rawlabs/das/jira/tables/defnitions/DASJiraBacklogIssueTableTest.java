package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.SearchResults;
import com.rawlabs.das.jira.tables.definitions.DASJiraBacklogIssueTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

@DisplayName("DAS Jira Backlog Issue Table Test")
public class DASJiraBacklogIssueTableTest extends MockTest {
  @Mock static BoardApi api;

  @InjectMocks DASJiraBacklogIssueTable dasJiraBacklogIssueTable;

  private static SearchResults searchResults;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("issue-search-result.json");
    searchResults = SearchResults.fromJson(node.toString());
  }

  @Test
  @DisplayName("Test execute")
  void testExecute() {}
}
