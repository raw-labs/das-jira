package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.ApplicationProperty;
import com.rawlabs.das.jira.rest.platform.model.SimpleApplicationPropertyBean;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.*;

import javax.annotation.Nullable;
import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEqDistinct;
import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraAdvancedSettingsTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_advanced_setting";
  private JiraSettingsApi jiraSettingsApi = new JiraSettingsApi();

  public DASJiraAdvancedSettingsTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "The application properties that are accessible on the Advanced Settings page.");
  }

  /** Constructor for mocks */
  DASJiraAdvancedSettingsTable(Map<String, String> options, JiraSettingsApi api) {
    this(options);
    this.jiraSettingsApi = api;
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("key"), 1));
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    String id = rowId.getString().getV();
    SimpleApplicationPropertyBean applicationPropertyBean = new SimpleApplicationPropertyBean();
    applicationPropertyBean.setId(id);
    applicationPropertyBean.setValue(newValues.getDataMap().get("value").getString().getV());
    try {
      ApplicationProperty applicationProperty =
          jiraSettingsApi.setApplicationProperty(id, applicationPropertyBean);
      return toRow(applicationProperty);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      String key = (String) extractEqDistinct(quals, "key");

      List<ApplicationProperty> maybeLimited;
      List<ApplicationProperty> result;
      if (key != null) {
        result = jiraSettingsApi.getApplicationProperty(key, null, null);
      } else {
        result = jiraSettingsApi.getAdvancedSettings();
      }
      maybeLimited =
          limit == null
              ? result
              : result.subList(0, Math.min(Math.toIntExact(limit), result.size()));

      return new DASExecuteResult() {
        private final Iterator<ApplicationProperty> iterator = maybeLimited.iterator();

        @Override
        public void close() {}

        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }

        @Override
        public Row next() {
          return toRow(iterator.next());
        }
      };
    } catch (ApiException e) {
      throw new DASSdkApiException(
          "Failed to fetch advanced settings: %s".formatted(e.getResponseBody()), e);
    }
  }

  private Row toRow(ApplicationProperty applicationProperty) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    this.addToRow("id", rowBuilder, applicationProperty.getId());
    this.addToRow("name", rowBuilder, applicationProperty.getName());
    this.addToRow("description", rowBuilder, applicationProperty.getDesc());
    this.addToRow("key", rowBuilder, applicationProperty.getKey());
    this.addToRow("type", rowBuilder, applicationProperty.getType());
    this.addToRow("value", rowBuilder, applicationProperty.getValue());
    this.addToRow("allowed_values", rowBuilder, applicationProperty.getAllowedValues());
    this.addToRow("title", rowBuilder, applicationProperty.getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    return Map.of(
        "id", createColumn("id", "The unique identifier of the property.", createStringType()),
        "name", createColumn("name", "The name of the application property.", createStringType()),
        "description",
            createColumn(
                "description", "The description of the application property.", createStringType()),
        "key", createColumn("key", "The key of the application property.", createStringType()),
        "type",
            createColumn("type", "The data type of the application property.", createStringType()),
        "value", createColumn("value", "The new value.", createStringType()),
        "allowed_values",
            createColumn(
                "allowed_values",
                "The allowed values, if applicable.",
                createListType(createStringType())),
        "title", createColumn("title", TITLE_DESC, createStringType()));
  }
}
