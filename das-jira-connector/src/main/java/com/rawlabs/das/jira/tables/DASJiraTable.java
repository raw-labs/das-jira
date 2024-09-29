package com.rawlabs.das.jira.tables;

import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.DASTable;
import com.rawlabs.das.sdk.java.RowsEstimation;
import com.rawlabs.das.sdk.java.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.sdk.java.utils.factory.value.ValueFactory;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class DASJiraTable implements DASTable {

  protected static final String TITLE_DESC = "Title of the resource.";

  private final Map<String, String> options;

  protected final ValueFactory valueFactory = new DefaultValueFactory();
  private final RowsEstimation rowsEstimation = new RowsEstimation(100, 100);

  protected DASJiraTable(Map<String, String> options) {
    this.options = options;
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    return null;
  }

  public abstract String getTableName();

  public abstract TableDefinition getTableDefinition();

  @Override
  public RowsEstimation getRelSize(List<Qual> quals, List<String> columns) {
    return rowsEstimation;
  }
}
