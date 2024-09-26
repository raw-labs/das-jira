package com.rawlabs.das.jira.tables;

import com.google.common.collect.HashBiMap;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.das.sdk.java.utils.DASRowModelMapper;
import com.rawlabs.das.sdk.java.utils.factory.ExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.ValueFactory;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.raw.*;
import org.apache.commons.text.CaseUtils;

import java.beans.Statement;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class DASJiraRowModelMapper extends DASRowModelMapper {

  private final Class<?> clazz;

  /**
   * Constructor for DASJiraModelToRow
   *
   * @param clazz Openapi generated model to be converted to Row (needed for reflection of fields)
   * @param tableDefinition Table definition for the table
   * @param transformations Map of transformations to be applied to the field names (key: original
   *     field name, value: transformed field name)
   */
  public DASJiraRowModelMapper(
      Class<?> clazz, TableDefinition tableDefinition, Map<String, String> transformations) {
    super(HashBiMap.create(transformations), tableDefinition);
    this.clazz = clazz;
  }

  public DASJiraRowModelMapper(Class<?> clazz, TableDefinition tableDefinition) {
    this(clazz, tableDefinition, Map.of());
  }

  @Override
  protected Value getValue(ColumnDefinition column, Object obj) {
    try {
      return ValueFactory.getInstance()
          .createValue(
              clazz
                  .getMethod(
                      "get"
                          + CaseUtils.toCamelCase(
                              withTransformationInverse(column.getName()), true, '_'))
                  .invoke(obj),
              column.getType());
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new DASSdkException("could not extract value", e);
    }
  }

  @Override
  public Object toModel(Row row) {
    try {
      Object obj = clazz.getDeclaredConstructor().newInstance();
      List<ColumnDefinition> columns = tableDefinition.getColumnsList();
      for (ColumnDefinition column : columns) {
        String columnName = column.getName();
        Value value =
            row.getDataOrDefault(withTransformation(columnName), Value.getDefaultInstance());
        Object extracted = ExtractValueFactory.getInstance().extractValue(value);
        new Statement(
                obj,
                "set" + CaseUtils.toCamelCase(withTransformationInverse(columnName), true, '_'),
                new Object[] {extracted})
            .execute();
//        clazz
//            .getMethod(
//                "set" + CaseUtils.toCamelCase(withTransformationInverse(columnName), true, '_'),
//                extracted.getClass())
//            .invoke(obj, extracted);
      }
      return obj;
    } catch (Exception e) {
      throw new DASSdkException("could not create model", e);
    }
  }
}
