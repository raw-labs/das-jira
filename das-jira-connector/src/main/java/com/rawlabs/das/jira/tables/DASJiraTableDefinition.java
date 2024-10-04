package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.HydrateFunction;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.*;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;

public class DASJiraTableDefinition<T> {
  private List<DASJiraColumnDefinition<T>> columnDefinitions;
  private final TableDefinition tableDefinition;
  private final HydrateFunction<T> hydrateFunction;

  public DASJiraTableDefinition(
      String name,
      String description,
      List<DASJiraColumnDefinition<T>> columnDefinitions,
      HydrateFunction<T> hydrateFunction) {
    this.columnDefinitions = columnDefinitions;
    tableDefinition =
        createTable(
            name,
            description,
            Stream.concat(
                    columnDefinitions.stream().map(DASJiraColumnDefinition::getColumnDefinition),
                    columnDefinitions.stream()
                        .flatMap(columnDefinition -> columnDefinition.getChildColumns().stream()))
                .toList());
    this.hydrateFunction = hydrateFunction;
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

    return new DASExecuteResult() {
      private final Iterator<DASJiraColumnDefinition<T>> definitions = columnDefinitions.iterator();
      private DASExecuteResult currentResult = null;

      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return definitions.hasNext() || (currentResult != null && currentResult.hasNext());
      }

      @Override
      public Row next() {
        if (currentResult == null) {
          DASJiraColumnDefinition<T> definition = definitions.next();
          currentResult = definition.getResult(rowBuilder, value, quals, columns, sortKeys, limit);
        }
        if (!currentResult.hasNext()) {
          currentResult = null;
          return next();
        }
        Row.Builder rowBuilder = Row.newBuilder();
        while (currentResult.hasNext()) {
          Row childRow = currentResult.next();
          childRow.getDataMap().forEach(rowBuilder::putData);
        }
        return rowBuilder.build();
      }
    };
  }

  public List<DASJiraColumnDefinition<T>> getColumnDefinitions() {
    return columnDefinitions;
  }

  public void setColumnDefinitions(List<DASJiraColumnDefinition<T>> columnDefinitions) {
    this.columnDefinitions = columnDefinitions;
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
