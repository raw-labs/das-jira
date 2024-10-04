//package com.rawlabs.das.jira.tables;
//
//import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
//import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
//import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
//import com.rawlabs.protocol.das.ColumnDefinition;
//import com.rawlabs.protocol.das.Row;
//
//import java.util.function.Function;
//
//public class DASJiraColumnDefinition {
//  public final ColumnDefinition columnDefinition;
//  public final Function<Object, Object> transformation;
//  private final ValueFactory valueFactory = new DefaultValueFactory();
//
//  public DASJiraColumnDefinition(
//      ColumnDefinition columnDefinition, Function<Object, Object> transformation) {
//    this.columnDefinition = columnDefinition;
//    this.transformation = transformation;
//  }
//
//  public void updateRow(Row.Builder rowBuilder, Object object) {
//    rowBuilder.putData(
//        columnDefinition.getName(),
//        valueFactory.createValue(new ValueTypeTuple(object, columnDefinition.getType())));
//  }
//
//  public ColumnDefinition getColumnDefinition() {
//    return columnDefinition;
//  }
//}
