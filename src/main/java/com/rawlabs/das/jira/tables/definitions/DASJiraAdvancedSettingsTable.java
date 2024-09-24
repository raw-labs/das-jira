package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraModelToRow;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.rest.jira.ApiException;
import com.rawlabs.das.rest.jira.api.JiraSettingsApi;
import com.rawlabs.das.rest.jira.model.ApplicationProperty;
import com.rawlabs.das.rest.jira.model.SimpleApplicationPropertyBean;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.ColumnFactory.*;
import static com.rawlabs.das.sdk.java.utils.TypeFactory.*;

public class DASJiraAdvancedSettingsTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_advanced_setting";

  private final DASJiraModelToRow jsonToRow =
      new DASJiraModelToRow(
          ApplicationProperty.class,
          getTableDefinition(),
          Map.of("desc", "description", "title", "name"));

  private final JiraSettingsApi api = new JiraSettingsApi();

  public DASJiraAdvancedSettingsTable(Map<String, String> options) {
    super(TABLE_NAME, options);
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
      return getResult(key);
    } catch (ApiException e) {
      throw new DASSdkException("Failed to fetch advanced settings", e);
    }
  }

  private DASExecuteResult getResult(@Nullable String key) throws ApiException {
    List<ApplicationProperty> applicationProperties =
        key == null ? api.getAdvancedSettings() : api.getApplicationProperty(key, null, null);
    Iterator<ApplicationProperty> iterator = applicationProperties.iterator();

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
          return jsonToRow.toRow(iterator.next());
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
      return jsonToRow.toRow(applicationProperty);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  protected TableDefinition buildTableDefinition() {
    return TableDefinition.newBuilder()
        .setTableId(TableId.newBuilder().setName(this.getTableName()))
        .setDescription(
            "The application properties that are accessible on the Advanced Settings page.")
        .addColumns(
            createColumn("id", "The unique identifier of the property.", createStringType()))
        .addColumns(
            createColumn("name", "The name of the application property.", createStringType()))
        .addColumns(
            createColumn(
                "description", "The description of the application property.", createStringType()))
        .addColumns(createColumn("key", "The key of the application property.", createStringType()))
        .addColumns(
            createColumn("type", "The data type of the application property.", createStringType()))
        .addColumns(createColumn("value", "The new value.", createStringType()))
        .addColumns(
            createColumn(
                "allowed_values",
                "The allowed values, if applicable.",
                createListType(createStringType())))
        .addColumns(
            createColumn(
                "title", "The default value of the application property.", createStringType()))
        .build();
  }
}
