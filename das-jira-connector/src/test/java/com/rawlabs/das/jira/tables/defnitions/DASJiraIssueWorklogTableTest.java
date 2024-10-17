package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueWorklogTable;
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

@DisplayName("DAS Jira Issue Worklog Table Test")
public class DASJiraIssueWorklogTableTest extends BaseMockTest {

  @Mock static IssueWorklogsApi issueWorklogsApi;
  @Mock static IssuesApi issuesApi;

  @InjectMocks DASJiraIssueWorklogTable dasJiraIssueWorklogTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}

  @Test
  @DisplayName("Get issue comments")
  void testGetIssuesForBacklog() {
    try (DASExecuteResult result =
        dasJiraIssueWorklogTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      Row row = result.next();
      assertNotNull(row);
    } catch (IOException e) {
      fail("Should not throw an exception");
    }
  }
}
