package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.software.api.EpicApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraEpicTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

@DisplayName("DAS Jira Epic Table Test")
public class DASJiraEpicTableTest extends BaseMockTest {

  @Mock static EpicApi epicApi;

  @InjectMocks DASJiraEpicTable dasJiraEpicTable;

  @BeforeAll
  static void beforeAll() {}

  @BeforeEach
  void setUp() {}
}
