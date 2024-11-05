package com.rawlabs.das.jira.tables.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.model.*;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;

import javax.annotation.Nullable;
import java.io.IOException;
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
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1));
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
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
  public Row insertRow(Row row) {
    try {
      Dashboard result = dashboardsApi.createDashboard(getDashboardDetails(row), null);
      return toRow(result);
    } catch (ApiException | IOException e) {
      throw new DASSdkApiException(e.getMessage(), e);
    }
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      Dashboard result =
          dashboardsApi.updateDashboard(
              extractValueFactory.extractValue(rowId).toString(),
              getDashboardDetails(newValues),
              null);
      return toRow(result);
    } catch (ApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      dashboardsApi.deleteDashboard(extractValueFactory.extractValue(rowId).toString());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return new DASJiraPaginatedResult<Dashboard>(limit) {

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
    try {
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

      Optional.ofNullable(dashboard.getPopularity())
          .ifPresent(p -> addToRow("popularity", rowBuilder, p.toString()));

      addToRow("rank", rowBuilder, dashboard.getRank());
      addToRow("view", rowBuilder, dashboard.getView());
      addToRow(
          "edit_permissions",
          rowBuilder,
          objectMapper.writeValueAsString(dashboard.getEditPermissions()));
      addToRow(
          "share_permissions",
          rowBuilder,
          objectMapper.writeValueAsString(dashboard.getSharePermissions()));
      addToRow("title", rowBuilder, dashboard.getName());
      return rowBuilder.build();
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException(e.getMessage());
    }
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
            createAnyType()));
    columns.put("title", createColumn("title", "Title of the resource.", createStringType()));
    return columns;
  }
}
