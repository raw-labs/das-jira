package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueCommentsApi;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.model.PageOfComments;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueCommentTable;
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
@DisplayName("DAS Jira Issue Comment Table Test")
public class DASJiraIssueCommentTableTest extends BaseMockTest {

  @Mock static IssueCommentsApi issueCommentsApi;
  @Mock static IssueSearchApi issueSearchApi;

  private DASJiraIssueCommentTable dasJiraIssueCommentTable;

  private static PageOfComments pageOfComments;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("issue-comments.json");
    pageOfComments = PageOfComments.fromJson(node.toString());
    DASJiraIssueTableTest.configBeforeAll();
  }

  @BeforeEach
  void setUp() throws ApiException {
    DASJiraIssueTableTest.configBeforeEach(issueSearchApi);
    dasJiraIssueCommentTable =
        new DASJiraIssueCommentTable(
            Map.of("timezone", "UTC"), // The options
            ZoneId.of("UTC"), // The jiraZoneId
            issueCommentsApi,
            issueSearchApi,
            null // issuesApi
            );
    when(issueCommentsApi.getComments(any(), any(), any(), any(), any()))
        .thenReturn(pageOfComments);
  }

  @Test
  @DisplayName("Get issue comments")
  void testGetIssuesForBacklog() {
    try (DASExecuteResult result =
        dasJiraIssueCommentTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
    } catch (IOException e) {
      fail("Should not throw an exception");
    }
  }
}
