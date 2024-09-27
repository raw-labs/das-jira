package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraBacklogIssueTable;
import com.rawlabs.das.rest.jira.api.JiraSettingsApi;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Backlog Issue Table Test")
public class DASJiraBacklogIssueTableTest extends MockTest {
    @Mock
    static JiraSettingsApi api;

    @InjectMocks
    DASJiraBacklogIssueTable dasJiraBacklogIssueTable;
}
