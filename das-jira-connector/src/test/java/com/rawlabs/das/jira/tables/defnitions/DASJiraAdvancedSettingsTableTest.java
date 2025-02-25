package com.rawlabs.das.jira.tables.defnitions;

import static com.rawlabs.das.jira.utils.factory.qual.QualFactory.createEq;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.ApplicationProperty;
import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkInvalidArgumentException;
import com.rawlabs.das.jira.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.v1.tables.Column;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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
        dasJiraAdvancedSettingsTable.execute(List.of(), List.of(), List.of(), null)) {
      assertTrue(result.hasNext());
      assertEquals(result.next().getColumns(0).getData().getString().getV(), "jira.home");
      assertTrue(result.hasNext());
      assertEquals(result.next().getColumns(0).getData().getString().getV(), "jira.clone.prefix");
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
            List.of(),
            null)) {
      assertTrue(result.hasNext());
      Row row = result.next();
      assertEquals(getByKey(row, "id").getString().getV(), "jira.home");
      assertEquals(getByKey(row, "key").getString().getV(), "jira.home");
      assertEquals(getByKey(row, "title").getString().getV(), "jira.home");
      assertEquals(getByKey(row, "description").getString().getV(), "Jira home directory");
      assertEquals(getByKey(row, "type").getString().getV(), "string");
      assertEquals(getByKey(row, "value").getString().getV(), "/var/jira/jira-home");
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
            List.of(),
            null)) {
      assertTrue(result.hasNext());
      Row row = result.next();
      assertEquals(getByKey(row, "id").getString().getV(), "jira.clone.prefix");
      assertEquals(getByKey(row, "key").getString().getV(), "jira.clone.prefix");
      assertEquals(
          getByKey(row, "title").getString().getV(),
          "The prefix added to the Summary field of cloned issues");
      assertEquals(getByKey(row, "type").getString().getV(), "string");
      assertEquals(getByKey(row, "value").getString().getV(), "CLONE -");
      assertTrue(getByKey(row, "description").hasNull());

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
            List.of(),
            1L)) {
      assertTrue(result.hasNext());
      assertEquals(result.next().getColumns(0).getData().getString().getV(), "jira.home");
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
        DASSdkInvalidArgumentException.class,
        () -> {
          try (DASExecuteResult result =
              dasJiraAdvancedSettingsTable.execute(
                  List.of(
                      createEq(
                          valueFactory.createValue(
                              new ValueTypeTuple("throw error", createStringType())),
                          "key")),
                  List.of(),
                  List.of(),
                  null)) {
            fail("Exception expected");
          }
        });
  }

  private Value getByKey(Row row, String key) {
    return row.getColumnsList().stream()
        .filter(c -> c.getName().equals(key))
        .map(Column::getData)
        .findFirst()
        .get();
  }
}
