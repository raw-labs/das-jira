package com.rawlabs.das.jira.utils.factory.table;

import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.types.Type;

public final class ColumnFactory {
  public static ColumnDefinition createColumn(String name, String description, Type type) {
    return ColumnDefinition.newBuilder()
        .setName(name)
        .setDescription(description)
        .setType(type)
        .build();
  }
}
