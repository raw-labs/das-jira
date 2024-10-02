package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.HydrateFunction;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;

public class DASJiraTableDefinition<T> {
  List<DASJiraColumnDefinition<T>> columnDefinitions;
  TableDefinition tableDefinition;
  HydrateFunction<T> hydrateFunction;

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

  public void updateRow(Row.Builder rowBuilder, T value) {
    columnDefinitions.forEach((columnDefinition) -> columnDefinition.putToRow(value, rowBuilder));
  }

  public Iterator<T> hydrate(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return hydrateFunction.hydrate(quals, columns, sortKeys, limit);
  }

  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    Iterator<T> iterator = hydrate(quals, columns, sortKeys, limit);
    return new DASExecuteResult() {
      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Row next() {
        Row.Builder rowBuilder = Row.newBuilder();
        updateRow(rowBuilder, iterator.next());
        return rowBuilder.build();
      }
    };
  }
}
