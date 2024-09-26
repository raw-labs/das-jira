package com.rawlabs.das.jira.tables.defnitions;

import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.rest.jira.ApiException;
import com.rawlabs.das.rest.jira.api.JiraSettingsApi;
import com.rawlabs.das.rest.jira.model.ApplicationProperty;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.utils.factory.ValueFactory;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.QualFactory.createEq;
import static com.rawlabs.das.sdk.java.utils.factory.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Advanced Settings Table Test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
      when(api.getApplicationProperty("jira.home", null, null)).thenReturn(appProps.subList(0, 1));
      when(api.getApplicationProperty("jira.clone.prefix", null, null))
          .thenReturn(appProps.subList(1, 2));
    } catch (ApiException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get all advanced settings")
  public void testGetAllAdvancedSettings() {
    try (DASExecuteResult result =
        dasJiraAdvancedSettingsTable.execute(List.of(), List.of(), null, null)) {
      assertTrue(result.hasNext());
      assertEquals(result.next().getDataMap().get("id").getString().getV(), "jira.home");
      assertTrue(result.hasNext());
      assertEquals(result.next().getDataMap().get("id").getString().getV(), "jira.clone.prefix");
    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get settings by key")
  public void testGetSettingsByKey() {
    try (DASExecuteResult result =
        dasJiraAdvancedSettingsTable.execute(
            List.of(
                createEq(
                    ValueFactory.getInstance().createValue("jira.home", createStringType()),
                    "key")),
            List.of(),
            null,
            null)) {
      assertTrue(result.hasNext());
      Row row = result.next();
      assertEquals(row.getDataMap().get("id").getString().getV(), "jira.home");
      assertEquals(row.getDataMap().get("key").getString().getV(), "jira.home");
      assertEquals(row.getDataMap().get("title").getString().getV(), "jira.home");
      assertEquals(row.getDataMap().get("description").getString().getV(), "Jira home directory");
      assertEquals(row.getDataMap().get("type").getString().getV(), "string");
      assertEquals(row.getDataMap().get("value").getString().getV(), "/var/jira/jira-home");
      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }

    try (DASExecuteResult result =
        dasJiraAdvancedSettingsTable.execute(
            List.of(
                createEq(
                    ValueFactory.getInstance().createValue("jira.clone.prefix", createStringType()),
                    "key")),
            List.of(),
            null,
            null)) {
      assertTrue(result.hasNext());
      Row row = result.next();
      assertEquals(row.getDataMap().get("id").getString().getV(), "jira.clone.prefix");
      assertEquals(row.getDataMap().get("key").getString().getV(), "jira.clone.prefix");
      assertEquals(
          row.getDataMap().get("title").getString().getV(),
          "The prefix added to the Summary field of cloned issues");
      assertEquals(row.getDataMap().get("type").getString().getV(), "string");
      assertEquals(row.getDataMap().get("value").getString().getV(), "CLONE -");
      assertTrue(row.getDataMap().get("description").hasNull());

      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }
}
