package com.rawlabs.das.jira.tables.results;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DAS Jira Pages Test")
public class DASJiraPagesTest {

  ValueFactory valueFactory = new DefaultValueFactory();

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
        new DASJiraPaginatedResult<>() {
          @Override
          public Row next() {
            Row.Builder rowBuilder = Row.newBuilder();
            rowBuilder.putData(
                "id",
                valueFactory.createValue(new ValueTypeTuple(this.getNext(), createStringType())));
            return rowBuilder.build();
          }

          @Override
          public DASJiraPage<String> fetchPage(long offset) {
            return getPage(offset);
          }
        }) {
      Row row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result1", row.getDataMap().get("id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result2", row.getDataMap().get("id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result3", row.getDataMap().get("id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result4", row.getDataMap().get("id").getString().getV());
      assertTrue(pagedResult.hasNext());
      row = pagedResult.next();
      assertNotNull(row);
      assertEquals("result5", row.getDataMap().get("id").getString().getV());
      assertFalse(pagedResult.hasNext());
    }
  }
}
