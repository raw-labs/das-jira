package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.raw.Type;
import com.rawlabs.protocol.raw.Value;

import java.util.List;
import java.util.function.Function;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;

public class DASJiraNormalColumnDefinition<T> implements DASJiraColumnDefinition<T> {
  private static final ValueFactory valueFactory = new DefaultValueFactory();
  private final Type type;
  private final ColumnDefinition columnDefinition;
  private final Function<T, Object> transformation;

  public DASJiraNormalColumnDefinition(
      String name, String description, Type type, Function<T, Object> transformation) {
    this.type = type;
    columnDefinition = createColumn(name, description, type);
    this.transformation = transformation;
  }

  @Override
  public void putToRow(T object, Row.Builder rowBuilder) {
    Value v = valueFactory.createValue(new ValueTypeTuple(transformation.apply(object), type));
    rowBuilder.putData(columnDefinition.getName(), v);
  }

  @Override
  public ColumnDefinition getColumnDefinition() {
    return columnDefinition;
  }

  @Override
  public List<ColumnDefinition> getChildColumns() {
    return List.of();
  }
}
