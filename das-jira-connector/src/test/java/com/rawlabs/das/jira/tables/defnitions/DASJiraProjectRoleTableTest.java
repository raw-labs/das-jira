package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssuePrioritiesApi;
import com.rawlabs.das.jira.rest.platform.api.ProjectRolesApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraPriorityTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraProjectRoleTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Project Role Table Test")
public class DASJiraProjectRoleTableTest extends BaseMockTest {
    @Mock
    static ProjectRolesApi projectRolesApi;

    @InjectMocks
    DASJiraProjectRoleTable dasJiraProjectRoleTable;

    @BeforeAll
    static void beforeAll() {}

    @BeforeEach
    void setUp() {}
}
