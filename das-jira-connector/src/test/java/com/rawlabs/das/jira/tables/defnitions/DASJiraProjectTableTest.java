package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanProject;
import com.rawlabs.das.jira.tables.definitions.DASJiraProjectTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
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

@DisplayName("DAS Jira Project Table Test")
public class DASJiraProjectTableTest extends BaseMockTest {

  @Mock static ProjectsApi api;

  @InjectMocks DASJiraProjectTable dasJiraProjectTable;

  private static final ValueFactory valueFactory = new DefaultValueFactory();
  private static final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();

  private static PageBeanProject searchResults;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("projects.json");
    searchResults = PageBeanProject.fromJson(node.toString());
  }

  @BeforeEach
  void setUp() throws ApiException, com.rawlabs.das.jira.rest.platform.ApiException {
    when(api.searchProjects(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
            any()))
        .thenReturn(searchResults);
  }

  @Test
  @DisplayName("Search projects")
  void testSearchProjects() {
    try (DASExecuteResult result = dasJiraProjectTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
      assertEquals("10000", extractValueFactory.extractValue(row, "id"));
      assertEquals("EX", extractValueFactory.extractValue(row, "key"));
      assertEquals("Example", row.getDataMap().get("name").getString().getV());
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertEquals("10001", extractValueFactory.extractValue(row, "id"));
      assertEquals("ABC", extractValueFactory.extractValue(row, "key"));
      assertEquals("Alphabetical", row.getDataMap().get("name").getString().getV());
    } catch (IOException e) {
      fail("Failed to execute table", e);
    }
  }
}
