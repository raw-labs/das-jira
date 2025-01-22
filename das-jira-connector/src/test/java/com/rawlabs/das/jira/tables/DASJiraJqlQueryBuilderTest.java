package com.rawlabs.das.jira.tables;

import static com.rawlabs.das.sdk.java.utils.factory.qual.QualFactory.*;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.v1.query.Operator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Jira JQL Query Builder")
public class DASJiraJqlQueryBuilderTest {

  private final ValueFactory valueFactory = new DefaultValueFactory();

  @Test
  @DisplayName("Should map only string or temporal values")
  public void shouldMapValues() {

    var string =
        DASJiraJqlQueryBuilder.mapValue(
            valueFactory.createValue(new ValueTypeTuple("DAS", createStringType())));

    var timestamp =
        DASJiraJqlQueryBuilder.mapValue(
            valueFactory.createValue(
                new ValueTypeTuple("2021-01-01T00:00:00Z", createTimestampType())));

    assertEquals("\"DAS\"", string);
    assertEquals("\"2021-01-01 00:00\"", timestamp);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            DASJiraJqlQueryBuilder.mapValue(
                valueFactory.createValue(new ValueTypeTuple(1, createIntType()))));
  }

  @Test
  @DisplayName("Should map operators to JQL")
  public void shouldMapOperators() {

    var geq =
        DASJiraJqlQueryBuilder.mapOperator(Operator.GREATER_THAN_OR_EQUAL);

    var eq = DASJiraJqlQueryBuilder.mapOperator(Operator.EQUALS);

    assertEquals(">=", geq);
    assertEquals("=", eq);
  }

  @Test
  @DisplayName("Should generate proper JQL queries")
  public void shouldGenerateJqlQuery() {
    String result =
        DASJiraJqlQueryBuilder.buildJqlQuery(
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
        "summary = \"DAS\" AND created >= \"2021-01-01 00:00\" AND due <= \"2021-01-01 00:00\"", result);
  }
}
