package com.rawlabs.das.jira.tables.results;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.*;

import com.rawlabs.das.sdk.java.utils.factory.value.*;
import com.rawlabs.protocol.das.v1.tables.Column;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DAS Jira Pages Test")
public class DASJiraPagesTest {

  ValueFactory valueFactory = new DefaultValueFactory();
  private final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();

  private DASJiraPage<String> getPage(Long offset) {
    List<String> page1 = List.of("result1", "result2");
    List<String> page2 = List.of("result3", "result4");
    List<String> page3 = List.of("result5");
    Long total = 5L;
    if (offset < 2) {
      return new DASJiraPage<>(page1, total);
    } else if (offset < 4) {
      return new DASJiraPage<>(page2, total);
    } else {
      return new DASJiraPage<>(page3, total);
    }
  }

  @Test
  @DisplayName("Test paginated results")
  public void testPaginatedResults() throws IOException {

    try (DASJiraPaginatedResult<String> pagedResult =
        new DASJiraPaginatedResult<>(null) {
          @Override
          public Row next() {
            Row.Builder rowBuilder = Row.newBuilder();
            rowBuilder.addColumns(
                Column.newBuilder()
                    .setName("id")
                    .setData(
                        valueFactory.createValue(
                            new ValueTypeTuple(this.getNext(), createStringType()))));
            return rowBuilder.build();
          }

          @Override
          public DASJiraPage<String> fetchPage(long offset) {
            return getPage(offset);
          }
        }) {
      Row row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result1", getByKey(row, "id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result2", getByKey(row, "id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result3", getByKey(row, "id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result4", getByKey(row, "id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result5", getByKey(row, "id").getString().getV());
      assertFalse(pagedResult.hasNext());
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
