package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.das.TableId;
import com.rawlabs.protocol.raw.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DASJiraModelToRowTest {

  private static final TableDefinition tableDefinition =
      TableDefinition.newBuilder()
          .setTableId(TableId.newBuilder().setName("test_table"))
          .addColumns(
              ColumnDefinition.newBuilder()
                  .setName("string_field")
                  .setDescription("StringField")
                  .setType(
                      Type.newBuilder()
                          .setString(
                              StringType.newBuilder().setTriable(false).setNullable(true).build()))
                  .build())
          .addColumns(
              ColumnDefinition.newBuilder()
                  .setName("int_field")
                  .setDescription("IntField")
                  .setType(
                      Type.newBuilder()
                          .setInt(IntType.newBuilder().setTriable(false).setNullable(true).build()))
                  .build())
          .addColumns(
              ColumnDefinition.newBuilder()
                  .setName("boolean_field")
                  .setDescription("booleanField")
                  .setType(
                      Type.newBuilder()
                          .setBool(
                              BoolType.newBuilder().setTriable(false).setNullable(true).build()))
                  .build())
          .addColumns(
              ColumnDefinition.newBuilder()
                  .setName("list_field")
                  .setDescription("listField")
                  .setType(
                      Type.newBuilder()
                          .setList(
                              ListType.newBuilder()
                                  .setInnerType(
                                      Type.newBuilder()
                                          .setString(
                                              StringType.newBuilder()
                                                  .setNullable(true)
                                                  .setTriable(false)
                                                  .build())
                                          .build())
                                  .build())
                          .build())
                  .build())
          .build();

  @Test
  @DisplayName("Model to row without column transformation")
  public void testModelWithoutTransformationToRow() {
    AllTypesTestObject testObject = new AllTypesTestObject();
    DASJiraModelToRow jsonToRow = new DASJiraModelToRow(testObject, tableDefinition);
    Row row = jsonToRow.toRow(testObject);

    assertEquals("string", row.getDataMap().get("string_field").getString().getV());
    assertTrue(row.getDataMap().containsKey("int_field"));
    assertEquals(1, row.getDataMap().get("int_field").getInt().getV());
    assertTrue(row.getDataMap().containsKey("boolean_field"));
    assertTrue(row.getDataMap().get("boolean_field").getBool().getV());
    assertTrue(row.getDataMap().containsKey("list_field"));
    assertEquals(3, row.getDataMap().get("list_field").getList().getValuesCount());
    assertEquals("a", row.getDataMap().get("list_field").getList().getValues(0).getString().getV());
    assertEquals("b", row.getDataMap().get("list_field").getList().getValues(1).getString().getV());
    assertEquals("c", row.getDataMap().get("list_field").getList().getValues(2).getString().getV());
  }

  @Test
  @DisplayName("Model to row with column transformation")
  public void testModelWithTransformationToRow() {
    AllTypesTestObject testObject = new AllTypesTestObject();
    DASJiraModelToRow jsonToRow =
        new DASJiraModelToRow(testObject, tableDefinition, Map.of("string_field", "StringField"));
    Row row = jsonToRow.toRow(testObject);
    assertTrue(row.getDataMap().containsKey("StringField"));
    assertEquals("string", row.getDataMap().get("StringField").getString().getV());
  }
}
