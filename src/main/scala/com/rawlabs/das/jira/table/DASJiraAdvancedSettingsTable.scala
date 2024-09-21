/*
 * Copyright 2024 RAW Labs S.A.
 *
 * Use of this software is governed by the Business Source License
 * included in the file licenses/BSL.txt.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0, included in the file
 * licenses/APL.txt.
 */

package com.rawlabs.das.jira.table
import com.rawlabs.protocol.das.{ColumnDefinition, TableDefinition, TableId}
import com.rawlabs.protocol.raw.{BoolType, StringType, TimestampType, Type}

class DASJiraAdvancedSettingsTable extends DASJiraTable("jira_advanced_setting") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "The application properties that are accessible on the Advanced Settings page."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("The ID of the application property.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("name")
          .setDescription("The name of the application property.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("description")
          .setDescription("The description of the application property.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
        .addColumns(
            ColumnDefinition
            .newBuilder()
            .setName("key")
            .setDescription("The key of the application property.")
            .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
            .build()
        )
        .addColumns(
            ColumnDefinition
            .newBuilder()
            .setName("type")
            .setDescription("The data type of the application property.")
            .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
            .build()
        )
        .addColumns(
            ColumnDefinition
            .newBuilder()
            .setName("value")
            .setDescription("The new value.")
            .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
            .build()
        )
        .addColumns(
            ColumnDefinition
            .newBuilder()
            .setName("allowed_values")
            .setDescription("The allowed values, if applicable.")
            .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
            .build()
        )
        .addColumns(
            ColumnDefinition
            .newBuilder()
            .setName("title")
            .setDescription(ColumnDescriptionTitle)
            .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
            .build()
        )
      .setStartupCost(1000)
    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

//  Columns: commonColumns([]*plugin.Column{
//    // top fields
//    {
//      Name:        "id",
//      Description: "The ID of the application property.",
//      Type:        proto.ColumnType_STRING,
//      Transform:   transform.FromGo(),
//    },
//    {
//      Name:        "name",
//      Description: "The name of the application property.",
//      Type:        proto.ColumnType_STRING,
//    },
//    {
//      Name:        "description",
//      Description: "The description of the application property.",
//      Type:        proto.ColumnType_STRING,
//      Transform:   transform.FromField("Description"),
//    },
//
//    // other important fields
//    {
//      Name:        "key",
//      Description: "The key of the application property.",
//      Type:        proto.ColumnType_STRING,
//    },
//    {
//      Name:        "type",
//      Description: "The data type of the application property.",
//      Type:        proto.ColumnType_STRING,
//    },
//    {
//      Name:        "value",
//      Description: "The new value.",
//      Type:        proto.ColumnType_STRING,
//    },
//
//    // JSON fields
//    {
//      Name:        "allowed_values",
//      Description: "The allowed values, if applicable.",
//      Type:        proto.ColumnType_JSON,
//    },
//
//    // Standard columns
//    {
//      Name:        "title",
//      Description: ColumnDescriptionTitle,
//      Type:        proto.ColumnType_STRING,
//      Transform:   transform.FromField("Name"),
//    },
//  }),
}
