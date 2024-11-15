package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.UsersApi;
import com.rawlabs.das.jira.rest.platform.model.*;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraUserTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_user";

  private UsersApi usersApi = new UsersApi();

  public DASJiraUserTable(Map<String, String> options) {
    super(options, TABLE_NAME, "User in the Jira cloud.");
  }

  /** Constructor for mocks */
  DASJiraUserTable(Map<String, String> options, UsersApi usersApi) {
    this(options);
    this.usersApi = usersApi;
  }

  @Override
  public String getUniqueColumn() {
    return "account_id";
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("account_id"), 1));
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  @Override
  public Row insertRow(Row row) {
    try {
      NewUserDetails newUserDetails = new NewUserDetails();
      newUserDetails.setName((String) extractValueFactory.extractValue(row, "name"));
      newUserDetails.setKey((String) extractValueFactory.extractValue(row, "key"));
      newUserDetails.setDisplayName((String) extractValueFactory.extractValue(row, "display_name"));
      newUserDetails.setEmailAddress(
          (String) extractValueFactory.extractValue(row, "email_address"));
      newUserDetails.setProducts(Collections.emptySet());
      User user = usersApi.createUser(newUserDetails);
      return toRow(user, List.of());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      usersApi.removeUser(extractValueFactory.extractValue(rowId).toString(), null, null);
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
    try {
      List<User> users = usersApi.getAllUsers(0, withMaxResultOrLimit(limit));
      return fromRowIterator(users.stream().map(u -> toRow(u, columns)).iterator());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  private Row toRow(User user, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    addToRow("display_name", rowBuilder, user.getDisplayName(), columns);
    addToRow("account_id", rowBuilder, user.getAccountId(), columns);
    addToRow("email_address", rowBuilder, user.getEmailAddress(), columns);

    var accountType = Optional.ofNullable(user.getAccountType()).map(Enum::name).orElse(null);
    addToRow("account_type", rowBuilder, accountType, columns);

    addToRow("active", rowBuilder, user.getActive(), columns);

    var self = Optional.ofNullable(user.getSelf()).map(Object::toString).orElse(null);
    addToRow("self", rowBuilder, self, columns);

    var avatarUrls =
        Optional.ofNullable(user.getAvatarUrls()).map(AvatarUrlsBean::toJson).orElse(null);
    addToRow("avatar_urls", rowBuilder, avatarUrls, columns);

    var groups =
        Optional.ofNullable(user.getGroups())
            .map(SimpleListWrapperGroupName::getItems)
            .map(g -> g.stream().map(GroupName::getName))
            .orElse(null);

    addToRow("group_names", rowBuilder, groups, columns);
    addToRow("title", rowBuilder, user.getDisplayName(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
    columns.put(
        "display_name",
        createColumn(
            "display_name",
            "The display name of the user. Depending on the user's privacy setting, this may return an alternative value.",
            createStringType()));
    columns.put(
        "account_id",
        createColumn(
            "account_id",
            "The account ID of the user, which uniquely identifies the user across all Atlassian products. For example, 5b10ac8d82e05b22cc7d4ef5.",
            createStringType()));
    columns.put(
        "email_address",
        createColumn(
            "email_address",
            "The email address of the user. Depending on the user's privacy setting, this may be returned as null.",
            createStringType()));
    columns.put(
        "account_type",
        createColumn(
            "account_type",
            "The user account type. Can take the following values: atlassian, app, customer and unknown.",
            createStringType()));
    columns.put("active", createColumn("active", "Indicates if user is active.", createBoolType()));
    columns.put("self", createColumn("self", "The URL of the user.", createStringType()));
    columns.put(
        "avatar_urls", createColumn("avatar_urls", "The avatars of the user.", createAnyType()));
    columns.put(
        "group_names",
        createColumn(
            "group_names",
            "The groups that the user belongs to.",
            createListType(createStringType())));
    columns.put("title", createColumn("title", TITLE_DESC, createStringType()));
    return columns;
  }
}
