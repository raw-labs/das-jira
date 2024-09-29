//package com.rawlabs.das.jira.tables;
//
//import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
//import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
//import com.rawlabs.protocol.das.ColumnDefinition;
//import com.rawlabs.protocol.raw.Type;
//import com.rawlabs.protocol.raw.Value;
//
//import java.util.Map;
//
//import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
//
//public class DasJiraProvidedValueColumnDefinition {
//  private final Type type;
//  private final ColumnDefinition columnDefinition;
//  private final ValueFactory valueFactory = new DefaultValueFactory();
//
//  public DasJiraProvidedValueColumnDefinition(String name, String description, Type type) {
//    this.type = type;
//    columnDefinition = createColumn(name, description, type);
//  }
//
//  public Value getValue(Map<String, Object> values) {
//    return valueFactory.createValue(values.get(this.getName()), type);
//  }
//
//  public ColumnDefinition getColumnDefinition() {
//    return columnDefinition;
//  }
//
//  public String getName() {
//    return columnDefinition.getName();
//  }
//}
