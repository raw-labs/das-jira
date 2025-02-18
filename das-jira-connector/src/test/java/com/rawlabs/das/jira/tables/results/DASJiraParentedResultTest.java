package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.jira.tables.MockTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASTable;
import com.rawlabs.das.jira.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.v1.tables.Column;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.createStringType;
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
                rowBuilder.addColumns(
                    Column.newBuilder()
                        .setName("child_id")
                        .setData(
                            valueFactory.createValue(
                                new ValueTypeTuple("child" + count++, createStringType()))));
                return rowBuilder.build();
              }
            };
          }
        }) {
      assertTrue(res.hasNext());
      Row row = res.next();
      assertNotNull(row);
      assertEquals("child1", getByKey(row, "child_id").getString().getV());
      assertEquals("1", getByKey(row, "id").getString().getV());
      assertEquals("mock1", getByKey(row, "name").getString().getV());
      assertTrue(res.hasNext());
      row = res.next();
      assertNotNull(row);
      assertEquals("child2", getByKey(row, "child_id").getString().getV());
      assertEquals("2", getByKey(row, "id").getString().getV());
      assertEquals("mock2", getByKey(row, "name").getString().getV());
      assertTrue(res.hasNext());
      row = res.next();
      assertNotNull(row);
      assertEquals("child3", getByKey(row, "child_id").getString().getV());
      assertEquals("3", getByKey(row, "id").getString().getV());
      assertEquals("mock3", getByKey(row, "name").getString().getV());
      assertFalse(res.hasNext());
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
