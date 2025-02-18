package com.rawlabs.das.jira.utils.factory.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.protobuf.ByteString;
import com.rawlabs.protocol.das.v1.types.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

// TODO (AZ) test this
public abstract class ValueFactory {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @SuppressWarnings("unchecked")
  public final Value createValue(ValueTypeTuple valueTypeTuple) {
    return switch (valueTypeTuple.type()) {
      case Type t when t.hasString() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getString().getNullable(),
              () -> this.createString((String) valueTypeTuple.value()));
      case Type t when t.hasBool() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getBool().getNullable(),
              () -> this.createBool((boolean) valueTypeTuple.value()));
      case Type t when t.hasByte() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getByte().getNullable(),
              () -> this.createByte((byte) valueTypeTuple.value()));
      case Type t when t.hasShort() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getShort().getNullable(),
              () -> this.createShort((short) valueTypeTuple.value()));
      case Type t when t.hasInt() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getInt().getNullable(),
              () -> this.createInt((int) valueTypeTuple.value()));
      case Type t when t.hasLong() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getLong().getNullable(),
              () -> this.createLong((long) valueTypeTuple.value()));
      case Type t when t.hasFloat() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getFloat().getNullable(),
              () -> this.createFloat((float) valueTypeTuple.value()));
      case Type t when t.hasDouble() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getDouble().getNullable(),
              () -> this.createDouble((double) valueTypeTuple.value()));
      case Type t when t.hasDecimal() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getDecimal().getNullable(),
              () -> this.createDecimal((String) valueTypeTuple.value()));
      case Type t when t.hasBinary() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getBinary().getNullable(),
              () -> this.createBinary((byte[]) valueTypeTuple.value()));
      case Type t when t.hasAny() ->
          withNullCheck(
              valueTypeTuple.value(),
              true,
              () -> {
                try {
                  return this.createAny((String) valueTypeTuple.value());
                } catch (IOException e) {
                  throw new IllegalArgumentException();
                }
              });
      case Type t when t.hasDate() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getDate().getNullable(),
              () -> this.createDate((String) valueTypeTuple.value()));
      case Type t when t.hasTime() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getTime().getNullable(),
              () -> this.createTime((String) valueTypeTuple.value()));
      case Type t when t.hasTimestamp() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getTimestamp().getNullable(),
              () -> this.createTimestamp((String) valueTypeTuple.value()));
      case Type t when t.hasInterval() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getInterval().getNullable(),
              () -> this.createInterval((String) valueTypeTuple.value()));
      case Type t when t.hasRecord() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getRecord().getNullable(),
              () -> this.createRecordWithType(valueTypeTuple.value()));
      case Type t when t.hasList() ->
          withNullCheck(
              valueTypeTuple.value(),
              t.getList().getNullable(),
              () ->
                  this.createList(
                      (List<Object>) valueTypeTuple.value(), t.getList().getInnerType()));

      default -> throw new IllegalStateException("Unexpected value: " + valueTypeTuple.type());
    };
  }

  private Value withNullCheck(Object obj, boolean isNullable, Supplier<Value> supplier) {
    if (obj == null && !isNullable) {
      throw new IllegalArgumentException("non nullable value is null");
    }
    if (obj == null) {
      return createNull();
    }
    return supplier.get();
  }

  public Value createNull() {
    return Value.newBuilder().setNull(ValueNull.newBuilder().build()).build();
  }

  protected Value createString(String string) {
    return Value.newBuilder().setString(ValueString.newBuilder().setV(string).build()).build();
  }

  protected Value createBool(boolean bool) {
    return Value.newBuilder().setBool(ValueBool.newBuilder().setV(bool).build()).build();
  }

  protected Value createByte(byte byteValue) {
    return Value.newBuilder().setByte(ValueByte.newBuilder().setV(byteValue).build()).build();
  }

  protected Value createShort(short shortValue) {
    return Value.newBuilder().setShort(ValueShort.newBuilder().setV(shortValue).build()).build();
  }

  protected Value createInt(int intValue) {
    return Value.newBuilder().setInt(ValueInt.newBuilder().setV(intValue).build()).build();
  }

  protected Value createLong(long longValue) {
    return Value.newBuilder().setLong(ValueLong.newBuilder().setV(longValue).build()).build();
  }

  protected Value createFloat(float floatValue) {
    return Value.newBuilder().setFloat(ValueFloat.newBuilder().setV(floatValue).build()).build();
  }

  protected Value createDouble(double doubleValue) {
    return Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(doubleValue).build()).build();
  }

  protected Value createDecimal(String decimalValue) {
    return Value.newBuilder()
        .setDecimal(ValueDecimal.newBuilder().setV(decimalValue).build())
        .build();
  }

  protected Value createBinary(byte[] binaryValue) {
    return Value.newBuilder()
        .setBinary(ValueBinary.newBuilder().setV(ByteString.copyFrom(binaryValue)).build())
        .build();
  }

  protected Value createList(List<Object> list, Type type) {
    List<Value> listOfValues =
        list.stream().map(o -> createValue(new ValueTypeTuple(o, type))).toList();
    return Value.newBuilder().setList(ValueList.newBuilder().addAllValues(listOfValues)).build();
  }

  //  @SuppressWarnings("unchecked")
  //  protected Value createList(Object obj) {
  //    List<Object> list = (List<Object>) obj;
  //    List<Value> listOfValues = list.stream().map(this::createValue).toList();
  //    return
  // Value.newBuilder().setList(ValueList.newBuilder().addAllValues(listOfValues)).build();
  //  }

  protected Value createAny(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode node = objectMapper.readTree(json);
    return recurseAny(node);
  }

  private Value recurseAny(JsonNode jsonNode) throws IOException {
    return switch (jsonNode) {
      case JsonNode node when node.isNull() -> createNull();
      case JsonNode node when node.isTextual() -> createString(jsonNode.textValue());
      case JsonNode node when node.isBoolean() ->
          createValue(new ValueTypeTuple(jsonNode.asBoolean(), createBoolType()));
      case JsonNode node when node.isShort() ->
          createValue(new ValueTypeTuple(jsonNode.shortValue(), createShortType()));
      case JsonNode node when node.isInt() ->
          createValue(new ValueTypeTuple(jsonNode.intValue(), createIntType()));
      case JsonNode node when node.isLong() ->
          createValue(new ValueTypeTuple(jsonNode.longValue(), createLongType()));
      case JsonNode node when node.isFloat() ->
          createValue(new ValueTypeTuple(jsonNode.floatValue(), createFloatType()));
      case JsonNode node when node.isDouble() ->
          createValue(new ValueTypeTuple(jsonNode.doubleValue(), createDoubleType()));
      case JsonNode node when node.isBinary() ->
          createValue(new ValueTypeTuple(jsonNode.binaryValue(), createBinaryType()));
      case JsonNode node when node.isMissingNode() -> createNull();
      case JsonNode node when node.isBigDecimal() ->
          createValue(new ValueTypeTuple(jsonNode.decimalValue(), createDecimalType()));
      case JsonNode node when node.isBigInteger() ->
          createValue(new ValueTypeTuple(jsonNode.bigIntegerValue(), createDecimalType()));
      case JsonNode node when node.isObject() -> {
        ValueRecord.Builder rb = ValueRecord.newBuilder();
        node.fields()
            .forEachRemaining(
                entry -> {
                  try {
                    rb.addAtts(
                        ValueRecordAttr.newBuilder()
                            .setName(entry.getKey())
                            .setValue(recurseAny(entry.getValue())));
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                });

        yield Value.newBuilder().setRecord(rb.build()).build();
      }

      case JsonNode node when node.isArray() -> {
        ArrayNode arrayNode = (ArrayNode) node;
        ValueList.Builder rb = ValueList.newBuilder();
        arrayNode.forEach(
            jn -> {
              try {
                rb.addValues(recurseAny(jn));
              } catch (IOException e) {
                throw new IllegalArgumentException();
              }
            });
        yield Value.newBuilder().setList(rb.build()).build();
      }

      default -> throw new IllegalStateException("Unexpected value: " + jsonNode);
    };
  }

  protected Value createDate(String date) {
    DateTime dateTime = DateTime.parse(date);
    return Value.newBuilder()
        .setDate(
            ValueDate.newBuilder()
                .setDay(dateTime.getDayOfMonth())
                .setMonth(dateTime.getMonthOfYear())
                .setYear(dateTime.getYear())
                .build())
        .build();
  }

  protected Value createTime(String time) {
    DateTime dateTime = DateTime.parse(time);
    return Value.newBuilder()
        .setTime(
            ValueTime.newBuilder()
                .setHour(dateTime.getHourOfDay())
                .setMinute(dateTime.getMinuteOfHour())
                .setSecond(dateTime.getSecondOfDay())
                .setNano(dateTime.getMillisOfSecond() * 1_000_000)
                .build())
        .build();
  }

  protected Value createTimestamp(String timestamp) {
    DateTime dateTime = DateTime.parse(timestamp);
    return Value.newBuilder()
        .setTimestamp(
            ValueTimestamp.newBuilder()
                .setDay(dateTime.getDayOfMonth())
                .setMonth(dateTime.getMonthOfYear())
                .setYear(dateTime.getYear())
                .setHour(dateTime.getHourOfDay())
                .setMinute(dateTime.getMinuteOfHour())
                .setSecond(dateTime.getSecondOfMinute())
                .setNano(dateTime.getMillisOfSecond() * 1_000_000)
                .build())
        .build();
  }

  protected Value createInterval(String interval) {
    Interval i = Interval.parse(interval);
    return Value.newBuilder()
        .setInterval(
            ValueInterval.newBuilder()
                .setDays((int) i.toDuration().getStandardDays())
                .setHours((int) i.toDuration().getStandardHours())
                .setMinutes((int) i.toDuration().getStandardMinutes())
                .setSeconds((int) i.toDuration().getStandardSeconds())
                .setMicros((int) i.toDuration().getMillis() * 1000)
                .build())
        .build();
  }

  @SuppressWarnings("unchecked")
  protected Value createRecordWithType(Object value) {
    Map<String, ValueTypeTuple> recordMap = (Map<String, ValueTypeTuple>) value;
    ValueRecord.Builder rb = ValueRecord.newBuilder();
    List<String> fieldNames = new ArrayList<>(recordMap.keySet());
    for (int i = 0; i < recordMap.size(); i++) {
      rb.addAtts(
          ValueRecordAttr.newBuilder()
              .setName(fieldNames.get(i))
              .setValue(createValue(recordMap.get(fieldNames.get(i)))));
    }
    return Value.newBuilder().setRecord(rb.build()).build();
  }

}
