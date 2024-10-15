package com.rawlabs.das.jira.tables.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.ModelConfiguration;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.RowsEstimation;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueTypeTuple;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraGlobalSettingTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_global_setting";

  private JiraSettingsApi jiraSettingsApi = new JiraSettingsApi();

  public DASJiraGlobalSettingTable(Map<String, String> options) {
    super(options, TABLE_NAME, "Returns the global settings in Jira.");
  }

  /** Constructor for mocks */
  DASJiraGlobalSettingTable(Map<String, String> options, JiraSettingsApi jiraSettingsApi) {
    this(options);
    this.jiraSettingsApi = jiraSettingsApi;
  }

  // the result is always one, arbitrary putting the first column
  @Override
  public String getUniqueColumn() {
    return "voting_enabled";
  }

  @Override
  public RowsEstimation getRelSize(List<Qual> quals, List<String> columns) {
    return new RowsEstimation(1, 50);
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      ModelConfiguration config = jiraSettingsApi.getConfiguration();
      Iterator<Row> iterator = List.of(toRow(config)).iterator();
      return fromRowIterator(iterator);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  private Row toRow(ModelConfiguration configuration) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("voting_enabled", rowBuilder, configuration.getVotingEnabled());
    addToRow("watching_enabled", rowBuilder, configuration.getWatchingEnabled());
    addToRow("unassigned_issues_allowed", rowBuilder, configuration.getUnassignedIssuesAllowed());
    addToRow("sub_tasks_enabled", rowBuilder, configuration.getSubTasksEnabled());
    addToRow("issue_linking_enabled", rowBuilder, configuration.getIssueLinkingEnabled());
    addToRow("time_tracking_enabled", rowBuilder, configuration.getTimeTrackingEnabled());
    addToRow("attachments_enabled", rowBuilder, configuration.getAttachmentsEnabled());
    Optional.ofNullable(configuration.getTimeTrackingConfiguration())
        .ifPresent(
            timeTrackingConfiguration -> {
              try {
                addToRow(
                    "time_tracking_configuration",
                    rowBuilder,
                    objectMapper.writeValueAsString(timeTrackingConfiguration));
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            });
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
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
