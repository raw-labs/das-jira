package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.model.ApplicationProperty;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.createEq;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Advanced Settings Table Test")
public class DASJiraAdvancedSettingsTableTest extends BaseMockTest {

  @Mock static JiraSettingsApi jiraSettingsApi;

  @InjectMocks DASJiraAdvancedSettingsTable dasJiraAdvancedSettingsTable;

  private static List<ApplicationProperty> appProps;

  @BeforeAll
  static void beforeAll() throws IOException {
    ArrayNode node = (ArrayNode) loadJson("application-properties.json");
    appProps =
        List.of(
            ApplicationProperty.fromJson(node.get(0).toString()),
            ApplicationProperty.fromJson(node.get(1).toString()));
  }

  @BeforeEach
  void setUp() {
    try {
      when(jiraSettingsApi.getAdvancedSettings()).thenReturn(appProps);
      when(jiraSettingsApi.getApplicationProperty("jira.home", null, null))
          .thenReturn(appProps.subList(0, 1));
      when(jiraSettingsApi.getApplicationProperty("jira.clone.prefix", null, null))
          .thenReturn(appProps.subList(1, 2));
      when(jiraSettingsApi.getApplicationProperty("throw error", null, null))
          .thenThrow(new ApiException("couldn't fetch data"));
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
    ValueFactory valueFactory = new DefaultValueFactory();
    try (DASExecuteResult result =
        dasJiraAdvancedSettingsTable.execute(
            List.of(
                createEq(
                    valueFactory.createValue(new ValueTypeTuple("jira.home", createStringType())),
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
                    valueFactory.createValue(
                        new ValueTypeTuple("jira.clone.prefix", createStringType())),
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

  @Test
  @DisplayName("Get all advanced settings with limit")
  public void testGetAllAdvancedSettingsWithLimit() {
    try (DASExecuteResult result =
        dasJiraAdvancedSettingsTable.execute(
            List.of(
                createEq(
                    valueFactory.createValue(new ValueTypeTuple("jira.home", createStringType())),
                    "key")),
            List.of(),
            null,
            1L)) {
      assertTrue(result.hasNext());
      assertEquals(result.next().getDataMap().get("id").getString().getV(), "jira.home");
      assertFalse(result.hasNext());
    } catch (IOException e) {
      fail("Exception not expected: %s".formatted(e.getMessage()));
    }
  }

  @Test
  @DisplayName("Get all advanced settings with error")
  public void testGetAllAdvancedSettingsWithWithError() {
    ValueFactory valueFactory = new DefaultValueFactory();
    assertThrows(
        DASSdkApiException.class,
        () -> {
          try (DASExecuteResult result =
              dasJiraAdvancedSettingsTable.execute(
                  List.of(
                      createEq(
                          valueFactory.createValue(
                              new ValueTypeTuple("throw error", createStringType())),
                          "key")),
                  List.of(),
                  null,
                  1L)) {
            fail("Exception expected");
          }
        });
  }
}
