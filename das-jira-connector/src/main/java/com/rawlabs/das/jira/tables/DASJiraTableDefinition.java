package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.HydrateFunction;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;

public class DASJiraTableDefinition<T> {
  private final List<DASJiraColumnDefinitionWithoutChildren<T>>
      dasJiraColumnDefinitionWithoutChildren;
  private final DASJiraColumnDefinitionWithChildren<T, ?> dasJiraColumnDefinitionWithChildren;

  private final TableDefinition tableDefinition;
  private final HydrateFunction<T> hydrateFunction;

  public DASJiraTableDefinition(
      String name,
      String description,
      List<DASJiraColumnDefinitionWithoutChildren<T>> columnDefinitions,
      DASJiraColumnDefinitionWithChildren<T, ?> parentColumnDefinition,
      HydrateFunction<T> hydrateFunction) {
    this.dasJiraColumnDefinitionWithoutChildren = columnDefinitions;
    this.dasJiraColumnDefinitionWithChildren = parentColumnDefinition;

    List<ColumnDefinition> columns =
        new ArrayList<>(
            columnDefinitions.stream()
                .map(DASJiraColumnDefinitionWithoutChildren::getColumnDefinition)
                .toList());

    if (parentColumnDefinition != null) {
      columns.addAll(parentColumnDefinition.getChildColumns());
    }

    tableDefinition = createTable(name, description, columns);
    this.hydrateFunction = hydrateFunction;
  }

  public DASJiraTableDefinition(
      String name,
      String description,
      List<DASJiraColumnDefinitionWithoutChildren<T>> columnDefinitions,
      HydrateFunction<T> hydrateFunction) {
    this(name, description, columnDefinitions, null, hydrateFunction);
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  public DASExecuteResult getResult(
      Row.Builder rowBuilder,
      T value,
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {

    dasJiraColumnDefinitionWithoutChildren.forEach(
        definition -> definition.updateRow(rowBuilder, value));

    if (dasJiraColumnDefinitionWithChildren == null) {
      Iterator<Row> iterator = List.of(rowBuilder.build()).iterator();
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
    } else {
      return new DASExecuteResult() {
        private final DASExecuteResult result =
            dasJiraColumnDefinitionWithChildren.getResult(
                rowBuilder, value, quals, columns, sortKeys, limit);

        @Override
        public void close() {}

        @Override
        public boolean hasNext() {
          return result.hasNext();
        }

        @Override
        public Row next() {
          Row.Builder rb = rowBuilder.clone();
          Row next = result.next();
          next.getDataMap().forEach(rb::putData);
          return rb.build();
        }
      };
    }
  }

  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    Iterator<T> iterator = hydrateFunction.hydrate(quals, columns, sortKeys, limit);

    return new DASExecuteResult() {
      DASExecuteResult currentResult = null;

      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return iterator.hasNext() || (currentResult != null && currentResult.hasNext());
      }

      @Override
      public Row next() {
        Row.Builder rowBuilder = Row.newBuilder();
        if ((currentResult == null || !currentResult.hasNext()) && iterator.hasNext()) {
          T next = iterator.next();
          currentResult = getResult(rowBuilder, next, quals, columns, sortKeys, limit);
        }
        assert currentResult != null;
        return currentResult.next();
      }
    };
  }
}
