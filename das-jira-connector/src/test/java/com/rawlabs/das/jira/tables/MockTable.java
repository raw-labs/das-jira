package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

public class MockTable extends DASJiraTable {

  public static final String TABLE_NAME = "mock_table";

  public MockTable(Map<String, String> options) {
    super(options, TABLE_NAME, "Mock table for testing.");
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    return Map.of(
        "id", createColumn("id", "id", createStringType()),
        "name", createColumn("name", "name", createStringType()));
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
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
    addToRow("id", builder, mockObject.getId());
    addToRow("name", builder, mockObject.getName());
    return builder.build();
  }

  public static class MockObject {
    private String id;
    private String name;

    public MockObject(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }
}
