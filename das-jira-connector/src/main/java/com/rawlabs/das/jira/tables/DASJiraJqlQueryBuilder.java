package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ExtractValueFactory;
import com.rawlabs.protocol.das.Operator;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.raw.Value;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class DASJiraJqlQueryBuilder {

  private static final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();
  static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  static String mapOperator(Operator operator) {
    return switch (operator) {
      case Operator op when op.hasEquals() -> "=";
      case Operator op when op.hasNotEquals() -> "!=";
      case Operator op when op.hasGreaterThan() -> ">";
      case Operator op when op.hasGreaterThanOrEqual() -> ">=";
      case Operator op when op.hasLessThan() -> "<";
      case Operator op when op.hasLessThanOrEqual() -> "<=";
      default -> throw new IllegalArgumentException("Unexpected operator: " + operator);
    };
  }

  static String mapValue(Value value) {
    String s = switch (value) {
      case Value v when v.hasString() -> v.getString().getV();
      case Value v when v.hasTime() -> {
        OffsetTime time = (OffsetTime) extractValueFactory.extractValue(value);
        yield time.format(formatter);
      }
      case Value v when (v.hasTimestamp() || v.hasDate()) -> {
        OffsetDateTime time = (OffsetDateTime) extractValueFactory.extractValue(value);
        yield time.format(formatter);
      }
      default -> throw new IllegalArgumentException("Unexpected value: " + value);
    };
    return "\"" + s + "\"";
  }

  // The exposed column name needs to be mapped to the corresponding inner JQL key.
  // JIRA represents several fields as JSON objects, and we often expose multiple
  // nested fields from these objects as separate columns in the table output.
  // These columns are named using the pattern: original key + "_" + nested key.
  // From this naming convention, the JQL key can typically be inferred by taking
  // the first part of the column name.
  private static String getIssueJqlKey(String columnName) {
    // However, there are exceptions, such as 'status_category', which should not
    // be mapped to the JQL 'status' field, but to `statusCategory`. Exceptions are
    // found in jqlKeyMap.
    if (jqlKeyMap.containsKey(columnName)) {
      return jqlKeyMap.get(columnName);
    }
    return columnName.split("_")[0].toLowerCase();
  }

  private static final Map<String, String> jqlKeyMap =
      Map.of("status_category", "statusCategory", "epic_key", "parentEpic");

  public static String buildJqlQuery(List<Qual> quals) {
    StringBuilder jqlQuery = new StringBuilder();
    StringJoiner joiner = new StringJoiner(" AND ");
    quals.stream()
        .filter(Qual::hasSimpleQual)
        .forEach(
            q -> {
              String column = getIssueJqlKey(q.getFieldName());
              String operator = mapOperator(q.getSimpleQual().getOperator());
              String value = mapValue(q.getSimpleQual().getValue());
              joiner.add(column + " " + operator + " " + value);
            });
    jqlQuery.append(joiner);
    return jqlQuery.toString();
  }
}
