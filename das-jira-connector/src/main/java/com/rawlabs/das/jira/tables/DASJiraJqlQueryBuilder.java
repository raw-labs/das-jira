package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ExtractValueFactory;
import com.rawlabs.protocol.das.v1.query.Operator;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.types.Value;
import com.rawlabs.protocol.das.v1.types.ValueTimestamp;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DASJiraJqlQueryBuilder {

  private final ZoneId localZoneId;
  private final ZoneId remoteZoneId;

  public DASJiraJqlQueryBuilder(ZoneId localZoneId, ZoneId remoteZoneId) {
    this.localZoneId = localZoneId;
    this.remoteZoneId = remoteZoneId;
  }

  private static final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();
  static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  String mapOperator(Operator operator) {
    return switch (operator) {
      case Operator.EQUALS -> "=";
      case Operator.NOT_EQUALS -> "!=";
      case Operator.GREATER_THAN -> ">";
      case Operator.GREATER_THAN_OR_EQUAL -> ">=";
      case Operator.LESS_THAN -> "<";
      case Operator.LESS_THAN_OR_EQUAL -> "<=";
      default -> throw new IllegalArgumentException("Unexpected operator: " + operator);
    };
  }

  String mapValue(Value value) {
    String s =
        switch (value) {
          case Value v when v.hasString() -> v.getString().getV();
          case Value v when v.hasTime() -> {
            OffsetTime time = (OffsetTime) extractValueFactory.extractValue(value);
            yield time.format(formatter);
          }
          case Value v when v.hasDate() -> {
            OffsetDateTime time = (OffsetDateTime) extractValueFactory.extractValue(value);
            yield time.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
          }
          case Value v when v.hasTimestamp() -> {
            // Without time zone, it's a local time, so we need to set its timezone to the
            // local timezone and then convert it to the remote timezone.
            ValueTimestamp rawTimestamp = v.getTimestamp();
            LocalDateTime time =
                LocalDateTime.of(
                    rawTimestamp.getYear(),
                    rawTimestamp.getMonth(),
                    rawTimestamp.getDay(),
                    rawTimestamp.getHour(),
                    rawTimestamp.getMinute(),
                    rawTimestamp.getSecond(),
                    rawTimestamp.getNano());
            ZonedDateTime dTime = time.atZone(localZoneId).withZoneSameInstant(remoteZoneId);
            yield dTime.format(formatter);
          }
          default -> throw new IllegalArgumentException("Unexpected value: " + value);
        };
    // If the string is all digits, it's a number and we don't need to quote it. This permits
    // to filter on 'id' without quotes, which is the required syntax. If it's not all digits, we
    // quote it, because it doesn't hurt, and it protects potential keywords.
    boolean allDigits = s.chars().allMatch(Character::isDigit);
    if (allDigits) {
      return s;
    } else {
      return "\"" + s.replace("\"", "\"\"") + "\"";
    }
  }

  // Tries to convert a list qual to a JQL string using IN or NOT IN.
  private Optional<String> maybeInCheck(Qual qual) {
    Operator rawOperator;
    List<Value> rawValues;
    if (qual.hasIsAnyQual()) {
      rawValues = qual.getIsAnyQual().getValuesList();
      rawOperator = qual.getIsAnyQual().getOperator();
    } else if (qual.hasIsAllQual()) {
      rawValues = qual.getIsAllQual().getValuesList();
      rawOperator = qual.getIsAllQual().getOperator();
    } else {
      return Optional.empty();
    }
    // If some values are null, we can't support this
    if (rawValues.stream().anyMatch(Value::hasNull)) {
      return Optional.empty();
    }
    List<String> values = new ArrayList<>();
    for (Value rawValue : rawValues) {
      values.add(mapValue(rawValue));
    }
    if (qual.hasIsAnyQual() && rawOperator == Operator.EQUALS) {
      // col EQUAL to ANY value IN (1,2,3) ==> IN
      StringJoiner joiner = new StringJoiner(", ", "IN (", ")");
      values.forEach(joiner::add);
      return Optional.of(joiner.toString());
    } else {
      if (qual.hasIsAllQual() && rawOperator == Operator.NOT_EQUALS) {
        // col NOT EQUAL to ALL values IN (1,2,3) ==> NOT IN
        StringJoiner joiner = new StringJoiner(", ", "NOT IN (", ")");
        values.forEach(joiner::add);
        return Optional.of(joiner.toString());
      } else {
        // We don't support other cases (ANY > 1 value, ALL > 1 value, etc.)
        return Optional.empty();
      }
    }
  }

  // Tries to convert an operator to a JQL string using IS NULL or IS NOT NULL. Basically only
  // equals and not equals are supported.
  private Optional<String> maybeNullCheck(Operator rawOperator) {
    if (rawOperator == Operator.EQUALS) {
      return Optional.of("IS NULL");
    } else if (rawOperator == Operator.NOT_EQUALS) {
      return Optional.of("IS NOT NULL");
    } else {
      return Optional.empty();
    }
  }

  // Tries to convert a list of quals to a list of JQL optional strings. If the qual is not
  // supported, the optional is empty. Returning a list of optionals allows us to know if all quals
  // are supported or not, which impacts whether or not one pushes LIMIT after.
  public List<Optional<String>> mkJql(List<Qual> quals) {
    List<Optional<String>> jqls = new ArrayList<>();
    for (Qual qual : quals) {
      String rawColumn = qual.getName();
      if (rawColumn.equals("id") || rawColumn.equals("key") || rawColumn.equals("title")) {
        if (qual.hasSimpleQual()) {
          Value rawValue = qual.getSimpleQual().getValue();
          // All simple comparisons are supported but not IS NULL OR IS NOT NULL
          if (rawValue.hasNull()) {
            jqls.add(Optional.empty());
            continue;
          }
          Operator rawOperator = qual.getSimpleQual().getOperator();
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "issueKey" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "issueKey " + s));
        }
      } else if (rawColumn.equals("self")) {
        // Not supported
        jqls.add(Optional.empty());
      } else if (rawColumn.equals("project_key")
          || rawColumn.equals("project_id")
          || rawColumn.equals("project_name")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "project " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "project" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "project " + s));
        }
      } else if (rawColumn.equals("status")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "status " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "status" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "status " + s));
        }
      } else if (rawColumn.equals("status_category")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "statusCategory " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "statusCategory" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "statusCategory " + s));
        }
      } else if (rawColumn.equals("epic_key")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // We have to filter on the parent field.
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "parent " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "parent" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "parent " + s));
        }
      } else if (rawColumn.equals("sprint_ids") || rawColumn.equals("sprint_names")) {
        // Not supported because the column is a list of values, and no predicate applies to that.
        jqls.add(Optional.empty());
      } else if (rawColumn.equals("assignee_account_id")
          || rawColumn.equals("assignee_email_address")
          || rawColumn.equals("assignee_display_name")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "assignee " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "assignee" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "assignee " + s));
        }
      } else if (rawColumn.equals("creator_account_id")
          || rawColumn.equals("creator_email_address")
          || rawColumn.equals("creator_display_name")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "creator " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "creator" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "creator " + s));
        }
      } else if (rawColumn.equals("created")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // All are supported. IS NULL and IS NOT NULL are supported.
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "created " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "created" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "created " + s));
        }
      } else if (rawColumn.equals("due_date")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // All are supported. IS NULL and IS NOT NULL are supported.
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "due " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "due" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "due " + s));
        }
      } else if (rawColumn.equals("description")) {
        // JSONB, not supported
        jqls.add(Optional.empty());
      } else if (rawColumn.equals("type")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          // IN is supported
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "type " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "type" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "type " + s));
        }
      } else if (rawColumn.equals("labels")) {
        // List, not supported
        jqls.add(Optional.empty());
      } else if (rawColumn.equals("priority")) {
        Operator rawOperator = qual.getSimpleQual().getOperator();
        // != and == are supported, not others because they aren't regular string comparisons.
        // IS NULL and IS NOT NULL are supported.
        // IN is supported
        if (qual.hasSimpleQual()) {
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "priority " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "priority" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "priority " + s));
        }
      } else if (rawColumn.equals("reporter_account_id")
          || rawColumn.equals("reporter_email_address")
          || rawColumn.equals("reporter_display_name")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // != and == are supported, not others. IS NULL and IS NOT NULL are supported.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "reporter " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "reporter" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "reporter " + s));
        }
      } else if (rawColumn.equals("summary")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // Only IS NULL and IS NOT NULL are supported. But we support = and != with ~ and !~
          // with Postgres applying the operator eventually.
          if (rawOperator != Operator.EQUALS && rawOperator != Operator.NOT_EQUALS) {
            jqls.add(Optional.empty());
            continue;
          }
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "summary " + p));
          } else if (rawValue.hasString()) {
            String value = rawValue.getString().getV();
            String escaped = value.replace("\"", "\\\"");
            if (rawOperator == Operator.EQUALS) {
              String jql = "summary ~ \"" + escaped + "\"";
              jqls.add(Optional.of(jql));
            } else {
              String jql = "summary !~ \"" + escaped + "\"";
              jqls.add(Optional.of(jql));
            }
          }
        } else {
          jqls.add(Optional.empty());
        }
      } else if (rawColumn.equals("updated")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // All are supported. IS NULL and IS NOT NULL are supported.
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "updated " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "updated" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "updated " + s));
        }
      } else if (rawColumn.equals("resolution_date")) {
        if (qual.hasSimpleQual()) {
          Operator rawOperator = qual.getSimpleQual().getOperator();
          // All are supported. IS NULL and IS NOT NULL are supported.
          Value rawValue = qual.getSimpleQual().getValue();
          if (rawValue.hasNull()) {
            jqls.add(maybeNullCheck(rawOperator).map(p -> "resolved " + p));
            continue;
          }
          String operator = mapOperator(rawOperator);
          String value = mapValue(rawValue);
          String jql = "resolved" + " " + operator + " " + value;
          jqls.add(Optional.of(jql));
        } else {
          // IN and NOT IN are supported
          Optional<String> maybeIn = maybeInCheck(qual);
          jqls.add(maybeIn.map(s -> "resolved " + s));
        }
      } else if (rawColumn.equals("components")) {
        // Not supported (it's a list)
        jqls.add(Optional.empty());
      } else if (rawColumn.equals("fields")) {
        // Not supported (JSONB)
        jqls.add(Optional.empty());
      } else if (rawColumn.equals("tags")) {
        // Not supported (JSONB)
        jqls.add(Optional.empty());
      } else {
        jqls.add(Optional.empty());
      }
    }
    return jqls;
  }

  public String buildJqlQuery(List<Qual> quals) {
    StringBuilder jqlQuery = new StringBuilder();
    StringJoiner joiner = new StringJoiner(" AND ");
    List<Optional<String>> jqls = mkJql(quals);
    for (Optional<String> jql : jqls) {
      jql.ifPresent(joiner::add);
    }
    jqlQuery.append(joiner);
    return jqlQuery.toString();
  }
}
