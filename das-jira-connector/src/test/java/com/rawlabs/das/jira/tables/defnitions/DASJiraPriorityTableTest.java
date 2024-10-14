package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssueCommentsApi;
import com.rawlabs.das.jira.rest.platform.api.IssuePrioritiesApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraIssueCommentTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraPriorityTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Issue Table Test")
public class DASJiraPriorityTableTest extends BaseMockTest {
    @Mock
    static IssuePrioritiesApi issuePrioritiesApi;

    @InjectMocks
    DASJiraPriorityTable dasJiraPriorityTable;

    @BeforeAll
    static void beforeAll() {}

    @BeforeEach
    void setUp() {}
}
