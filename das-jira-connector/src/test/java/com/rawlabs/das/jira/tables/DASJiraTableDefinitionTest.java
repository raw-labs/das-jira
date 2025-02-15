package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test mock table definition")
public class DASJiraTableDefinitionTest {

  @Test
  @DisplayName("Table execution")
  void testTableExecution() {
    MockTable mockTable = new MockTable(Map.of());
    try (DASExecuteResult result = mockTable.execute(List.of(), List.of(), null, null)) {
      Row row = result.next();
      assertNotNull(row);
      assertEquals("1", row.getDataMap().get("id").getString().getV());
      assertEquals("mock1", row.getDataMap().get("name").getString().getV());
      row = result.next();
      assertNotNull(row);
      assertEquals("2", row.getDataMap().get("id").getString().getV());
      assertEquals("mock2", row.getDataMap().get("name").getString().getV());
      row = result.next();
      assertNotNull(row);
      assertEquals("3", row.getDataMap().get("id").getString().getV());
    } catch (IOException e) {
      fail("Failed to execute table", e);
    }
  }
}
