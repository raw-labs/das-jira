package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.TableDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static org.junit.jupiter.api.Assertions.*;

public class DASJiraTableDefinitionTest {

  private static final DASJiraTableDefinition<AllTypesTestObject1> dasJiraTableDefinition =
      new DASJiraTableDefinition<>(
          "test_table",
          "Test Table",
          List.of(
              new DASJiraParentColumnDefinition<>(
                  "string_field1",
                  "StringField1",
                  createStringType(),
                  AllTypesTestObject1::getStringField,
                  new DASJiraTableDefinition<>(
                      "test_table2",
                      "Test Table 2",
                      List.of(
                          new DASJiraNormalColumnDefinition<>(
                              "string_field2",
                              "StringField",
                              createStringType(),
                              AllTypesTestObject2::getStringField),
                          new DASJiraNormalColumnDefinition<>(
                              "int_field2",
                              "IntField",
                              createIntType(),
                              AllTypesTestObject2::getIntField),
                          new DASJiraNormalColumnDefinition<>(
                              "boolean_field2",
                              "booleanField",
                              createBoolType(),
                              AllTypesTestObject2::getBooleanField),
                          new DASJiraNormalColumnDefinition<>(
                              "list_field2",
                              "listField",
                              createListType(createStringType()),
                              AllTypesTestObject2::getListField)),
                      (_, _, _, _) -> List.of(new AllTypesTestObject2()).iterator())),
              new DASJiraNormalColumnDefinition<>(
                  "int_field1", "IntField1", createIntType(), AllTypesTestObject1::getIntField),
              new DASJiraNormalColumnDefinition<>(
                  "boolean_field1",
                  "booleanField",
                  createBoolType(),
                  AllTypesTestObject1::getBooleanField),
              new DASJiraNormalColumnDefinition<>(
                  "list_field1",
                  "listField1",
                  createListType(createStringType()),
                  AllTypesTestObject1::getListField)),
          (_, _, _, _) -> List.of(new AllTypesTestObject1()).iterator());

  @Test
  @DisplayName("Table Definition test")
  public void testTableDefinition() {
    TableDefinition tableDefinition = dasJiraTableDefinition.getTableDefinition();
    List<ColumnDefinition> columns = tableDefinition.getColumnsList();
    assertEquals(columns.size(), 8);
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("string_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("int_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("boolean_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("list_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("string_field2")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("int_field2")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("boolean_field2")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("list_field2")));
  }

  @Test
  @DisplayName("Execute nested table test")
  public void testModelToRow() {

    try (DASExecuteResult result = dasJiraTableDefinition.execute(null, null, null, null)) {
      Row row = result.next();

      assertEquals("string1", row.getDataMap().get("string_field").getString().getV());
      assertTrue(row.getDataMap().containsKey("int_field"));
      assertEquals(1, row.getDataMap().get("int_field").getInt().getV());
      assertTrue(row.getDataMap().containsKey("boolean_field"));
      assertTrue(row.getDataMap().get("boolean_field").getBool().getV());
      assertTrue(row.getDataMap().containsKey("list_field"));
      assertEquals(3, row.getDataMap().get("list_field").getList().getValuesCount());
      assertEquals(
          "a1", row.getDataMap().get("list_field").getList().getValues(0).getString().getV());
      assertEquals(
          "b1", row.getDataMap().get("list_field").getList().getValues(1).getString().getV());
      assertEquals(
          "c1", row.getDataMap().get("list_field").getList().getValues(2).getString().getV());

      assertEquals("string2", row.getDataMap().get("string_field2").getString().getV());

    } catch (IOException e) {
      fail("Should not throw exception: " + e.getMessage());
    }
  }
}
