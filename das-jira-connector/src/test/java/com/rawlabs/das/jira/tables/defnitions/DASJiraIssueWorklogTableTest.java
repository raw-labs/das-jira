package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
import com.rawlabs.das.jira.rest.platform.model.PageOfWorklogs;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueWorklogTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.tables.Row;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.time.ZoneId;
import java.util.Map;

@DisplayName("DAS Jira Issue Worklog Table Test")
public class DASJiraIssueWorklogTableTest extends BaseMockTest {

  @Mock static IssueWorklogsApi issueWorklogsApi;
  @Mock static IssueSearchApi issueSearchApi;

  DASJiraIssueWorklogTable dasJiraIssueWorklogTable;

  private static PageOfWorklogs pageOfWorklogs;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("issues-worklog.json");
    pageOfWorklogs = PageOfWorklogs.fromJson(node.toString());
    DASJiraIssueTableTest.configBeforeAll();
  }

  @BeforeEach
  void setUp() throws ApiException {
    DASJiraIssueTableTest.configBeforeEach(issueSearchApi);
    dasJiraIssueWorklogTable =
        new DASJiraIssueWorklogTable(
            Map.of("timezone", "UTC"), // The options
            ZoneId.of("UTC"), // The jiraZoneId
            issueWorklogsApi,
            issueSearchApi,
            null // issuesApi
            );
    when(issueWorklogsApi.getIssueWorklog(any(), any(), any(), any(), any(), any()))
        .thenReturn(pageOfWorklogs);
  }

  @Test
  @DisplayName("Get issue worklog")
  void testIssueWorklog() {
    try (DASExecuteResult result =
        dasJiraIssueWorklogTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
    } catch (IOException e) {
      fail("Should not throw an exception");
    }
  }
}
