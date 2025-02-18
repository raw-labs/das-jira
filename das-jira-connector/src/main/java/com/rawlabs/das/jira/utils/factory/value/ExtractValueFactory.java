package com.rawlabs.das.jira.utils.factory.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import com.rawlabs.protocol.das.v1.types.ValueDate;
import com.rawlabs.protocol.das.v1.types.ValueTime;
import com.rawlabs.protocol.das.v1.types.ValueTimestamp;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public abstract class ExtractValueFactory {

    ObjectMapper mapper = new ObjectMapper();

    public final Object extractValue(Value value) {
        return switch (value) {
            case Value v when v.hasNull() -> null;
            case Value v when v.hasString() -> v.getString().getV();
            case Value v when v.hasBool() -> v.getBool().getV();
            case Value v when v.hasByte() -> v.getByte().getV();
            case Value v when v.hasShort() -> v.getShort().getV();
            case Value v when v.hasInt() -> v.getInt().getV();
            case Value v when v.hasLong() -> v.getLong().getV();
            case Value v when v.hasFloat() -> v.getFloat().getV();
            case Value v when v.hasDouble() -> v.getDouble().getV();
            case Value v when v.hasDecimal() -> v.getDecimal().getV();
            case Value v when v.hasBinary() -> v.getBinary().getV().toByteArray();
            case Value v when v.hasDate() -> getDate(v);
            case Value v when v.hasTime() -> getTime(v);
            case Value v when v.hasTimestamp() -> getTimestamp(v);
            case Value v when v.hasInterval() -> getInterval(v);
            case Value v when v.hasRecord() -> getRecord(v);
            case Value v when v.hasList() -> getList(v);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public final Object extractValue(Row row, String columnName) {
        Value value = row.getColumnsList().stream().filter(c -> c.getName().equals(columnName)).findFirst().get().getData();
        return extractValue(value);
    }

    protected Object getList(Value v) {
        List<Value> list = v.getList().getValuesList();
        List<Object> result = new ArrayList<>(list.size());
        for (Value value : list) {
            result.add(extractValue(value));
        }
        return result;
    }

    protected Object getRecord(Value v) {
        return recurseRecord(v);
    }

    protected JsonNode recurseRecord(Value value) {
        return switch (value) {
            case Value v when v.hasNull() -> NullNode.getInstance();
            case Value v when v.hasString() -> mapper.valueToTree(v.getString().getV());
            case Value v when v.hasBool() -> mapper.valueToTree(v.getBool().getV());
            case Value v when v.hasByte() -> mapper.valueToTree(v.getByte().getV());
            case Value v when v.hasShort() -> mapper.valueToTree(v.getShort().getV());
            case Value v when v.hasInt() -> mapper.valueToTree(v.getInt().getV());
            case Value v when v.hasLong() -> mapper.valueToTree(v.getLong().getV());
            case Value v when v.hasFloat() -> mapper.valueToTree(v.getFloat().getV());
            case Value v when v.hasDouble() -> mapper.valueToTree(v.getDouble().getV());
            case Value v when v.hasDecimal() -> mapper.valueToTree(v.getDecimal().getV());
            case Value v when v.hasBinary() -> mapper.valueToTree(v.getBinary().getV().toByteArray());
            case Value v when v.hasDate() -> mapper.valueToTree(getDate(v));
            case Value v when v.hasTime() -> mapper.valueToTree(getTime(v));
            case Value v when v.hasTimestamp() -> mapper.valueToTree(getTimestamp(v));
            case Value v when v.hasInterval() -> mapper.valueToTree(getInterval(v));
            case Value v when v.hasRecord() -> {
                ObjectNode objectNode = mapper.createObjectNode();
                v.getRecord()
                        .getAttsList()
                        .forEach(f -> objectNode.set(f.getName(), recurseRecord(f.getValue())));
                yield objectNode;
            }
            case Value v when v.hasList() -> {
                List<Value> list = v.getList().getValuesList();
                List<JsonNode> result = new ArrayList<>(list.size());
                for (Value vl : list) {
                    result.add(recurseRecord(vl));
                }
                yield mapper.valueToTree(result);
            }
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    protected Object getDate(Value v) {
        ValueDate date = v.getDate();
        return OffsetDateTime.of(
                date.getYear(), date.getMonth(), date.getDay(), 0, 0, 0, 0, ZoneOffset.ofTotalSeconds(0));
    }

    protected Object getTime(Value v) {
        ValueTime time = v.getTime();

        return OffsetTime.of(
                time.getHour(),
                time.getMinute(),
                time.getSecond(),
                time.getNano(),
                ZoneOffset.ofTotalSeconds(0));
    }

    protected Object getTimestamp(Value v) {
        ValueTimestamp timestamp = v.getTimestamp();
        return OffsetDateTime.of(
                timestamp.getYear(),
                timestamp.getMonth(),
                timestamp.getDay(),
                timestamp.getHour(),
                timestamp.getMinute(),
                timestamp.getSecond(),
                timestamp.getNano(),
                ZoneOffset.ofTotalSeconds(0));
    }

    protected Object getInterval(Value v) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
