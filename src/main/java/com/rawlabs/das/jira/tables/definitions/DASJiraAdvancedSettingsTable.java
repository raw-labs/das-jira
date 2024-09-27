package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.tables.DASJiraRowModelMapper;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.rest.jira.ApiException;
import com.rawlabs.das.rest.jira.api.JiraSettingsApi;
import com.rawlabs.das.rest.jira.model.ApplicationProperty;
import com.rawlabs.das.rest.jira.model.SimpleApplicationPropertyBean;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.das.sdk.java.utils.factory.table.TableFactory;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.*;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraAdvancedSettingsTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_advanced_setting";
  private JiraSettingsApi api = new JiraSettingsApi();

  private final DASJiraRowModelMapper rowModelConverter =
      new DASJiraRowModelMapper(
          ApplicationProperty.class,
          getTableDefinition(),
          Map.of("desc", "description", "name", "title"));

  public DASJiraAdvancedSettingsTable(Map<String, String> options) {
    super(TABLE_NAME, options);
  }

  /** Constructor for mocks */
  DASJiraAdvancedSettingsTable(Map<String, String> options, JiraSettingsApi api) {
    super(TABLE_NAME, options);
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
          return rowModelConverter.toRow(iterator.next());
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
      return rowModelConverter.toRow(applicationProperty);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  protected TableDefinition buildTableDefinition() {
    return TableFactory.createTable(
        this.getTableName(),
        "The application properties that are accessible on the Advanced Settings page.",
        List.of(
            createColumn("id", "The unique identifier of the property.", createStringType()),
            createColumn("name", "The name of the application property.", createStringType()),
            createColumn(
                "description", "The description of the application property.", createStringType()),
            createColumn("key", "The key of the application property.", createStringType()),
            createColumn("type", "The data type of the application property.", createStringType()),
            createColumn("value", "The new value.", createStringType()),
            createColumn(
                "allowed_values",
                "The allowed values, if applicable.",
                createListType(createStringType())),
            createColumn(
                "title", "The default value of the application property.", createStringType())));
  }
}
