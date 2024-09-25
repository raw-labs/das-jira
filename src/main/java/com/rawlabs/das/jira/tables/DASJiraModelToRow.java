package com.rawlabs.das.jira.tables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.das.sdk.java.utils.ValueFactory;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.raw.*;
import org.apache.commons.text.CaseUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class DASJiraModelToRow {

  private final Class<?> clazz;
  private final BiMap<String, String> transformations;
  private final TableDefinition tableDefinition;
  private final ValueFactory valueFactory = new ValueFactory();

  /**
   * Constructor for DASJiraModelToRow
   *
   * @param clazz Openapi generated model to be converted to Row (needed for reflection of fields)
   * @param tableDefinition Table definition for the table
   * @param transformations Map of transformations to be applied to the field names (key: original
   *     field name, value: transformed field name)
   */
  public DASJiraModelToRow(
      Class<?> clazz, TableDefinition tableDefinition, Map<String, String> transformations) {
    this.clazz = clazz;
    this.transformations = HashBiMap.create(transformations);
    this.tableDefinition = tableDefinition;
  }

  public DASJiraModelToRow(Class<?> clazz, TableDefinition tableDefinition) {
    this(clazz, tableDefinition, Map.of());
  }

  public Row toRow(Object obj) {
    Row.Builder rowBuilder = Row.newBuilder();
    List<ColumnDefinition> columns = tableDefinition.getColumnsList();
    for (ColumnDefinition column : columns) {
      String columnName = column.getName();
      rowBuilder.putData(checkForTransformation(columnName), getValue(column, obj));
    }
    return rowBuilder.build();
  }

  private Value getValue(ColumnDefinition column, Object obj) {
    return this.valueFactory.createValue(extractValue(obj, column.getName()), column.getType());
  }

  private Object extractValue(Object obj, String fieldName) {
    try {
      return clazz
          .getMethod(
              "get" + CaseUtils.toCamelCase(checkForTransformationInverse(fieldName), true, '_'))
          .invoke(obj);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new DASSdkException("could not extract value", e);
    }
  }

  private String checkForTransformation(String fieldName) {
    return transformations.getOrDefault(fieldName, fieldName);
  }

  private String checkForTransformationInverse(String fieldName) {
    return transformations.inverse().getOrDefault(fieldName, fieldName);
  }
}
