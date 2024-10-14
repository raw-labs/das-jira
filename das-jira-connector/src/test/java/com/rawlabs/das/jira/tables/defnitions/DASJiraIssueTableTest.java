package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Issue Table Test")
public class DASJiraIssueTableTest extends BaseMockTest {
  @Mock static IssuesApi issuesApi;

  @InjectMocks DASJiraIssueTable dasJiraIssueTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
