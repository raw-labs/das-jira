package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.Equals;
import com.rawlabs.protocol.das.GreaterThanOrEqual;
import com.rawlabs.protocol.das.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.*;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Jira JQL Query Builder")
public class DASJiraJqlQueryBuilderTest {

  private final ValueFactory valueFactory = new DefaultValueFactory();
  DASJiraJqlQueryBuilder builder = new DASJiraJqlQueryBuilder();

  @Test
  @DisplayName("Should map only string or temporal values")
  public void shouldMapValues() {

    var string =
        builder.mapValue(valueFactory.createValue(new ValueTypeTuple("DAS", createStringType())));

    var timestamp =
        builder.mapValue(
            valueFactory.createValue(
                new ValueTypeTuple("2021-01-01T00:00:00Z", createTimestampType())));

    assertEquals("DAS", string);
    assertEquals("2021-01-01 00:00", timestamp);
    assertThrows(
        IllegalArgumentException.class,
        () -> builder.mapValue(valueFactory.createValue(new ValueTypeTuple(1, createIntType()))));
  }

  @Test
  @DisplayName("Should map operators to JQL")
  public void shouldMapOperators() {

    var geq =
        builder.mapOperator(
            Operator.newBuilder()
                .setGreaterThanOrEqual(GreaterThanOrEqual.newBuilder().build())
                .build());

    var eq =
        builder.mapOperator(Operator.newBuilder().setEquals(Equals.newBuilder().build()).build());

    assertEquals(">=", geq);
    assertEquals("=", eq);
  }

  @Test
  @DisplayName("Should generate proper JQL queries")
  public void shouldGenerateJqlQuery() {
    String result =
        builder.buildJqlQuery(
            List.of(
                createEq(
                    valueFactory.createValue(new ValueTypeTuple("DAS", createStringType())),
                    "summary"),
                createGte(
                    valueFactory.createValue(
                        new ValueTypeTuple("2021-01-01T00:00:00Z", createTimestampType())),
                    "created_date"),
                createLte(
                    valueFactory.createValue(
                        new ValueTypeTuple("2021-01-01T00:00:00Z", createTimestampType())),
                    "due_date")));

    assertEquals(
        "summary = DAS AND created >= 2021-01-01 00:00 AND due <= 2021-01-01 00:00", result);
  }
}
