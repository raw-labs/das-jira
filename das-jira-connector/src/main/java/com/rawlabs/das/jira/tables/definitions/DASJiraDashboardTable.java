package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.model.*;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.protocol.das.v1.query.PathKey;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;

public class DASJiraDashboardTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_dashboard";

  private final DashboardsApi dashboardsApi;

  public DASJiraDashboardTable(Map<String, String> options, DashboardsApi dashboardsApi) {
    super(
        options, TABLE_NAME, "Your dashboard is the main display you see when you log in to Jira.");
    this.dashboardsApi = dashboardsApi;
  }

  public String uniqueColumn() {
    return "id";
  }

  public List<PathKey> getTablePathKeys() {
    return List.of(PathKey.newBuilder().addKeyColumns("id").build());
  }

  public List<Row> bulkInsert(List<Row> rows) {
    return rows.stream().map(this::insert).toList();
  }

  private List<SharePermission> getPermissions(Row row, String columnName) throws IOException {
    ArrayNode arrayNode = (ArrayNode) extractValueFactory.extractValue(row, columnName);
    List<SharePermission> editPermissions = new ArrayList<>();
    for (int i = 0; i < arrayNode.size(); i++) {
      editPermissions.add(SharePermission.fromJson(arrayNode.get(i).toString()));
    }
    return editPermissions;
  }

  private DashboardDetails getDashboardDetails(Row row) throws IOException {
    DashboardDetails dashboardDetails = new DashboardDetails();
    dashboardDetails.setDescription((String) extractValueFactory.extractValue(row, "description"));
    dashboardDetails.setName((String) extractValueFactory.extractValue(row, "name"));
    dashboardDetails.setSharePermissions(getPermissions(row, "share_permissions"));
    dashboardDetails.setEditPermissions(getPermissions(row, "edit_permissions"));
    return dashboardDetails;
  }

  @Override
  public Row insert(Row row) {
    try {
      Dashboard result = dashboardsApi.createDashboard(getDashboardDetails(row), null);
      return toRow(result, List.of());
    } catch (ApiException | IOException e) {
      throw new DASSdkException(e.getMessage(), e);
    }
  }

  @Override
  public Row update(Value rowId, Row newValues) {
    try {
      Dashboard result =
          dashboardsApi.updateDashboard(
              extractValueFactory.extractValue(rowId).toString(),
              getDashboardDetails(newValues),
              null);
      return toRow(result, List.of());
    } catch (ApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(Value rowId) {
    try {
      dashboardsApi.deleteDashboard(extractValueFactory.extractValue(rowId).toString());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {
    return new DASJiraPaginatedResult<Dashboard>(limit) {

      @Override
      public Row next() {
        return toRow(this.getNext(), columns);
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

  private Row toRow(Dashboard dashboard, List<String> columns) {
    try {
      Row.Builder rowBuilder = Row.newBuilder();
      addToRow("id", rowBuilder, dashboard.getId(), columns);
      addToRow("name", rowBuilder, dashboard.getName(), columns);

      var self = Optional.ofNullable(dashboard.getSelf()).map(Object::toString).orElse(null);
      addToRow("self", rowBuilder, self, columns);

      addToRow("is_favourite", rowBuilder, dashboard.getIsFavourite(), columns);

      var maybeOwner = Optional.ofNullable(dashboard.getOwner());
      addToRow(
          "owner_account_id",
          rowBuilder,
          maybeOwner.map(UserBean::getAccountId).orElse(null),
          columns);
      addToRow(
          "owner_display_name",
          rowBuilder,
          maybeOwner.map(UserBean::getDisplayName).orElse(null),
          columns);

      var popularity =
          Optional.ofNullable(dashboard.getPopularity()).map(Object::toString).orElse(null);
      addToRow("popularity", rowBuilder, popularity, columns);

      addToRow("rank", rowBuilder, dashboard.getRank(), columns);
      addToRow("view", rowBuilder, dashboard.getView(), columns);
      addToRow(
          "edit_permissions",
          rowBuilder,
          objectMapper.writeValueAsString(dashboard.getEditPermissions()),
          columns);
      addToRow(
          "share_permissions",
          rowBuilder,
          objectMapper.writeValueAsString(dashboard.getSharePermissions()),
          columns);
      addToRow("title", rowBuilder, dashboard.getName(), columns);
      return rowBuilder.build();
    } catch (JsonProcessingException e) {
      throw new DASSdkException(e.getMessage());
    }
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
    columns.put("rank", createColumn("rank", "The rank of this dashboard.", createIntType()));
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
            createAnyType()));
    columns.put("title", createColumn("title", "Title of the resource.", createStringType()));
    return columns;
  }
}
