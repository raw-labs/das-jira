package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanComponentWithIssueCount;
import com.rawlabs.das.jira.rest.platform.model.PageOfDashboards;
import com.rawlabs.das.jira.tables.definitions.DASJiraDashboardTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Dashboard Table Test")
public class DASJiraDashboardTableTest extends BaseMockTest {

  @Mock static DashboardsApi dashboardsApi;

  @InjectMocks DASJiraDashboardTable dasJiraDashboardTable;

  private static PageOfDashboards pageOfDashboards;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("dashboards.json");
    pageOfDashboards = PageOfDashboards.fromJson(node.toString());
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(dashboardsApi.getAllDashboards(any(), any(), any())).thenReturn(pageOfDashboards);
  }

  @Test
  @DisplayName("Get all dashboards")
  void testGetAllDashboards() throws IOException {
    try (var result = dasJiraDashboardTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals("10000", extractValueFactory.extractValue(row, "id"));
      assertEquals("System Dashboard", extractValueFactory.extractValue(row, "name"));
      assertInstanceOf(ArrayList.class, extractValueFactory.extractValue(row, "share_permissions"));
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertEquals("20000", extractValueFactory.extractValue(row, "id"));
      assertFalse(result.hasNext());
    }
  }
}
