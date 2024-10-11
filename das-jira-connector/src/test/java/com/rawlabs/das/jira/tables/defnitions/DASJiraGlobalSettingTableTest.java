package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.software.api.EpicApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraEpicTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraGlobalSettingTable;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Global Setting Table Test")
public class DASJiraGlobalSettingTableTest extends BaseMockTest {
    @Mock
    static JiraSettingsApi jiraSettingsApi;

    @InjectMocks
    DASJiraGlobalSettingTable dasJiraGlobalSettingTable;
}
