package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.rest.platform.model.SearchResults;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Issue Table Test")
public class DASJiraIssueTableTest extends BaseMockTest {
  @Mock private IssueSearchApi issueSearchApi;

  private static DASJiraIssueTable dasJiraIssueTable;

  private static SearchResults searchResults;

  public static void configBeforeAll() throws IOException {
    JsonNode node = loadJson("issues.json");
    searchResults = SearchResults.fromJson(node.toString());
  }

  @BeforeAll
  static void beforeAll() throws IOException {
    configBeforeAll();
  }

  public static void configBeforeEach(IssueSearchApi issueSearchApi) throws ApiException {
    when(issueSearchApi.searchForIssuesUsingJql(
            any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(searchResults);
  }

  @BeforeEach
  void setUp() throws ApiException {
    configBeforeEach(issueSearchApi);
    dasJiraIssueTable =
        new DASJiraIssueTable(
            Map.of("timezone", "UTC"), // The options
            ZoneId.of("UTC"), // The jiraZoneId (whatever you need in test)
            issueSearchApi,
            null // issuesApi
            );
  }

  @DisplayName("Get issues")
  @Test
  void testGetIssues() throws IOException {
    try (var result = dasJiraIssueTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals("33060", extractValueFactory.extractValue(row, "id"));
      assertEquals("SP-1", extractValueFactory.extractValue(row, "key"));
      assertEquals("Task", extractValueFactory.extractValue(row, "type"));
      assertEquals("test issue", extractValueFactory.extractValue(row, "summary"));
      assertEquals("RD-15048", extractValueFactory.extractValue(row, "epic_key"));
      ObjectNode description = (ObjectNode) extractValueFactory.extractValue(row, "description");
      // Fetch the first text of the content ("do this")
      assertEquals(
          "do this", description.get("content").get(0).get("content").get(0).get("text").asText());
      ArrayList<String> components =
          (ArrayList<String>) extractValueFactory.extractValue(row, "components");
      assertEquals("10151", components.get(0));
      assertFalse(result.hasNext());
    }
  }
}
