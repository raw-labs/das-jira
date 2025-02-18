package com.rawlabs.das.jira.utils.factory.type;

import com.rawlabs.protocol.das.v1.types.*;

import java.util.List;

public final class TypeFactory {
  public static Type createStringType(boolean nullable) {
    return Type.newBuilder()
        .setString(StringType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createStringType() {
    return createStringType(true);
  }

  public static Type createBoolType(boolean nullable) {
    return Type.newBuilder()
        .setBool(BoolType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createBoolType() {
    return createBoolType(true);
  }

  public static Type createByteType(boolean nullable) {
    return Type.newBuilder()
        .setByte(ByteType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createByteType() {
    return createByteType(true);
  }

  public static Type createShortType(boolean nullable) {
    return Type.newBuilder()
        .setShort(ShortType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createShortType() {
    return createShortType(true);
  }

  public static Type createIntType(boolean nullable) {
    return Type.newBuilder()
        .setInt(IntType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createIntType() {
    return createIntType(true);
  }

  public static Type createLongType(boolean nullable) {
    return Type.newBuilder()
        .setLong(LongType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createLongType() {
    return createLongType(true);
  }

  public static Type createFloatType(boolean nullable) {
    return Type.newBuilder()
        .setFloat(FloatType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createFloatType() {
    return createFloatType(true);
  }

  public static Type createDoubleType(boolean nullable) {
    return Type.newBuilder()
        .setDouble(DoubleType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createDoubleType() {
    return createDoubleType(true);
  }

  public static Type createDecimalType(boolean nullable) {
    return Type.newBuilder()
        .setDecimal(DecimalType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createDecimalType() {
    return createDecimalType(true);
  }

  public static Type createBinaryType(boolean nullable) {
    return Type.newBuilder()
        .setBinary(BinaryType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createBinaryType() {
    return createBinaryType(true);
  }

  public static Type createListType(Type innerType, boolean nullable) {
    return Type.newBuilder()
        .setList(
            ListType.newBuilder()
                .setInnerType(innerType)
                .setNullable(nullable)
                .build())
        .build();
  }

  public static Type createListType(Type innerType) {
    return createListType(innerType, true);
  }

  public static Type createDateType(boolean nullable) {
    return Type.newBuilder()
        .setDate(DateType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createDateType() {
    return createDateType(true);
  }

  public static Type createTimeType(boolean nullable) {
    return Type.newBuilder()
        .setTime(TimeType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createTimeType() {
    return createTimeType(true);
  }

  public static Type createTimestampType(boolean nullable) {
    return Type.newBuilder()
        .setTimestamp(TimestampType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createTimestampType() {
    return createTimestampType(true);
  }

  public static Type createIntervalType(boolean nullable) {
    return Type.newBuilder()
        .setInterval(IntervalType.newBuilder().setNullable(nullable).build())
        .build();
  }

  public static Type createIntervalType() {
    return createIntervalType(true);
  }

  public static Type createRecordType(boolean nullable, List<AttrType> atts) {
    var recordTypeBuilder = RecordType.newBuilder().setNullable(nullable);
    atts.forEach(recordTypeBuilder::addAtts);
    return Type.newBuilder().setRecord(recordTypeBuilder.build()).build();
  }

  public static Type createRecordType(List<AttrType> atts) {
    return createRecordType(true, atts);
  }

  public static AttrType createAttType(String name, Type type) {
    return AttrType.newBuilder().setName(name).setTipe(type).build();
  }
  
  public static Type createAnyType() {
    return Type.newBuilder().setAny(AnyType.newBuilder().build()).build();
  }
}
