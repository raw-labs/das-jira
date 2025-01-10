package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.tables.Column;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
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
    try (DASExecuteResult result = mockTable.execute(List.of(), List.of(), null)) {
      Row row = result.next();
      assertNotNull(row);
      assertEquals("1", getByKey(row, "id").getString().getV());
      assertEquals("mock1", getByKey(row, "name").getString().getV());
      row = result.next();
      assertNotNull(row);
      assertEquals("2", getByKey(row, "id").getString().getV());
      assertEquals("mock2", getByKey(row, "name").getString().getV());
      row = result.next();
      assertNotNull(row);
      assertEquals("3", getByKey(row, "id").getString().getV());
    } catch (IOException e) {
      fail("Failed to execute table", e);
    }
  }

  private Value getByKey(Row row, String key) {
    return row.getColumnsList().stream()
        .filter(c -> c.getName().equals(key))
        .map(Column::getData)
        .findFirst()
        .get();
  }
}
