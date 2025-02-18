package com.rawlabs.das.jira.utils.factory.qual;

import com.rawlabs.das.jira.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ExtractValueFactory;
import com.rawlabs.protocol.das.v1.query.Operator;
import com.rawlabs.protocol.das.v1.query.Qual;

import java.util.List;
import java.util.function.Function;

public class ExtractQualFactory {

  protected static ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();

  private static List<Object> extract(
          List<Qual> quals, String fieldName, Function<Qual, Boolean> predicate) {
    return quals.stream()
        .filter(
            q ->
                q.hasSimpleQual()
                    && predicate.apply(q)
                    && q.getName().equals(fieldName))
        .map(qual -> extractValueFactory.extractValue(qual.getSimpleQual().getValue()))
        .toList();
  }

  private static Object extractDistinct(List<Object> qualsResult, String columnName) {
    if (qualsResult.size() > 1) {
      throw new IllegalArgumentException("Only one filter can be specified for %s".formatted(columnName));
    }
    return qualsResult.isEmpty() ? null : qualsResult.getFirst();
  }

  public static List<Object> extractEq(List<Qual> quals, String fieldName) {
    return extract(quals, fieldName, q -> q.getSimpleQual().getOperator() == Operator.EQUALS);
  }

  public static Object extractEqDistinct(List<Qual> quals, String fieldName) {
    return extractDistinct(extractEq(quals, fieldName), fieldName);
  }

  public static List<Object> extractNeq(List<Qual> quals, String fieldName) {
    return extract(quals, fieldName, q -> q.getSimpleQual().getOperator() == Operator.NOT_EQUALS);
  }

  public static Object extractNeqDistinct(List<Qual> quals, String fieldName) {
    return extractDistinct(extractNeq(quals, fieldName), fieldName);
  }

  public static List<Object> extractGt(List<Qual> quals, String fieldName) {
    return extract(quals, fieldName, q -> q.getSimpleQual().getOperator() == Operator.GREATER_THAN);
  }

  public static Object extractGtDistinct(List<Qual> quals, String fieldName) {
    return extractDistinct(extractGt(quals, fieldName), fieldName);
  }

  public static List<Object> extractGte(List<Qual> quals, String fieldName) {
    return extract(quals, fieldName, q -> q.getSimpleQual().getOperator() == Operator.GREATER_THAN_OR_EQUAL);
  }

  public static Object extractGteDistinct(List<Qual> quals, String fieldName) {
    return extractDistinct(extractGte(quals, fieldName), fieldName);
  }

  public static List<Object> extractLt(List<Qual> quals, String fieldName) {
    return extract(quals, fieldName, q -> q.getSimpleQual().getOperator() == Operator.LESS_THAN);
  }

  public static Object extractLtDistinct(List<Qual> quals, String fieldName) {
    return extractDistinct(extractLt(quals, fieldName), fieldName);
  }

  public static List<Object> extractLte(List<Qual> quals, String fieldName) {
    return extract(quals, fieldName, q -> q.getSimpleQual().getOperator() == Operator.LESS_THAN_OR_EQUAL);
  }

  public static Object extractLteDistinct(List<Qual> quals, String fieldName) {
    return extractDistinct(extractLte(quals, fieldName), fieldName);
  }
}
