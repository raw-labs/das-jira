package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectComponentsApi;
import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanComponentWithIssueCount;
import com.rawlabs.das.jira.tables.definitions.DASJiraComponentTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraProjectTable;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
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

@DisplayName("DAS Jira Component Table Test")
public class DASJiraComponentTableTest extends BaseMockTest {

  @Mock static ProjectsApi projectsApi;
  @Mock static ProjectComponentsApi projectComponentsApi;

  @InjectMocks DASJiraComponentTable dasJiraComponentTable;

  private static PageBeanComponentWithIssueCount pageBeanComponentWithIssueCount;

  @BeforeAll
  static void beforeAll() throws IOException {
    DASJiraProjectTableTest.beforeAll();
    JsonNode node = loadJson("project-components.json");
    pageBeanComponentWithIssueCount = PageBeanComponentWithIssueCount.fromJson(node.toString());
  }

  @BeforeEach
  void setUp() throws ApiException {
    DASJiraProjectTableTest.setupBeforeEach(projectsApi);
    when(projectComponentsApi.getProjectComponentsPaginated(
            any(), any(), any(), any(), any(), any()))
        .thenReturn(pageBeanComponentWithIssueCount);
  }

  @Test
  @DisplayName("Get project components")
  void testGetProjectComponents() {
    try (var result = dasJiraComponentTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals("10000", extractValueFactory.extractValue(row, "id"));
      assertEquals(10000, extractValueFactory.extractValue(row, "project_id"));
      assertEquals("HSP", extractValueFactory.extractValue(row, "project"));
      assertEquals(
          "5b10a2844c20165700ede21g",
          extractValueFactory.extractValue(row, "real_assignee_account_id"));
      assertTrue(result.hasNext());
      result.next();
      assertTrue(result.hasNext());
      result.next();
      assertTrue(result.hasNext());
      result.next();
      assertFalse(result.hasNext());

    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }
}
