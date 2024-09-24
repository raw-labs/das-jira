package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.RowsEstimation;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class DASJiraTable implements DASTable {
  private final Map<String, String> options;
  private final String tableName;
  private final TableDefinition tableDefinition;
  private final RowsEstimation rowsEstimation = new RowsEstimation(100, 100);

  protected DASJiraTable(String tableName, Map<String, String> options) {
    this.options = options;
    this.tableName = tableName;
    this.tableDefinition = buildTableDefinition();
  }

  protected abstract TableDefinition buildTableDefinition();
//  protected abstract TableDefinition buildMappings();


  @Override
  public DASExecuteResult execute(List<Qual> quals, List<String> columns, @Nullable List<SortKey> sortKeys, @Nullable Long limit) {
    return null;
  }

  public String getTableName() {
    return tableName;
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  @Override
  public RowsEstimation getRelSize(List<Qual> quals, List<String> columns) {
    return rowsEstimation;
  }
}
