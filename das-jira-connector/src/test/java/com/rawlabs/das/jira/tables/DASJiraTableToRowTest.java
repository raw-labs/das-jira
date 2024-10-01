package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DASJiraTableToRowTest {

  private static final DASJiraTableDefinition dasJiraTableDefinition =
      new DASJiraTableDefinition(
          "test_table",
          "Test Table",
          Map.of(
              "string_field",
                  new DASJiraColumnDefinition("string_field", "StringField", createStringType()),
              "int_field", new DASJiraColumnDefinition("int_field", "IntField", createIntType()),
              "boolean_field",
                  new DASJiraColumnDefinition("boolean_field", "booleanField", createBoolType()),
              "list_field",
                  new DASJiraColumnDefinition(
                      "list_field", "listField", createListType(createStringType()))));

  private static Row toRow(AllTypesTestObject obj) {
    Row.Builder rowBuilder = Row.newBuilder();
    dasJiraTableDefinition.updateRow("string_field", rowBuilder, obj.getStringField());
    dasJiraTableDefinition.updateRow("int_field", rowBuilder, obj.getIntField());
    dasJiraTableDefinition.updateRow("boolean_field", rowBuilder, obj.getBooleanField());
    dasJiraTableDefinition.updateRow("list_field", rowBuilder, obj.getListField());
    return rowBuilder.build();
  }

  @Test
  @DisplayName("Model to row")
  public void testModelToRow() {
    AllTypesTestObject testObject = new AllTypesTestObject();
    Row row = toRow(testObject);

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
}
