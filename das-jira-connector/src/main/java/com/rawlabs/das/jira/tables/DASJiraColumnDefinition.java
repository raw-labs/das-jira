package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.raw.Type;
import com.rawlabs.protocol.raw.Value;

import java.util.Iterator;
import java.util.function.Function;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;

public class DASJiraColumnDefinition {
  private static final ValueFactory valueFactory = new DefaultValueFactory();
  private final Type type;
  private final ColumnDefinition columnDefinition;
  private final Function<Object, Object> transformation;

  private final DASJiraTableDefinition childTableDefinition;

  public DASJiraColumnDefinition(
      String name, String description, Type type, Function<Object, Object> transformation) {
    this.type = type;
    columnDefinition = createColumn(name, description, type);
    this.transformation = transformation;
    childTableDefinition = null;
  }

  public DASJiraColumnDefinition(
      String name,
      String description,
      Type type,
      Function<Object, Object> transformation,
      DASJiraTableDefinition childTableDefinition) {
    this.type = type;
    columnDefinition = createColumn(name, description, type);
    this.transformation = transformation;
    this.childTableDefinition = childTableDefinition;
  }

  void putToRow(Object object, Row.Builder rowBuilder) {
    Value v = valueFactory.createValue(new ValueTypeTuple(transformation.apply(object), type));
    rowBuilder.putData(columnDefinition.getName(), v);
    if (childTableDefinition != null) {
      Iterator<Object> iterator = childTableDefinition.hydrate(null, null, null, null);
      childTableDefinition.updateRow(rowBuilder, object);
    }
  }

  public ColumnDefinition getColumnDefinition() {
    return columnDefinition;
  }
}
