package com.rawlabs.das.jira.tables;

import com.rawlabs.protocol.das.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DASJiraTableToRowTest {

  private static final DASJiraTableSubDefinition<AllTypesTestObject> dasJiraTableDefinition =
      new DASJiraTableSubDefinition<>(
          "test_table",
          "Test Table",
          List.of(
              new DASJiraColumnDefinition<>(
                  "string_field",
                  "StringField",
                  createStringType(),
                  AllTypesTestObject::getStringField),
              new DASJiraColumnDefinition<>(
                  "int_field", "IntField", createIntType(), AllTypesTestObject::getIntField),
              new DASJiraColumnDefinition<>(
                  "boolean_field",
                  "booleanField",
                  createBoolType(),
                  AllTypesTestObject::getBooleanField),
              new DASJiraColumnDefinition<>(
                  "list_field",
                  "listField",
                  createListType(createStringType()),
                  AllTypesTestObject::getListField)),
          List.of(
              new DasJiraProvidedValueColumnDefinition(
                  "provided_value", "ProvidedValue", createStringType())));

  @Test
  @DisplayName("Model to row")
  public void testModelToRow() {
    AllTypesTestObject testObject = new AllTypesTestObject();
    Row row = dasJiraTableDefinition.getRow(testObject, Map.of("provided_value", "provided"));

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
    assertTrue(row.getDataMap().containsKey("provided_value"));
    assertEquals("provided", row.getDataMap().get("provided_value").getString().getV());
  }
}
