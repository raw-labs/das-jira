package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.ModelConfiguration;
import com.rawlabs.das.jira.tables.definitions.DASJiraGlobalSettingTable;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Global Setting Table Test")
public class DASJiraGlobalSettingTableTest extends BaseMockTest {
  @Mock static JiraSettingsApi jiraSettingsApi;

  @InjectMocks DASJiraGlobalSettingTable dasJiraGlobalSettingTable;

  private static ModelConfiguration modelConfiguration;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("global-settings.json");
    modelConfiguration = ModelConfiguration.fromJson(node.toString());
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(jiraSettingsApi.getConfiguration()).thenReturn(modelConfiguration);
  }

  @Test
  @DisplayName("Get global settings")
  void testGetGlobalSettings() throws IOException {
    try (var result = dasJiraGlobalSettingTable.execute(List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals(true, extractValueFactory.extractValue(row, "voting_enabled"));
      assertEquals(true, extractValueFactory.extractValue(row, "watching_enabled"));
      assertEquals(false, extractValueFactory.extractValue(row, "unassigned_issues_allowed"));
      assertEquals(false, extractValueFactory.extractValue(row, "sub_tasks_enabled"));
      assertEquals(true, extractValueFactory.extractValue(row, "issue_linking_enabled"));
      assertEquals(true, extractValueFactory.extractValue(row, "attachments_enabled"));
      ObjectNode timeTrackingConfiguration =
          (ObjectNode) extractValueFactory.extractValue(row, "time_tracking_configuration");
      assertEquals("DAY", timeTrackingConfiguration.get("defaultUnit").asText());
      assertFalse(result.hasNext());
    }
  }
}
