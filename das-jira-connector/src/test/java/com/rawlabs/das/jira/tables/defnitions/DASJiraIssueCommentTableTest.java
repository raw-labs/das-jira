package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssueCommentsApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueCommentTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Issue Comment Table Test")
public class DASJiraIssueCommentTableTest extends BaseMockTest {

  @Mock static IssueCommentsApi issueCommentsApi;

  @InjectMocks DASJiraIssueCommentTable dasJiraIssueCommentTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
