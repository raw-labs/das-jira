package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssueTypesApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueTypeTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Issue Type Table Test")
public class DASJiraIssueTypeTableTest extends BaseMockTest {
  @Mock static IssueTypesApi issueTypesApi;

  @InjectMocks DASJiraIssueTypeTable dasJiraIssueTypeTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
