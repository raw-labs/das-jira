package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.software.api.SprintApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraSprintTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Sprint Table Test")
public class DASJiraSprintTableTest extends BaseMockTest {
  @Mock static SprintApi sprintApi;

  @InjectMocks DASJiraSprintTable dasJiraSprintTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
