package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.jira.tables.MockTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DAS Jira table with parent result tests")
public class DASJiraParentedResultTest {
  ValueFactory valueFactory = new DefaultValueFactory();

  private static DASTable parentTable = new MockTable(Map.of());

  @Test
  @DisplayName("Test parented result results")
  public void testPaginatedResults() {

    try (DASJiraWithParentTableResult res =
        new DASJiraWithParentTableResult(parentTable, List.of(), List.of(), List.of(), null) {
          private int count = 1;

          @Override
          public DASExecuteResult fetchChildResult(Row parentRow) {
            return new DASExecuteResult() {
              boolean first = true;

              @Override
              public void close() {}

              @Override
              public boolean hasNext() {
                if (first) {
                  first = false;
                  return true;
                }
                return false;
              }

              @Override
              public Row next() {
                Row.Builder rowBuilder = parentRow.toBuilder();
                rowBuilder.putData(
                    "child_id",
                    valueFactory.createValue(
                        new ValueTypeTuple("child" + count++, createStringType())));
                return rowBuilder.build();
              }
            };
          }
        }) {
      assertTrue(res.hasNext());
      Row row = res.next();
      assertNotNull(row);
      assertEquals("child1", row.getDataMap().get("child_id").getString().getV());
      assertEquals("1", row.getDataMap().get("id").getString().getV());
      assertEquals("mock1", row.getDataMap().get("name").getString().getV());
      assertTrue(res.hasNext());
      row = res.next();
      assertNotNull(row);
      assertEquals("child2", row.getDataMap().get("child_id").getString().getV());
      assertEquals("2", row.getDataMap().get("id").getString().getV());
      assertEquals("mock2", row.getDataMap().get("name").getString().getV());
      assertTrue(res.hasNext());
      row = res.next();
      assertNotNull(row);
      assertEquals("child3", row.getDataMap().get("child_id").getString().getV());
      assertEquals("3", row.getDataMap().get("id").getString().getV());
      assertEquals("mock3", row.getDataMap().get("name").getString().getV());
      assertFalse(res.hasNext());
    }
  }
}
