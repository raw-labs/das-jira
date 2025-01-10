package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.WorkflowsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanWorkflow;
import com.rawlabs.das.jira.tables.definitions.DASJiraWorkflowTable;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Workflow Table Test")
public class DASJiraWorkflowTableTest extends BaseMockTest {
  @Mock static WorkflowsApi workflowsApi;

  @InjectMocks DASJiraWorkflowTable dasJiraWorkflowTable;

  private static PageBeanWorkflow workflows;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("workflows.json");
    workflows = PageBeanWorkflow.fromJson(node.toString());
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(workflowsApi.getWorkflowsPaginated(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(workflows);
  }

  @Test
  @DisplayName("Get workflows")
  void testGetWorkflows() throws IOException {
    try (var result = dasJiraWorkflowTable.execute(List.of(), List.of(), List.of())) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals(
          "5ed312c5-f7a6-4a78-a1f6-8ff7f307d063",
          extractValueFactory.extractValue(row, "entity_id"));
      assertEquals("SCRUM Workflow", extractValueFactory.extractValue(row, "name"));
      assertFalse(result.hasNext());
    }
  }
}
