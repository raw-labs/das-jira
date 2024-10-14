package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.IssuePrioritiesApi;
import com.rawlabs.das.jira.rest.platform.api.UsersApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraPriorityTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraUserTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira User Table Test")
public class DASJiraUserTableTest extends BaseMockTest {
  @Mock static UsersApi usersApi;

  @InjectMocks DASJiraUserTable dasJiraUserTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
