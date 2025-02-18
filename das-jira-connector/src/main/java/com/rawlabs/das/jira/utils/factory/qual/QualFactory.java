package com.rawlabs.das.jira.utils.factory.qual;

import com.rawlabs.protocol.das.v1.query.Operator;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SimpleQual;
import com.rawlabs.protocol.das.v1.types.Value;

public final class QualFactory {
  public static Qual createEq(Value v, String fieldName) {
    return Qual.newBuilder()
        .setSimpleQual(
            SimpleQual.newBuilder()
                .setValue(v)
                .setOperator(Operator.EQUALS))
        .setName(fieldName)
        .build();
  }

  public static Qual createNeq(Value v, String fieldName) {
    return Qual.newBuilder()
        .setSimpleQual(
            SimpleQual.newBuilder()
                .setValue(v)
                .setOperator(Operator.NOT_EQUALS))
        .setName(fieldName)
        .build();
  }

  public static Qual createGt(Value v, String fieldName) {
    return Qual.newBuilder()
        .setSimpleQual(
            SimpleQual.newBuilder()
                .setValue(v)
                .setOperator(Operator.GREATER_THAN))
        .setName(fieldName)
        .build();
  }

  public static Qual createGte(Value v, String fieldName) {
    return Qual.newBuilder()
        .setSimpleQual(
            SimpleQual.newBuilder()
                .setValue(v)
                .setOperator(Operator.GREATER_THAN_OR_EQUAL))
        .setName(fieldName)
        .build();
  }

  public static Qual createLt(Value v, String fieldName) {
    return Qual.newBuilder()
        .setSimpleQual(
            SimpleQual.newBuilder()
                .setValue(v)
                .setOperator(Operator.LESS_THAN))
        .setName(fieldName)
        .build();
  }

  public static Qual createLte(Value v, String fieldName) {
    return Qual.newBuilder()
        .setSimpleQual(
            SimpleQual.newBuilder()
                .setValue(v)
                .setOperator(Operator.LESS_THAN_OR_EQUAL))
        .setName(fieldName)
        .build();
  }
}
