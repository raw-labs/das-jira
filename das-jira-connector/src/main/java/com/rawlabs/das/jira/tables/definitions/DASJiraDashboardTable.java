package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.api.JiraSettingsApi;
import com.rawlabs.das.jira.rest.platform.model.Dashboard;
import com.rawlabs.das.jira.rest.platform.model.PageOfDashboards;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;

import javax.annotation.Nullable;
import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraDashboardTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_dashboard";

  private DashboardsApi dashboardsApi = new DashboardsApi();

  public DASJiraDashboardTable(Map<String, String> options) {
    super(
        options, TABLE_NAME, "Your dashboard is the main display you see when you log in to Jira.");
  }

  /** Constructor for mocks */
  DASJiraDashboardTable(Map<String, String> options, DashboardsApi dashboardsApi) {
    this(options);
    this.dashboardsApi = dashboardsApi;
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    return super.canSort(sortKeys);
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return super.getPathKeys();
  }

  @Override
  public Row insertRow(Row row) {
    return super.insertRow(row);
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return super.insertRows(rows);
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    return super.updateRow(rowId, newValues);
  }

  @Override
  public void deleteRow(Value rowId) {
    super.deleteRow(rowId);
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return new DASJiraPaginatedResult<Dashboard>() {

      @Override
      public Row next() {
        return toRow(this.getNext());
      }

      @Override
      public DASJiraPage<Dashboard> fetchPage(long offset) {
        try {
          PageOfDashboards page =
              dashboardsApi.getAllDashboards(
                  null, Math.toIntExact(offset), withMaxResultOrLimit(limit));
          return new DASJiraPage<>(
              page.getDashboards(), Long.valueOf(Objects.requireNonNullElse(page.getTotal(), 0)));
        } catch (ApiException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private Row toRow(Dashboard dashboard) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, dashboard.getId());
    addToRow("name", rowBuilder, dashboard.getName());
    Optional.ofNullable(dashboard.getSelf())
        .ifPresent(s -> addToRow("self", rowBuilder, s.toString()));
    addToRow("is_favourite", rowBuilder, dashboard.getIsFavourite());
    Optional.ofNullable(dashboard.getOwner())
        .ifPresent(
            o -> {
              addToRow("owner_account_id", rowBuilder, o.getAccountId());
              addToRow("owner_display_name", rowBuilder, o.getDisplayName());
            });
    addToRow("popularity", rowBuilder, dashboard.getPopularity());
    addToRow("rank", rowBuilder, dashboard.getRank());
    addToRow("view", rowBuilder, dashboard.getView());
    addToRow("edit_permissions", rowBuilder, dashboard.getEditPermissions());
    addToRow("share_permissions", rowBuilder, dashboard.getSharePermissions());
    addToRow("title", rowBuilder, dashboard.getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The ID of the dashboard.", createStringType()));
    columns.put("name", createColumn("name", "The name of the dashboard.", createStringType()));
    columns.put(
        "self", createColumn("self", "The URL of the dashboard details.", createStringType()));
    columns.put(
        "is_favourite",
        createColumn(
            "is_favourite",
            "Indicates if the dashboard is selected as a favorite by the user.",
            createBoolType()));
    columns.put(
        "owner_account_id",
        createColumn(
            "owner_account_id", "The user info of owner of the dashboard.", createStringType()));
    columns.put(
        "owner_display_name",
        createColumn(
            "owner_display_name", "The user info of owner of the dashboard.", createStringType()));
    columns.put(
        "popularity",
        createColumn(
            "popularity",
            "The number of users who have this dashboard as a favorite.",
            createStringType()));
    columns.put("rank", createColumn("rank", "The rank of this dashboard.", createStringType()));
    columns.put("view", createColumn("view", "The URL of the dashboard.", createStringType()));
    columns.put(
        "edit_permissions",
        createColumn(
            "edit_permissions",
            "The details of any edit share permissions for the dashboard.",
            createAnyType()));
    columns.put(
        "share_permissions",
        createColumn(
            "share_permissions",
            "The details of any view share permissions for the dashboard.",
            createStringType()));
    columns.put("title", createColumn("title", "Title of the resource.", createAnyType()));
    return columns;
  }
}
