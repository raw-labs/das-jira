package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.model.GetAllBoards200ResponseValuesInner;
import com.rawlabs.das.jira.rest.software.model.IssueBean;
import com.rawlabs.das.jira.tables.*;
import com.rawlabs.das.sdk.DASSdkException;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.das.TableDefinition;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraBacklogIssueTable extends DASJiraBaseTable {

  private static final String TABLE_NAME = "jira_backlog_issue";

  private DASJiraTableDefinition<GetAllBoards200ResponseValuesInner> dasJiraTableDefinition =
      buildTable();

  private BoardApi api = new BoardApi();

  @Override
  public String getTableName() {
    return dasJiraTableDefinition.getTableDefinition().getTableId().getName();
  }

  @Override
  public TableDefinition getTableDefinition() {
    return null;
    //    return dasJiraTableDefinition.getTableDefinition();
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit) {
    try {
      GetAllBoards200ResponseValuesInner result = api.getBoard(1L);
    } catch (Exception e) {
      throw new DASSdkException(e.getMessage(), e);
    }
    return super.execute(quals, columns, sortKeys, limit);
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
  public String getUniqueColumn() {
    return "id";
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

  @SuppressWarnings("unchecked")
  public final DASJiraTableDefinition<GetAllBoards200ResponseValuesInner> buildTable() {
    DASJiraBoardTable dasJiraBoardTable = new DASJiraBoardTable(options);
    return new DASJiraTableDefinition<>(
        TABLE_NAME,
        "The backlog contains incomplete issues that are not assigned to any future or active sprint.",
        List.of(
            new DASJiraNormalColumnDefinition<>(
                "board_name",
                "The key of the issue.",
                createStringType(),
                GetAllBoards200ResponseValuesInner::getName),
            new DASJiraParentColumnDefinition<>(
                "board_id",
                "The ID of the issue.",
                createStringType(),
                GetAllBoards200ResponseValuesInner::getId,
                new DASJiraTableDefinition<>(
                    "issues",
                    "issues of the board",
                    List.of(
                        new DASJiraNormalColumnDefinition<>(
                            "id", "The ID of the issue.", createStringType(), IssueBean::getId),
                        new DASJiraNormalColumnDefinition<>(
                            "key", "The key of the issue.", createStringType(), IssueBean::getKey),
                        new DASJiraNormalColumnDefinition<>(
                            "self",
                            "The URL of the issue details.",
                            createStringType(),
                            IssueBean::getSelf),
                        new DASJiraNormalColumnDefinition<>(
                            "project_key",
                            "A friendly key that identifies the project.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> project =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("project", null);
                              return project == null ? null : project.getOrDefault("key", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "project_id",
                            "A friendly key that identifies the project.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> project =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("project", null);
                              return project == null ? null : project.getOrDefault("id", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "project_name",
                            "Name of the project to that issue belongs.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> project =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("project", null);
                              return project == null ? null : project.getOrDefault("name", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "status",
                            "The status of the issue. Eg: To Do, In Progress, Done.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> status =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("status", null);
                              return status.getOrDefault("name", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "assignee_account_id",
                            "Account Id the user/application that the issue is assigned to work.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> assignee =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("assignee", null);
                              return assignee == null
                                  ? null
                                  : assignee.getOrDefault("accountId", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "assignee_display_name",
                            "Display name the user/application that the issue is assigned to work.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> assignee =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("assignee", null);
                              return assignee == null
                                  ? null
                                  : assignee.getOrDefault("displayName", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "created",
                            "Time when the issue was created.",
                            createTimestampType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("created", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "creator_account_id",
                            "Account Id of the user/application that created the issue.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> creator =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("creator", null);
                              return creator == null
                                  ? null
                                  : creator.getOrDefault("accountId", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "creator_display_name",
                            "Display name of the user/application that created the issue.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> creator =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("creator", null);
                              return creator == null
                                  ? null
                                  : creator.getOrDefault("displayName", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "description",
                            "Description of the issue.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("description", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "due_date",
                            "Time by which the issue is expected to be completed.",
                            createTimestampType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("duedate", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "epic_key",
                            "The key of the epic to which issue belongs.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("epic", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "priority",
                            "Priority assigned to the issue.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> priority =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("priority", null);
                              return priority == null ? null : priority.getOrDefault("name", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "reporter_account_id",
                            "Account Id of the user/application issue is reported.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> reporter =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("reporter", null);
                              return reporter == null
                                  ? null
                                  : reporter.getOrDefault("accountId", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "reporter_display_name",
                            "Display name of the user/application issue is reported.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> reporter =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("reporter", null);
                              return reporter == null
                                  ? null
                                  : reporter.getOrDefault("displayName", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "summary",
                            "Details of the user/application that created the issue.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("summary", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "type",
                            "The name of the issue type.",
                            createStringType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              Map<String, Object> type =
                                  (Map<String, Object>)
                                      issueBean.getFields().getOrDefault("type", null);
                              return type == null ? null : type.getOrDefault("name", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "updated",
                            "Time when the issue was last updated.",
                            createTimestampType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("updated", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "components",
                            "List of components associated with the issue.",
                            createAnyType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("components", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "fields",
                            "Json object containing important subfields of the issue.",
                            createAnyType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields();
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "labels",
                            "A list of labels applied to the issue.",
                            createListType(createStringType()),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              return issueBean.getFields().getOrDefault("labels", null);
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "tags",
                            "A map of label names associated with this issue",
                            createAnyType(),
                            (IssueBean issueBean) -> {
                              assert issueBean.getFields() != null;
                              var labels =
                                  (List<String>) issueBean.getFields().getOrDefault("lables", null);
                              Map<String, Boolean> tags = new HashMap<>();
                              labels.forEach(label -> tags.put(label, true));
                              return tags;
                            }),
                        new DASJiraNormalColumnDefinition<>(
                            "title", TITLE_DESC, createStringType(), IssueBean::getKey)),
                    (quals, _, _, limit) -> null))),
        dasJiraBoardTable.hydrateFunction);
  }

  public DASJiraBacklogIssueTable(Map<String, String> options) {
    super(options);
  }

  /** Constructor for mocks */
  DASJiraBacklogIssueTable(Map<String, String> options, BoardApi api) {
    this(options);
    this.api = api;
  }
}
