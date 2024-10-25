package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.model.AddGroupBean;
import com.rawlabs.das.jira.rest.platform.model.Group;
import com.rawlabs.das.jira.rest.platform.model.GroupDetails;
import com.rawlabs.das.jira.rest.platform.model.UserDetails;
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

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraGroupTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_group";

  private GroupsApi groupsApi = new GroupsApi();

  public DASJiraGroupTable(Map<String, String> options) {
    super(
        options,
        TABLE_NAME,
        "Group is a collection of users. Administrators create groups so that the administrator can assign permissions to a number of people at once.");
  }

  /** Constructor for mocks */
  DASJiraGroupTable(Map<String, String> options, GroupsApi groupsApi) {
    this(options);
    this.groupsApi = groupsApi;
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
  public Row insertRow(Row row) {
    try {
      AddGroupBean groupBean = new AddGroupBean();
      groupBean.setName((String) extractValueFactory.extractValue(row, "name"));
      Group group = this.groupsApi.createGroup(groupBean);
      GroupDetails groupDetails = new GroupDetails();
      groupDetails.setName(group.getName());
      groupDetails.setGroupId(group.getGroupId());
      return toRow(groupDetails, Collections.emptyList(), Collections.emptyList());
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      this.groupsApi.removeGroup(
          null, (String) extractValueFactory.extractValue(rowId), null, null);
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

    return new DASJiraPaginatedResult<GroupDetails>(limit) {

      @Override
      public Row next() {

        List<String> memberIds = new ArrayList<>();
        List<String> memberNames = new ArrayList<>();

        GroupDetails groupDetails = this.getNext();

        if (columns.contains("member_ids")
            || columns.contains("member_names")
            || columns.isEmpty()) {
          try (var childResult =
              new DASJiraPaginatedResult<UserDetails>(limit) {

                @Override
                public Row next() {
                  throw new UnsupportedOperationException();
                }

                @Override
                public DASJiraPage<UserDetails> fetchPage(long offset) {
                  try {
                    var result =
                        groupsApi.getUsersFromGroup(
                            null,
                            groupDetails.getGroupId(),
                            null,
                            offset,
                            withMaxResultOrLimit(limit));
                    return new DASJiraPage<>(result.getValues(), result.getTotal());
                  } catch (ApiException e) {
                    throw new DASSdkApiException(e.getMessage());
                  }
                }
              }) {

            while (childResult.hasNext()) {
              UserDetails userDetails = childResult.getNext();
              memberIds.add(userDetails.getAccountId());
              memberNames.add(userDetails.getDisplayName());
            }
          }
        }
        return toRow(groupDetails, memberIds, memberNames);
      }

      @Override
      public DASJiraPage<GroupDetails> fetchPage(long offset) {
        try {
          var result =
              groupsApi.bulkGetGroups(offset, withMaxResultOrLimit(limit), null, null, null, null);
          return new DASJiraPage<>(result.getValues(), result.getTotal());
        } catch (ApiException e) {
          throw new DASSdkApiException(e.getMessage());
        }
      }
    };
  }

  private Row toRow(GroupDetails groupDetails, List<String> memberIds, List<String> memberNames) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("name", rowBuilder, groupDetails.getName());
    addToRow("id", rowBuilder, groupDetails.getGroupId());
    addToRow("member_ids", rowBuilder, memberIds);
    addToRow("member_names", rowBuilder, memberNames);
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
    columns.put("name", createColumn("name", "The name of the group.", createStringType()));
    columns.put("id", createColumn("id", "The ID of the group.", createStringType()));
    columns.put(
        "member_ids",
        createColumn(
            "member_ids",
            "List of account ids of users associated with the group.",
            createListType(createStringType())));
    columns.put(
        "member_names",
        createColumn(
            "member_names",
            "List of names of users associated with the group.",
            createListType(createStringType())));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));

    return columns;
  }
}
