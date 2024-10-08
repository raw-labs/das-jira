package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraProjectTable;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Project Table Test")
public class DASJiraProjectTableTest extends MockTest {

  @Mock static ProjectsApi api;

  @InjectMocks DASJiraProjectTable dasJiraBoardTable;

  private static final ValueFactory valueFactory = new DefaultValueFactory();
}
