package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueCommentsApi;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.rest.platform.model.PageOfComments;
import com.rawlabs.das.jira.rest.platform.model.PageOfWorklogs;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueCommentTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Issue Comment Table Test")
public class DASJiraIssueCommentTableTest extends BaseMockTest {

  @Mock static IssueCommentsApi issueCommentsApi;
  @Mock static IssueSearchApi issueSearchApi;

  @InjectMocks DASJiraIssueCommentTable dasJiraIssueCommentTable;

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
    when(issueCommentsApi.getComments(any(), any(), any(), any(), any()))
        .thenReturn(pageOfComments);
  }

  @Test
  @DisplayName("Get issue comments")
  void testGetIssuesForBacklog() {
    try (DASExecuteResult result =
        dasJiraIssueCommentTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
    } catch (IOException e) {
      fail("Should not throw an exception");
    }
  }
}
