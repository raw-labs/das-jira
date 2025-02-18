package com.rawlabs.das.jira.tables;

import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.createStringType;

import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class MockTable extends DASJiraTable {

  public static final String TABLE_NAME = "mock_table";

  public MockTable(Map<String, String> options) {
    super(options, TABLE_NAME, "Mock table for testing.");
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columnDefinitions = new LinkedHashMap<>();
    columnDefinitions.put("id", createColumn("id", "id", createStringType()));
    columnDefinitions.put("name", createColumn("name", "name", createStringType()));
    return columnDefinitions;
  }

  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      List<SortKey> sortKeys,
      @Nullable Long limit) {

    return new DASExecuteResult() {
      private final Iterator<MockObject> mockObjects =
          List.of(
                  new MockObject("1", "mock1"),
                  new MockObject("2", "mock2"),
                  new MockObject("3", "mock3"))
              .iterator();

      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return mockObjects.hasNext();
      }

      @Override
      public Row next() {
        return toRow(mockObjects.next());
      }
    };
  }

  public Row toRow(MockObject mockObject) {
    Row.Builder builder = Row.newBuilder();
    addToRow("id", builder, mockObject.id(), List.of());
    addToRow("name", builder, mockObject.name(), List.of());
    return builder.build();
  }

  public record MockObject(String id, String name) {
  }
}
