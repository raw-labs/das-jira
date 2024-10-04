package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ExtractValueFactory;
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

  private static DASJiraTableDefinitionOld<AllTypesTestObject> buildTable(boolean isFlat) {
    return new DASJiraTableDefinitionOld<>(
        "test_table",
        "Test Table",
        List.of(
            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                "int_field1", "IntField1", createIntType(), AllTypesTestObject::getIntField),
            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                "boolean_field1",
                "booleanField",
                createBoolType(),
                AllTypesTestObject::getBooleanField),
            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                "list_field1",
                "listField1",
                createListType(createStringType()),
                AllTypesTestObject::getListField)),
        new DASJiraColumnDefinitionWithChildrenOld<>(
            "string_field1",
            "StringField1",
            createStringType(),
            AllTypesTestObject::getStringField,
            new DASJiraTableDefinitionOld<>(
                "test_table2",
                "Test Table 2",
                List.of(
                    new DASJiraColumnDefinitionWithoutChildrenOld<>(
                        "int_field2", "IntField", createIntType(), AllTypesTestObject::getIntField),
                    new DASJiraColumnDefinitionWithoutChildrenOld<>(
                        "boolean_field2",
                        "booleanField",
                        createBoolType(),
                        AllTypesTestObject::getBooleanField),
                    new DASJiraColumnDefinitionWithoutChildrenOld<>(
                        "list_field2",
                        "listField",
                        createListType(createStringType()),
                        AllTypesTestObject::getListField)),
                new DASJiraColumnDefinitionWithChildrenOld<>(
                    "string_field2",
                    "StringField3",
                    createStringType(),
                    AllTypesTestObject::getStringField,
                    new DASJiraTableDefinitionOld<>(
                        "test_table3",
                        "Test Table 3",
                        List.of(
                            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                                "string_field3",
                                "StringField",
                                createStringType(),
                                AllTypesTestObject::getStringField),
                            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                                "int_field3",
                                "IntField",
                                createIntType(),
                                AllTypesTestObject::getIntField),
                            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                                "boolean_field3",
                                "booleanField",
                                createBoolType(),
                                AllTypesTestObject::getBooleanField),
                            new DASJiraColumnDefinitionWithoutChildrenOld<>(
                                "list_field3",
                                "listField",
                                createListType(createStringType()),
                                AllTypesTestObject::getListField)),
                        (_, _, _, _) ->
                            isFlat
                                ? List.of(new AllTypesTestObject(3)).iterator()
                                : List.of(new AllTypesTestObject(5), new AllTypesTestObject(6))
                                    .iterator())),
                (_, _, _, _) ->
                    isFlat
                        ? List.of(new AllTypesTestObject(2)).iterator()
                        : List.of(new AllTypesTestObject(3), new AllTypesTestObject(4))
                            .iterator())),
        (_, _, _, _) ->
            isFlat
                ? List.of(new AllTypesTestObject(1)).iterator()
                : List.of(new AllTypesTestObject(1), new AllTypesTestObject(2)).iterator());
  }

  @Test
  @DisplayName("Table Definition test")
  public void testTableDefinition() {
    var dasJiraTableDefinition = buildTable(true);
    TableDefinition tableDefinition = dasJiraTableDefinition.getTableDefinition();
    List<ColumnDefinition> columns = tableDefinition.getColumnsList();
    assertEquals(columns.size(), 12);
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("string_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("int_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("boolean_field1")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("list_field1")));

    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("string_field2")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("int_field2")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("boolean_field2")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("list_field2")));

    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("string_field3")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("int_field3")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("boolean_field3")));
    assertTrue(columns.stream().anyMatch(column -> column.getName().equals("list_field3")));
  }

  @Test
  @DisplayName("Execute nested table test")
  public void testModelToFlatRow() {

    var dasJiraTableDefinition = buildTable(true);

    try (DASExecuteResult result = dasJiraTableDefinition.execute(null, null, null, null)) {
      Row row = result.next();

      assertEquals("string1", row.getDataMap().get("string_field1").getString().getV());
      assertTrue(row.getDataMap().containsKey("int_field1"));
      assertEquals(1, row.getDataMap().get("int_field1").getInt().getV());
      assertTrue(row.getDataMap().containsKey("boolean_field1"));
      assertTrue(row.getDataMap().get("boolean_field1").getBool().getV());
      assertTrue(row.getDataMap().containsKey("list_field1"));
      assertEquals(3, row.getDataMap().get("list_field1").getList().getValuesCount());
      assertEquals(
          "a1", row.getDataMap().get("list_field1").getList().getValues(0).getString().getV());
      assertEquals(
          "b1", row.getDataMap().get("list_field1").getList().getValues(1).getString().getV());
      assertEquals(
          "c1", row.getDataMap().get("list_field1").getList().getValues(2).getString().getV());

      assertEquals("string2", row.getDataMap().get("string_field2").getString().getV());
      assertTrue(row.getDataMap().containsKey("int_field2"));
      assertEquals(2, row.getDataMap().get("int_field2").getInt().getV());
      assertTrue(row.getDataMap().containsKey("boolean_field2"));
      assertTrue(row.getDataMap().get("boolean_field2").getBool().getV());
      assertTrue(row.getDataMap().containsKey("list_field2"));
      assertEquals(3, row.getDataMap().get("list_field2").getList().getValuesCount());
      assertEquals(
          "a2", row.getDataMap().get("list_field2").getList().getValues(0).getString().getV());
      assertEquals(
          "b2", row.getDataMap().get("list_field2").getList().getValues(1).getString().getV());
      assertEquals(
          "c2", row.getDataMap().get("list_field2").getList().getValues(2).getString().getV());

      assertEquals("string3", row.getDataMap().get("string_field3").getString().getV());
      assertTrue(row.getDataMap().containsKey("int_field3"));
      assertEquals(3, row.getDataMap().get("int_field3").getInt().getV());
      assertTrue(row.getDataMap().containsKey("boolean_field3"));
      assertTrue(row.getDataMap().get("boolean_field3").getBool().getV());
      assertTrue(row.getDataMap().containsKey("list_field3"));
      assertEquals(3, row.getDataMap().get("list_field3").getList().getValuesCount());
      assertEquals(
          "a3", row.getDataMap().get("list_field3").getList().getValues(0).getString().getV());
      assertEquals(
          "b3", row.getDataMap().get("list_field3").getList().getValues(1).getString().getV());
      assertEquals(
          "c3", row.getDataMap().get("list_field3").getList().getValues(2).getString().getV());
      assert (!result.hasNext());

    } catch (IOException e) {
      fail("Should not throw exception: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Execute nested table test")
  public void testModelToNestedRow() {
    var dasJiraTableDefinition = buildTable(false);
    try (DASExecuteResult result = dasJiraTableDefinition.execute(null, null, null, null)) {
      ExtractValueFactory valueFactory = new DefaultExtractValueFactory();
      while (result.hasNext()) {
        Row row = result.next();
        StringBuilder sb = new StringBuilder();
        row.getDataMap()
            .forEach(
                (key, value) ->
                    sb.append(key)
                        .append(": ")
                        .append(valueFactory.extractValue(value))
                        .append(", "));
        System.out.println(sb.append("\n").toString());
      }

    } catch (IOException e) {
      fail("Should not throw exception: " + e.getMessage());
    }
  }
}
