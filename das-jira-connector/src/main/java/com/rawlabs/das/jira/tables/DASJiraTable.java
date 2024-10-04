package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.RowsEstimation;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.*;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;

public abstract class DASJiraTable implements DASTable {

  protected static final String TITLE_DESC = "Title of the resource.";

  protected final Map<String, String> options;
  protected final ValueFactory valueFactory = new DefaultValueFactory();

  private final TableDefinition tableDefinition;
  private final Map<String, ColumnDefinition> columnDefinitions;

  private final RowsEstimation rowsEstimation = new RowsEstimation(100, 100);

  protected DASJiraTable(Map<String, String> options, String table, String description) {
    this.options = options;
    this.columnDefinitions = buildColumnDefinitions();
    this.tableDefinition =
        createTable(table, description, columnDefinitions.values().stream().toList());
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }

  public String getTableName() {
    return getTableDefinition().getTableId().getName();
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  @Override
  public RowsEstimation getRelSize(List<Qual> quals, List<String> columns) {
    return rowsEstimation;
  }

  protected void addToRow(String columnName, Row.Builder rowBuilder, Object value) {
    rowBuilder.putData(
        columnName,
        valueFactory.createValue(
            new ValueTypeTuple(value, columnDefinitions.get(columnName).getType())));
  }

  protected void initRow(Row.Builder rowBuilder) {
    columnDefinitions.keySet().forEach(columnName -> addToRow(columnName, rowBuilder, null));
  }

  protected abstract Map<String, ColumnDefinition> buildColumnDefinitions();

  public DASExecuteResult fromRowIterator(Iterator<Row> rows) {
    return new DASExecuteResult() {
      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return rows.hasNext();
      }

      @Override
      public Row next() {
        return rows.next();
      }
    };
  }
}
