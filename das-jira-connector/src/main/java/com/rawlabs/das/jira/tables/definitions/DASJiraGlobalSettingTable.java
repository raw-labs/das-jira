package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.ModelConfiguration;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraGlobalSettingTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_global_setting";

  private final JiraSettingsApi jiraSettingsApi;

  public DASJiraGlobalSettingTable(Map<String, String> options, JiraSettingsApi jiraSettingsApi) {
    super(options, TABLE_NAME, "Returns the global settings in Jira.");
    this.jiraSettingsApi = jiraSettingsApi;
  }

  // the result is always one, arbitrary putting the first column
  public String uniqueColumn() {
    return "voting_enabled";
  }

  public TableEstimate getTableEstimate(List<Qual> quals, List<String> columns) {
    return new TableEstimate(1, 50);
  }

  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    try {
      ModelConfiguration config = jiraSettingsApi.getConfiguration();
      Iterator<Row> iterator = List.of(toRow(config, columns)).iterator();
      return fromRowIterator(iterator);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  private Row toRow(ModelConfiguration configuration, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("voting_enabled", rowBuilder, configuration.getVotingEnabled(), columns);
    addToRow("watching_enabled", rowBuilder, configuration.getWatchingEnabled(), columns);
    addToRow(
        "unassigned_issues_allowed",
        rowBuilder,
        configuration.getUnassignedIssuesAllowed(),
        columns);
    addToRow("sub_tasks_enabled", rowBuilder, configuration.getSubTasksEnabled(), columns);
    addToRow("issue_linking_enabled", rowBuilder, configuration.getIssueLinkingEnabled(), columns);
    addToRow("time_tracking_enabled", rowBuilder, configuration.getTimeTrackingEnabled(), columns);
    addToRow("attachments_enabled", rowBuilder, configuration.getAttachmentsEnabled(), columns);
    var maybeTimeTrackingConfiguration =
        Optional.ofNullable(configuration.getTimeTrackingConfiguration());

    addToRow(
        "time_tracking_configuration",
        rowBuilder,
        maybeTimeTrackingConfiguration
            .map(
                c -> {
                  try {
                    return objectMapper.writeValueAsString(c);
                  } catch (JsonProcessingException e) {
                    throw new DASSdkException(e.getMessage(), e);
                  }
                })
            .orElse(null),
        columns);

    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
    columns.put(
        "voting_enabled",
        createColumn(
            "voting_enabled",
            "Whether the ability for users to vote on issues is enabled.",
            createBoolType()));
    columns.put(
        "watching_enabled",
        createColumn(
            "watching_enabled",
            "Whether the ability for users to watch issues is enabled.",
            createBoolType()));
    columns.put(
        "unassigned_issues_allowed",
        createColumn(
            "unassigned_issues_allowed",
            "Whether the ability to create unassigned issues is enabled.",
            createBoolType()));
    columns.put(
        "sub_tasks_enabled",
        createColumn(
            "sub_tasks_enabled",
            "Whether the ability to create subtasks for issues is enabled.",
            createBoolType()));
    columns.put(
        "issue_linking_enabled",
        createColumn(
            "issue_linking_enabled",
            "Whether the ability to link issues is enabled.",
            createBoolType()));
    columns.put(
        "time_tracking_enabled",
        createColumn(
            "time_tracking_enabled",
            "Whether the ability to track time is enabled.",
            createBoolType()));
    columns.put(
        "attachments_enabled",
        createColumn(
            "attachments_enabled",
            "Whether the ability to add attachments to issues is enabled.",
            createBoolType()));
    columns.put(
        "time_tracking_configuration",
        createColumn(
            "time_tracking_configuration", "The configuration of time tracking.", createAnyType()));
    return columns;
  }
}
