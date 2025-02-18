package com.rawlabs.das.jira.utils.types;

import com.rawlabs.das.jira.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.v1.types.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.createListType;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.createStringType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Value Factory Tests")
public class ValueFactoryTest {

  @Test
  @DisplayName("Test create string value")
  void testCreateStringValue() {
    ValueFactory factory = new DefaultValueFactory();
    Value v = factory.createValue(new ValueTypeTuple("test", createStringType()));
    assertEquals("test", v.getString().getV());
  }

  @Test
  @DisplayName("Test create null value")
  void testCreateNullValue() {
    ValueFactory factory = new DefaultValueFactory();
    Value v = factory.createValue(new ValueTypeTuple(null, createStringType()));
    assertTrue(v.hasNull());
  }

  @Test
  @DisplayName("Test create list value")
  void testCreateListValue() {
    List<List<String>> list = List.of(List.of("a", "b", "c"));
    ValueFactory factory = new DefaultValueFactory();
    Value v =
        factory.createValue(
            new ValueTypeTuple(list, createListType(createListType(createStringType()))));
    assertTrue(v.hasList());
    assertEquals(1, v.getList().getValuesCount());
    assertEquals(3, v.getList().getValues(0).getList().getValuesCount());
  }
}
