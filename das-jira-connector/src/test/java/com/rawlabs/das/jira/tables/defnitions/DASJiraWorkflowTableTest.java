package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.WorkflowsApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraWorkflowTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Workflow Table Test")
public class DASJiraWorkflowTableTest extends BaseMockTest {
  @Mock static WorkflowsApi workflowsApi;

  @InjectMocks DASJiraWorkflowTable dasJiraWorkflowTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
