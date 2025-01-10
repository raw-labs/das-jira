package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanProject;
import com.rawlabs.das.jira.tables.definitions.DASJiraProjectTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ExtractValueFactory;
import com.rawlabs.protocol.das.v1.tables.Row;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Project Table Test")
public class DASJiraProjectTableTest extends BaseMockTest {

  @Mock static ProjectsApi projectsApi;

  @InjectMocks DASJiraProjectTable dasJiraProjectTable;

  private static final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();

  private static PageBeanProject searchResults;

  public static void setupBeforeAll() throws IOException {
    JsonNode node = loadJson("projects.json");
    searchResults = PageBeanProject.fromJson(node.toString());
  }

  @BeforeAll
  static void beforeAll() throws IOException {
    setupBeforeAll();
  }

  static void setupBeforeEach(ProjectsApi api) throws ApiException {
    when(api.searchProjects(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
            any()))
        .thenReturn(searchResults);
  }

  @BeforeEach
  void setUp() throws ApiException, com.rawlabs.das.jira.rest.platform.ApiException {
    setupBeforeEach(projectsApi);
  }

  @Test
  @DisplayName("Search projects")
  void testSearchProjects() {
    try (DASExecuteResult result = dasJiraProjectTable.execute(List.of(), List.of(), List.of())) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
      assertEquals("10000", extractValueFactory.extractValue(row, "id"));
      assertEquals("EX", extractValueFactory.extractValue(row, "key"));
      assertEquals("Example", extractValueFactory.extractValue(row, "name"));
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertEquals("10001", extractValueFactory.extractValue(row, "id"));
      assertEquals("ABC", extractValueFactory.extractValue(row, "key"));
      assertEquals("Alphabetical", extractValueFactory.extractValue(row, "name"));
    } catch (IOException e) {
      fail("Failed to execute table", e);
    }
  }
}
