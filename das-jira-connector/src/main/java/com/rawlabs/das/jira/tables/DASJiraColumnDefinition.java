package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.raw.Type;
import com.rawlabs.protocol.raw.Value;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;

public class DASJiraColumnDefinition {
  private static final ValueFactory valueFactory = new DefaultValueFactory();
  private final Type type;
  private final ColumnDefinition columnDefinition;

  public DASJiraColumnDefinition(String name, String description, Type type) {
    this.type = type;
    columnDefinition = createColumn(name, description, type);
  }

  public void putToRow(Object object, Row.Builder rowBuilder) {
    Value v = valueFactory.createValue(new ValueTypeTuple(object, type));
    rowBuilder.putData(columnDefinition.getName(), v);
  }

  public ColumnDefinition getColumnDefinition() {
    return columnDefinition;
  }

  public String getName() {
    return columnDefinition.getName();
  }
}
