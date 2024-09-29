package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraColumnDefinition;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.rest.ApiException;
import com.rawlabs.das.jira.rest.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.model.ApplicationProperty;
import com.rawlabs.das.jira.rest.model.SimpleApplicationPropertyBean;
import com.rawlabs.das.jira.tables.DASJiraTableDefinition;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.*;

import javax.annotation.Nullable;
import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraAdvancedSettingsTable extends DASJiraTable {

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
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    Optional<Qual> simpleQuals =
        quals.stream()
            .filter(
                q ->
                    q.hasSimpleQual()
                        && (q.getSimpleQual().hasOperator())
                        && q.getSimpleQual().getOperator().hasEquals()
                        && q.getFieldName().equals("key"))
            .findFirst();
    String key =
        simpleQuals.map(qual -> qual.getSimpleQual().getValue().getString().getV()).orElse(null);
    try {
      return getResult(key, limit);
    } catch (ApiException e) {
      throw new DASSdkException("Failed to fetch advanced settings", e);
    }
  }

  private DASExecuteResult getResult(@Nullable String key, @Nullable Long limit)
      throws ApiException {

    List<ApplicationProperty> result =
        key == null ? api.getAdvancedSettings() : api.getApplicationProperty(key, null, null);

    List<ApplicationProperty> maybeLimited =
        limit == null ? result : result.subList(0, Math.toIntExact(limit));

    Iterator<ApplicationProperty> iterator = maybeLimited.iterator();

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
          return toRow(iterator.next());
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
      return toRow(applicationProperty);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getTableName() {
    return dasJiraTableDefinition.getTableDefinition().getTableId().getName();
  }

  @Override
  public TableDefinition getTableDefinition() {
    return dasJiraTableDefinition.getTableDefinition();
  }

  private Row toRow(ApplicationProperty applicationProperty) {
    Row.Builder rowBuilder = Row.newBuilder();
    dasJiraTableDefinition.updateRow("id", rowBuilder, applicationProperty.getId());
    dasJiraTableDefinition.updateRow("name", rowBuilder, applicationProperty.getName());
    dasJiraTableDefinition.updateRow("description", rowBuilder, applicationProperty.getDesc());
    dasJiraTableDefinition.updateRow("key", rowBuilder, applicationProperty.getKey());
    dasJiraTableDefinition.updateRow("type", rowBuilder, applicationProperty.getType());
    dasJiraTableDefinition.updateRow("value", rowBuilder, applicationProperty.getValue());
    dasJiraTableDefinition.updateRow(
        "allowed_values", rowBuilder, applicationProperty.getAllowedValues());
    dasJiraTableDefinition.updateRow("title", rowBuilder, applicationProperty.getName());
    return rowBuilder.build();
  }

  DASJiraTableDefinition dasJiraTableDefinition =
      new DASJiraTableDefinition(
          TABLE_NAME,
          "The application properties that are accessible on the Advanced Settings page.",
          Map.of(
              "id",
              new DASJiraColumnDefinition(
                  "id", "The unique identifier of the property.", createStringType()),
              "name",
              new DASJiraColumnDefinition(
                  "name", "The name of the application property.", createStringType()),
              "description",
              new DASJiraColumnDefinition(
                  "description",
                  "The description of the application property.",
                  createStringType()),
              "key",
              new DASJiraColumnDefinition(
                  "key", "The key of the application property.", createStringType()),
              "type",
              new DASJiraColumnDefinition(
                  "type", "The data type of the application property.", createStringType()),
              "value",
              new DASJiraColumnDefinition("value", "The new value.", createStringType()),
              "allowed_values",
              new DASJiraColumnDefinition(
                  "allowed_values",
                  "The allowed values, if applicable.",
                  createListType(createStringType())),
              "title",
              new DASJiraColumnDefinition("title", TITLE_DESC, createStringType())));
}
