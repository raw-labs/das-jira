package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraEpicTable;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Group Table")
public class DASJiraGroupTableTest extends BaseMockTest {
  @Mock static GroupsApi groupsApi;

  @InjectMocks DASJiraEpicTable dasJiraEpicTable;
}
