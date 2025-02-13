package com.rawlabs.das.jira.tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.RowsEstimation;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.das.sdk.java.utils.factory.value.*;
import com.rawlabs.protocol.das.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEqDistinct;
import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.createEq;
import static com.rawlabs.das.sdk.java.utils.factory.table.TableFactory.createTable;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createLongType;

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

  private final RowsEstimation rowsEstimation = new RowsEstimation(100, 100);

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

  @Override
  public RowsEstimation getRelSize(List<Qual> quals, List<String> columns) {
    return rowsEstimation;
  }

  protected void addToRow(
      String columnName, Row.Builder rowBuilder, Object value, List<String> columns) {
    if (hasProjection(columns, columnName)) {
      ColumnDefinition definition = columnDefinitions.get(columnName);
      rowBuilder.putData(
          columnName, valueFactory.createValue(new ValueTypeTuple(value, definition.getType())));
    }
  }

  private boolean hasProjection(List<String> columns, String columnName) {
    return columnDefinitions.containsKey(columnName)
        && (columns == null || columns.isEmpty() || columns.contains(columnName));
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
    return Optional.ofNullable(sortKeys)
        .map(
            keys -> {
              if (keys.size() > 1) throw new DASSdkApiException("Only one sort key is allowed.");
              return keys.getFirst();
            })
        .map(key -> (key.getIsReversed() ? "-" : "+") + key.getName().replace("title", "name"))
        .orElse(null);
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
