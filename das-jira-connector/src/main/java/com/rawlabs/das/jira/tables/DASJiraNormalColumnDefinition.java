package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Type;
import com.rawlabs.protocol.raw.Value;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
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
  public DASExecuteResult getResult(
      Row.Builder rowBuilder,
      T object,
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    Value v = valueFactory.createValue(new ValueTypeTuple(transformation.apply(object), type));
    Row newRow = rowBuilder.putData(columnDefinition.getName(), v).build();
    Iterator<Row> iterator = List.of(newRow).iterator();
    return new DASExecuteResult() {
      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Row next() {
        return iterator.next();
      }
    };
  }

  @Override
  public ColumnDefinition getColumnDefinition() {
    return columnDefinition;
  }

  @Override
  public String getName() {
    return this.columnDefinition.getName();
  }

  @Override
  public List<ColumnDefinition> getChildColumns() {
    return List.of();
  }
}
