package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.jira.utils.ExceptionHandling.makeSdkException;
import static com.rawlabs.das.jira.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.jira.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.model.AddGroupBean;
import com.rawlabs.das.jira.rest.platform.model.Group;
import com.rawlabs.das.jira.rest.platform.model.GroupDetails;
import com.rawlabs.das.jira.rest.platform.model.UserDetails;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.query.PathKey;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraGroupTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_group";

  private final GroupsApi groupsApi;

  public DASJiraGroupTable(Map<String, String> options, GroupsApi groupsApi) {
    super(
        options,
        TABLE_NAME,
        "Group is a collection of users. Administrators create groups so that the administrator can assign permissions to a number of people at once.");
    this.groupsApi = groupsApi;
  }

  public String uniqueColumn() {
    return "id";
  }

  public List<PathKey> getTablePathKeys() {
    return List.of(PathKey.newBuilder().addKeyColumns("id").build());
  }

  @Override
  public List<Row> bulkInsert(List<Row> rows) {
    return rows.stream().map(this::insert).toList();
  }

  @Override
  public Row insert(Row row) {
    try {
      AddGroupBean groupBean = new AddGroupBean();
      groupBean.setName((String) extractValueFactory.extractValue(row, "name"));
      Group group = this.groupsApi.createGroup(groupBean);
      GroupDetails groupDetails = new GroupDetails();
      groupDetails.setName(group.getName());
      groupDetails.setGroupId(group.getGroupId());
      return toRow(groupDetails, List.of(), List.of(), List.of());
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  @Override
  public void delete(Value rowId) {
    try {
      this.groupsApi.removeGroup(
          null, (String) extractValueFactory.extractValue(rowId), null, null);
    } catch (ApiException e) {
      throw makeSdkException(e);
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, List<SortKey> sortKeys, @Nullable Long limit) {

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
                    throw makeSdkException(e);
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
        return toRow(groupDetails, memberIds, memberNames, columns);
      }

      @Override
      public DASJiraPage<GroupDetails> fetchPage(long offset) {
        try {
          var result =
              groupsApi.bulkGetGroups(offset, withMaxResultOrLimit(limit), null, null, null, null);
          return new DASJiraPage<>(result.getValues(), result.getTotal());
        } catch (ApiException e) {
          throw makeSdkException(e);
        }
      }
    };
  }

  private Row toRow(
      GroupDetails groupDetails,
      List<String> memberIds,
      List<String> memberNames,
      List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("name", rowBuilder, groupDetails.getName(), columns);
    addToRow("id", rowBuilder, groupDetails.getGroupId(), columns);
    addToRow("member_ids", rowBuilder, memberIds, columns);
    addToRow("member_names", rowBuilder, memberNames, columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
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
