package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.rest.jira.ApiException;
import com.rawlabs.das.rest.jira.api.JiraSettingsApi;
import com.rawlabs.das.rest.jira.model.ApplicationProperty;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Advanced Settings Table Test")
@ExtendWith(MockitoExtension.class)
public class DASJiraAdvancedSettingsTableTest {

  @Mock static JiraSettingsApi api;

  @InjectMocks DASJiraAdvancedSettingsTable dasJiraAdvancedSettingsTable;

  private static List<ApplicationProperty> appProps;

  @BeforeAll
  static void beforeAll() {
    ApplicationProperty appProp1 = new ApplicationProperty();
    appProp1.setId("jira.home");
    appProp1.setKey("jira.home");
    appProp1.setName("jira.home");
    appProp1.setDesc("Jira home directory");
    appProp1.setType("string");
    appProp1.setValue("/var/jira/jira-home");
    ApplicationProperty appProp2 = new ApplicationProperty();
    appProp2.setId("jira.clone.prefix");
    appProp2.setKey("jira.clone.prefix");
    appProp2.setName("The prefix added to the Summary field of cloned issues");
    appProp2.setType("string");
    appProp2.setValue("CLONE -");
    appProps = List.of(appProp1, appProp2);
  }

  @BeforeEach
  void setUp() {
    try {
      when(api.getAdvancedSettings()).thenReturn(appProps);
    } catch (ApiException e) {
      fail("Exception not expected");
    }
  }

  @Test
  @DisplayName("Get all advanced settings")
  public void testGetTableDefinition() {
    try (DASExecuteResult result =
        dasJiraAdvancedSettingsTable.execute(List.of(), List.of(), null, null)) {
      assertTrue(result.hasNext());
      assertEquals(result.next().getDataMap().get("id").getString().getV(), "jira.home");
    } catch (IOException e) {
      fail("Exception not expected");
    }
  }
}
