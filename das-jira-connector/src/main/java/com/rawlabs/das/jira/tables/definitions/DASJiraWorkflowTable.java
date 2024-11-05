package com.rawlabs.das.jira.tables.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.WorkflowsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanWorkflow;
import com.rawlabs.das.jira.rest.platform.model.Workflow;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEqDistinct;
import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraWorkflowTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_workflow";

  private WorkflowsApi workflowsApi = new WorkflowsApi();

  public DASJiraWorkflowTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "A Jira workflow is a set of statuses and transitions that an issue moves through during its lifecycle, and typically represents a process within your organization.");
  }

  /** Constructor for mocks */
  DASJiraWorkflowTable(Map<String, String> options, WorkflowsApi workflowsApi) {
    this(options);
    this.workflowsApi = workflowsApi;
  }

  @Override
  public String getUniqueColumn() {
    return "entity_id";
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    return sortKeys.stream()
        .filter(sortKey -> sortKey.getName().equals("name") || sortKey.getName().equals("title"))
        .toList();
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      workflowsApi.deleteInactiveWorkflow(rowId.toString());
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
    final String name = (String) extractEqDistinct(quals, "name");
    final Set<String> setOfNames = name == null ? null : Set.of(name);

    return new DASJiraPaginatedResult<Workflow>(limit) {
      @Override
      public Row next() {
        return toRow(getNext());
      }

      @Override
      public DASJiraPage<Workflow> fetchPage(long offset) {
        try {
          PageBeanWorkflow result =
              workflowsApi.getWorkflowsPaginated(
                  offset,
                  withMaxResultOrLimit(limit),
                  setOfNames,
                  null,
                  null,
                  withOrderBy(sortKeys),
                  null);
          return new DASJiraPage<>(result.getValues(), result.getTotal());
        } catch (ApiException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private Row toRow(Workflow workflow) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("name", rowBuilder, workflow.getId().getName());
    addToRow("entity_id", rowBuilder, workflow.getId().getEntityId());
    addToRow("description", rowBuilder, workflow.getDescription());
    addToRow("is_default", rowBuilder, workflow.getIsDefault());
    try {
      StringBuilder sb = new StringBuilder();
      StringJoiner joiner = new StringJoiner(",");
      sb.append("[");
      Optional.ofNullable(workflow.getTransitions())
          .ifPresent(t -> t.forEach(transition -> joiner.add(transition.toJson())));
      sb.append(joiner.toString());
      sb.append("]");
      addToRow("transitions", rowBuilder, sb.toString());
      addToRow("statuses", rowBuilder, objectMapper.writeValueAsString(workflow.getStatuses()));
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException(e.getMessage());
    }
    addToRow("title", rowBuilder, workflow.getId().getName());
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("name", createColumn("name", "The name of the workflow.", createStringType()));
    columns.put(
        "entity_id",
        createColumn("entity_id", "The entity ID of the workflow.", createStringType()));
    columns.put(
        "description",
        createColumn("description", "The description of the workflow.", createStringType()));
    columns.put(
        "is_default",
        createColumn("is_default", "Whether this is the default workflow.", createBoolType()));
    columns.put(
        "transitions",
        createColumn("transitions", "The transitions of the workflow.", createAnyType()));
    columns.put(
        "statuses", createColumn("statuses", "The statuses of the workflow.", createAnyType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
