package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.utils.TableFactory;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.TypeFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DASJiraModelToRowTest {

  private static final TableDefinition tableDefinition =
      TableFactory.createTable(
          "test_table",
          "Test table",
          List.of(
              createColumn("string_field", "StringField", createStringType()),
              createColumn("int_field", "IntField", createIntType()),
              createColumn("boolean_field", "booleanField", createBoolType()),
              createColumn("list_field", "listField", createListType(createStringType()))));

  @Test
  @DisplayName("Model to row without column transformation")
  public void testModelWithoutTransformationToRow() {
    AllTypesTestObject testObject = new AllTypesTestObject();
    DASJiraModelToRow jsonToRow = new DASJiraModelToRow(AllTypesTestObject.class, tableDefinition);
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
        new DASJiraModelToRow(
            AllTypesTestObject.class, tableDefinition, Map.of("string_field", "StringField"));
    Row row = jsonToRow.toRow(testObject);
    assertTrue(row.getDataMap().containsKey("StringField"));
    assertEquals("string", row.getDataMap().get("StringField").getString().getV());
  }
}