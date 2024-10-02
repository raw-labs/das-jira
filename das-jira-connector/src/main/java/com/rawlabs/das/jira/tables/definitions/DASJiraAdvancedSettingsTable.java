package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraColumnDefinition;
import com.rawlabs.das.jira.tables.DASJiraBaseTable;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.ApplicationProperty;
import com.rawlabs.das.jira.rest.platform.model.SimpleApplicationPropertyBean;
import com.rawlabs.das.jira.tables.DASJiraTableDefinition;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.*;

import javax.annotation.Nullable;
import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEq;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraAdvancedSettingsTable extends DASJiraBaseTable {

  public static final String TABLE_NAME = "jira_advanced_setting";
  private JiraSettingsApi api = new JiraSettingsApi();

  public DASJiraAdvancedSettingsTable(Map<String, String> options) {
    super(options);
  }

  /** Constructor for mocks */
  DASJiraAdvancedSettingsTable(Map<String, String> options, JiraSettingsApi api) {
    super(options);
    this.api = api;
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
  @SuppressWarnings("unchecked")
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    Iterator<ApplicationProperty> iterator =
        (Iterator<ApplicationProperty>)
            dasJiraAdvancedSettingsTableDefinition.hydrate(quals, columns, sortKeys, limit);
    return new DASExecuteResult() {
      @Override
      public void close() {}

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Row next() {
        try {
          Row.Builder rowBuilder = Row.newBuilder();
          dasJiraAdvancedSettingsTableDefinition.updateRow(rowBuilder, iterator.next());
          return rowBuilder.build();
        } catch (NoSuchElementException e) {
          throw new DASSdkException("Failed to fetch advanced settings", e);
        }
      }
    };
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    String id = rowId.getString().getV();
    SimpleApplicationPropertyBean applicationPropertyBean = new SimpleApplicationPropertyBean();
    applicationPropertyBean.setId(id);
    applicationPropertyBean.setValue(newValues.getDataMap().get("value").getString().getV());
    try {
      ApplicationProperty applicationProperty =
          api.setApplicationProperty(id, applicationPropertyBean);
      Row.Builder rowBuilder = Row.newBuilder();
      dasJiraAdvancedSettingsTableDefinition.updateRow(rowBuilder, applicationProperty);
      return rowBuilder.build();
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getTableName() {
    return dasJiraAdvancedSettingsTableDefinition.getTableDefinition().getTableId().getName();
  }

  @Override
  public TableDefinition getTableDefinition() {
    return dasJiraAdvancedSettingsTableDefinition.getTableDefinition();
  }

  public final DASJiraTableDefinition dasJiraAdvancedSettingsTableDefinition =
      new DASJiraTableDefinition(
          TABLE_NAME,
          "The application properties that are accessible on the Advanced Settings page.",
          List.of(
              new DASJiraColumnDefinition(
                  "id",
                  "The unique identifier of the property.",
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getId()),
              new DASJiraColumnDefinition(
                  "name",
                  "The name of the application property.",
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getName()),
              new DASJiraColumnDefinition(
                  "description",
                  "The description of the application property.",
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getDesc()),
              new DASJiraColumnDefinition(
                  "key",
                  "The key of the application property.",
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getKey()),
              new DASJiraColumnDefinition(
                  "type",
                  "The data type of the application property.",
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getType()),
              new DASJiraColumnDefinition(
                  "value",
                  "The new value.",
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getValue()),
              new DASJiraColumnDefinition(
                  "allowed_values",
                  "The allowed values, if applicable.",
                  createListType(createStringType()),
                  (Object ap) -> ((ApplicationProperty) ap).getAllowedValues()),
              new DASJiraColumnDefinition(
                  "title",
                  TITLE_DESC,
                  createStringType(),
                  (Object ap) -> ((ApplicationProperty) ap).getName())),
          (quals, columns, sortKeys, limit) -> {
            try {
              assert quals != null;
              String key = (String) extractEq(quals, "key");
              List<ApplicationProperty> result =
                  key == null
                      ? api.getAdvancedSettings()
                      : api.getApplicationProperty(key, null, null);

              List<ApplicationProperty> maybeLimited =
                  limit == null ? result : result.subList(0, Math.toIntExact(limit));

              return maybeLimited.iterator();
            } catch (ApiException e) {
              throw new DASSdkException("Failed to fetch advanced settings", e);
            }
          });
}
