package com.rawlabs.das.jira.tables;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEqDistinct;
import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.createEq;
import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createLongType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.das.sdk.DASTable;
import com.rawlabs.das.sdk.java.utils.factory.value.*;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.Column;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.tables.TableDefinition;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class DASJiraTable implements DASTable {

  protected static final String TITLE_DESC = "Title of the resource.";
  protected static final int MAX_RESULTS = 1000;
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  protected final Map<String, String> options;
  protected final ValueFactory valueFactory = new DefaultValueFactory();
  protected final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();
  protected ObjectMapper objectMapper = new ObjectMapper();

  private final TableDefinition tableDefinition;
  private final Map<String, ColumnDefinition> columnDefinitions;

  public TableEstimate getTableEstimate(List<Qual> quals, List<String> columns) {
    return new TableEstimate(100, 100);
  }

  protected DASJiraTable(Map<String, String> options, String table, String description) {
    this.options = options;
    this.columnDefinitions = buildColumnDefinitions();
    this.tableDefinition =
        createTable(table, description, columnDefinitions.values().stream().toList());
  }

  public String getTableName() {
    return getTableDefinition().getTableId().getName();
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  protected void addToRow(
      String columnName, Row.Builder rowBuilder, Object value, List<String> columns) {
    if (hasProjection(columns, columnName)) {
      rowBuilder.addColumns(
          Column.newBuilder()
              .setName(columnName)
              .setData(
                  valueFactory.createValue(
                      new ValueTypeTuple(value, columnDefinitions.get(columnName).getType()))));
    }
  }

  private boolean hasProjection(List<String> columns, String columnName) {
    return columns == null || columns.isEmpty() || columns.contains(columnName);
  }

  protected abstract LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions();

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

  public Integer withMaxResultOrLimit(Long limit) {
    return limit == null ? MAX_RESULTS : (int) Math.min(limit, MAX_RESULTS);
  }

  public String withOrderBy(List<SortKey> sortKeys) {
    if (sortKeys.isEmpty()) {
      return null;
    }
    if (sortKeys.size() > 1) {
      throw new DASSdkException("Only one sort key is allowed.");
    }
    SortKey key = sortKeys.getFirst();
    return (key.getIsReversed() ? "-" : "+") + key.getName().replace("title", "name");
  }

  public List<Qual> withParentJoin(List<Qual> quals, String childColumn, String parentColumn) {
    List<Qual> qls = new ArrayList<>();
    Object eqId = extractEqDistinct(quals, childColumn);
    if (eqId != null) {
      qls.add(
          createEq(
              valueFactory.createValue(
                  new ValueTypeTuple(Long.valueOf((String) eqId), createLongType())),
              parentColumn));
    }
    return qls;
  }

  public List<String> getColumns() {
    return columnDefinitions.keySet().stream().toList();
  }

  public OffsetDateTime getDateTime(String dateString) {
    // Parse the string to an OffsetDateTime object
    return OffsetDateTime.parse(dateString, formatter);
  }
}
