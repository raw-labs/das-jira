package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.initializer.DASJiraInitializer;
import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("DAS Jira Table Manager Test")
public class DASJiraTableManagerTest {

  public static final DASJiraTableManager dasJiraTableManager = new DASJiraTableManager(Map.of(), null, null);

  @Test
  @DisplayName("Table manager initialization test")
  public void testGetTableDefinition() {
    assertNotNull(dasJiraTableManager);
    DASJiraTable table = dasJiraTableManager.getTable(DASJiraAdvancedSettingsTable.TABLE_NAME);
    assertNotNull(table);
    int tableDefinitionsCount = dasJiraTableManager.getTableDefinitions().size();
    assertNotEquals(0, tableDefinitionsCount);
    int tablesCount = dasJiraTableManager.getTables().size();
    assertNotEquals(0, tablesCount);
  }
}
