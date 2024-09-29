package com.rawlabs.das.jira;

import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.sdk.java.DASSdk;
import com.rawlabs.das.sdk.java.DASTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("General DAS Jira Test")
public class DASJiraTest {

  @Test
  public void testGetDasType() {
    DASJiraBuilder dasJiraBuilder = new DASJiraBuilder();
    assertNotNull(dasJiraBuilder);
    assertEquals(dasJiraBuilder.getDasType(), "jira");

    DASSdk dasJira =
        dasJiraBuilder.build(Map.of("base_url", "test_url", "personal_access_token", "pat"), null);
    assertNotNull(dasJira);
    assertInstanceOf(DASJira.class, dasJira);
    int tableDefinitionsCount = dasJira.getTableDefinitions().size();
    assertNotEquals(0, tableDefinitionsCount);
    DASTable tablesCount = dasJira.getTable(DASJiraAdvancedSettingsTable.TABLE_NAME);
    assertNotNull(tablesCount);
  }
}
