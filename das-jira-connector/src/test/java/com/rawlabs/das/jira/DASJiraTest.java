package com.rawlabs.das.jira;

import static org.junit.jupiter.api.Assertions.*;

import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.sdk.DASSdk;
import com.rawlabs.das.sdk.DASTable;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("General DAS Jira Test")
public class DASJiraTest {

  @Test
  public void testGetDasType() {
    DASJiraBuilder dasJiraBuilder = new DASJiraBuilder();
    assertNotNull(dasJiraBuilder);
    assertEquals("jira", dasJiraBuilder.getDasType());
    DASSdk dasJira =
        dasJiraBuilder.build(Map.of("base_url", "test_url", "personal_access_token", "pat"), null);
    assertNotNull(dasJira);
    assertInstanceOf(DASJira.class, dasJira);
    int tableDefinitionsCount = dasJira.getTableDefinitions().size();
    assertNotEquals(0, tableDefinitionsCount);
    Optional<DASTable> tablesCount = dasJira.getTable(DASJiraAdvancedSettingsTable.TABLE_NAME);
    assertTrue(tablesCount.isPresent());
  }
}
