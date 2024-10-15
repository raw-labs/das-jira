package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.createStringType;

public class DASJiraIssueTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_issue";

  private IssuesApi issuesApi = new IssuesApi();

  public DASJiraIssueTable(Map<String, String> options) {
    super(
        options, TABLE_NAME, "Issues help manage code, estimate workload, and keep track of team.");
  }

  /** Constructor for mocks */
  DASJiraIssueTable(Map<String, String> options, IssuesApi issuesApi) {
    this(options);
    this.issuesApi = issuesApi;
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

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }

  private Row toRow() {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    //    addToRow("voting_enabled", rowBuilder, configuration.getVotingEnabled());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("name", createColumn("name", "The name of the group.", createStringType()));
    return columns;
  }
}
