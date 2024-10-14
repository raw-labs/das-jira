package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssueWorklogsApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueWorklogTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Issue Worklog Table Test")
public class DASJiraIssueWorklogTableTest extends BaseMockTest {
  @Mock static IssueWorklogsApi issueWorklogsApi;

  @InjectMocks DASJiraIssueWorklogTable dasJiraIssueWorklogTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
