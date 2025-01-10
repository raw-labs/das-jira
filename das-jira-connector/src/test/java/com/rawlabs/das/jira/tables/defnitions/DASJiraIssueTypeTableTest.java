package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueTypesApi;
import com.rawlabs.das.jira.rest.platform.model.IssueTypeDetails;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueTypeTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Issue Type Table Test")
public class DASJiraIssueTypeTableTest extends BaseMockTest {
  @Mock static IssueTypesApi issueTypesApi;

  @InjectMocks DASJiraIssueTypeTable dasJiraIssueTypeTable;

  private static List<IssueTypeDetails> issueTypeDetails;

  @BeforeAll
  static void beforeAll() throws IOException {
    ArrayNode node = (ArrayNode) loadJson("issue-types.json");

    issueTypeDetails = new ArrayList<>();
    for (JsonNode jsonNode : node) {
      issueTypeDetails.add(IssueTypeDetails.fromJson(jsonNode.toString()));
    }
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(issueTypesApi.getIssueAllTypes()).thenReturn(issueTypeDetails);
  }

  @Test
  @DisplayName("Get issue types")
  void testGetIssueTypes() throws IOException {
    try (var result = dasJiraIssueTypeTable.execute(List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals("3", extractValueFactory.extractValue(row, "id"));
      assertEquals("Task", extractValueFactory.extractValue(row, "name"));
      assertEquals(
          "A task that needs to be done.", extractValueFactory.extractValue(row, "description"));
      assertEquals(0, extractValueFactory.extractValue(row, "hierarchy_level"));
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertFalse(result.hasNext());
    }
  }
}
