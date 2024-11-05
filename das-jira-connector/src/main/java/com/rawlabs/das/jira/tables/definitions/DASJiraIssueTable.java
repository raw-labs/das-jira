package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.IssueSearchApi;
import com.rawlabs.das.jira.rest.platform.api.IssuesApi;
import com.rawlabs.das.jira.rest.platform.model.IssueBean;
import com.rawlabs.das.jira.rest.platform.model.IssueUpdateDetails;
import com.rawlabs.das.jira.tables.DASJiraIssueTransformationTable;
import com.rawlabs.das.jira.tables.DASJiraJqlQueryBuilder;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.*;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraIssueTable extends DASJiraIssueTransformationTable {

  public static final String TABLE_NAME = "jira_issue";

  private IssueSearchApi issueSearchApi = new IssueSearchApi();
  private IssuesApi issuesApi = new IssuesApi();

  public DASJiraIssueTable(Map<String, String> options) {
    super(
        options, TABLE_NAME, "Issues help manage code, estimate workload, and keep track of team.");
  }

  /** Constructor for mocks */
  DASJiraIssueTable(Map<String, String> options, IssueSearchApi issueSearchApi) {
    this(options);
    this.issueSearchApi = issueSearchApi;
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  @Override
  public Row insertRow(Row row) {
    try {
      IssueUpdateDetails issueUpdateDetails = new IssueUpdateDetails();
      var result = this.issuesApi.createIssue(issueUpdateDetails, null);
      Row.Builder builder = Row.newBuilder();
      addToRow("id", builder, result.getId());
      addToRow("key", builder, result.getKey());
      addToRow("self", builder, result.getSelf());
      return builder.build();
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      issuesApi.deleteIssue(extractValueFactory.extractValue(rowId).toString(), null);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {

    return new DASJiraPaginatedResult<IssueBean>(limit) {

      @Override
      public Row next() {
        return toRow(this.getNext(), names());
      }

      @Override
      public DASJiraPage<IssueBean> fetchPage(long offset) {
        try {
          var result =
              issueSearchApi.searchForIssuesUsingJql(
                  DASJiraJqlQueryBuilder.buildJqlQuery(quals),
                  Math.toIntExact(offset),
                  withMaxResultOrLimit(limit),
                  null,
                  null,
                  "names",
                  null,
                  null,
                  null);
          return new DASJiraPage<>(
              result.getIssues(),
              Long.valueOf(Objects.requireNonNullElse(result.getTotal(), 0)),
              result.getNames());
        } catch (ApiException e) {
          throw new DASSdkApiException(e.getMessage());
        }
      }
    };
  }

  @SuppressWarnings("unchecked")
  private Row toRow(IssueBean issueBean, Map<String, String> names) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("id", rowBuilder, issueBean.getId());
    addToRow("key", rowBuilder, issueBean.getKey());
    Optional.ofNullable(issueBean.getSelf())
        .ifPresent(self -> addToRow("self", rowBuilder, self.toString()));
    Optional.ofNullable(issueBean.getFields())
        .ifPresent(
            fields -> {
              processFields(fields, names, rowBuilder);

              Optional.ofNullable(fields.get(names.get("Parent")))
                  .ifPresent(
                      p ->
                          Optional.ofNullable(((Map<String, Object>) p).get("fields"))
                              .flatMap(
                                  parentFields ->
                                      Optional.ofNullable(
                                              ((Map<String, Object>) parentFields)
                                                  .get(names.get("Issue Type")))
                                          .flatMap(
                                              issueType ->
                                                  Optional.ofNullable(
                                                      ((Map<String, Object>) issueType)
                                                          .get("name"))))
                              .ifPresent(
                                  name -> {
                                    if (name.equals("Epic")) {
                                      addToRow(
                                          "epic_key",
                                          rowBuilder,
                                          ((Map<String, Object>) p).get("key"));
                                    }
                                  }));
              Optional.ofNullable(fields.get(names.get("Sprint")))
                  .ifPresent(
                      sprints -> {
                        List<Map<String, Object>> sprintList = (List<Map<String, Object>>) sprints;
                        addToRow(
                            "sprint_ids",
                            rowBuilder,
                            sprintList.stream().map(s -> s.get("id").toString()).toList());
                        addToRow(
                            "sprint_names",
                            rowBuilder,
                            sprintList.stream().map(s -> s.get("name")).toList());
                      });
            });

    addToRow("title", rowBuilder, issueBean.getKey());

    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("id", createColumn("id", "The ID of the issue", createStringType()));
    columns.put("key", createColumn("key", "The key of the issue", createStringType()));
    columns.put("self", createColumn("self", "The URL of the issue details", createStringType()));
    columns.put(
        "project_key",
        createColumn(
            "project_key", "A friendly key that identifies the project", createStringType()));
    columns.put(
        "project_id",
        createColumn(
            "project_id", "A friendly key that identifies the project", createStringType()));
    columns.put(
        "status",
        createColumn(
            "status",
            "Json object containing important subfields info the issue",
            createStringType()));
    columns.put(
        "status_category",
        createColumn(
            "status_category",
            "The status category (Open, In Progress, Done) of the ticket",
            createStringType()));
    columns.put(
        "epic_key",
        createColumn("epic_key", "The key of the epic to which issue belongs", createStringType()));
    columns.put(
        "sprint_ids",
        createColumn(
            "sprint_ids",
            "The list of ids of the sprint to which issue belongs",
            createListType(createStringType())));
    columns.put(
        "sprint_names",
        createColumn(
            "sprint_names",
            "The list of names of the sprint to which issue belongs",
            createListType(createStringType())));
    columns.put(
        "assignee_account_id",
        createColumn(
            "assignee_account_id",
            "Account Id the user/application that the issue is assigned to work",
            createStringType()));
    columns.put(
        "assignee_display_name",
        createColumn(
            "assignee_display_name",
            "Display name the user/application that the issue is assigned to work",
            createStringType()));
    columns.put(
        "creator_account_id",
        createColumn(
            "creator_account_id",
            "Account Id of the user/application that created the issue",
            createStringType()));
    columns.put(
        "creator_display_name",
        createColumn(
            "creator_display_name",
            "Display name of the user/application that created the issue",
            createStringType()));
    columns.put(
        "created",
        createColumn("created", "Time when the issue was created", createTimestampType()));
    columns.put(
        "due_date",
        createColumn(
            "due_date",
            "Time by which the issue is expected to be completed",
            createTimestampType()));
    columns.put(
        "description", createColumn("description", "Description of the issue", createStringType()));
    columns.put("type", createColumn("type", "The name of the issue type", createStringType()));
    columns.put(
        "labels",
        createColumn(
            "labels", "A list of labels applied to the issue", createListType(createStringType())));
    columns.put(
        "priority", createColumn("priority", "Priority assigned to the issue", createStringType()));
    columns.put(
        "project_name",
        createColumn(
            "project_name", "Name of the project to that issue belongs", createStringType()));
    columns.put(
        "reporter_account_id",
        createColumn(
            "reporter_account_id",
            "Account Id of the user/application issue is reported",
            createStringType()));
    columns.put(
        "reporter_display_name",
        createColumn(
            "reporter_display_name",
            "Display name of the user/application issue is reported",
            createStringType()));
    columns.put(
        "resolution_date",
        createColumn("resolution_date", "Date the issue was resolved", createTimestampType()));
    columns.put(
        "summary",
        createColumn(
            "summary",
            "Details of the user/application that created the issue",
            createStringType()));
    columns.put(
        "updated",
        createColumn("updated", "Time when the issue was last updated", createTimestampType()));
    columns.put(
        "components",
        createColumn(
            "components",
            "List of components associated with the issue",
            createListType(createStringType())));
    columns.put(
        "fields",
        createColumn(
            "fields", "Json object containing important subfields of the issue", createAnyType()));
    columns.put(
        "tags",
        createColumn(
            "tags",
            "A map of label names associated with this issue, in Steampipe standard format",
            createAnyType()));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
