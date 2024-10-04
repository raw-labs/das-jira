package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
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
import java.util.List;
import java.util.function.Function;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;

public class DASJiraParentColumnDefinition<T, K> implements DASJiraColumnDefinition<T> {
  private static final ValueFactory valueFactory = new DefaultValueFactory();
  private final Type type;
  private final ColumnDefinition columnDefinition;
  private final Function<T, Object> transformation;

  private final DASJiraTableDefinition<K> childTableDefinition;

  public DASJiraParentColumnDefinition(
      String name,
      String description,
      Type type,
      Function<T, Object> transformation,
      DASJiraTableDefinition<K> childTableDefinition) {
    this.type = type;
    columnDefinition = createColumn(name, description, type);
    this.transformation = transformation;
    this.childTableDefinition = childTableDefinition;
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
    try (DASExecuteResult result = childTableDefinition.execute(quals, columns, sortKeys, limit)) {
      return new DASExecuteResult() {
        final Row.Builder rowBuilder = newRow.toBuilder();

        @Override
        public void close() {}

        @Override
        public boolean hasNext() {
          return result.hasNext();
        }

        @Override
        public Row next() {
          while (result.hasNext()) {
            Row childRow = result.next();
            childRow.getDataMap().forEach(rowBuilder::putData);
          }
          return rowBuilder.build();
        }
      };
    } catch (IOException e) {
      throw new DASSdkException(e.getMessage(), e);
    }
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
    return childTableDefinition.getTableDefinition().getColumnsList();
  }
}
