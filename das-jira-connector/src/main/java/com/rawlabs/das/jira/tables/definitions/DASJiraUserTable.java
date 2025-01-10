package com.rawlabs.das.jira.tables.definitions;

import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.UsersApi;
import com.rawlabs.das.jira.rest.platform.model.*;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.sdk.DASExecuteResult;
import com.rawlabs.protocol.das.v1.query.PathKey;
import com.rawlabs.protocol.das.v1.query.Qual;
import com.rawlabs.protocol.das.v1.query.SortKey;
import com.rawlabs.protocol.das.v1.tables.ColumnDefinition;
import com.rawlabs.protocol.das.v1.tables.Row;
import com.rawlabs.protocol.das.v1.types.Value;
import java.util.*;
import org.jetbrains.annotations.Nullable;

public class DASJiraUserTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_user";

  private final UsersApi usersApi;

  public DASJiraUserTable(Map<String, String> options, UsersApi usersApi) {
    super(options, TABLE_NAME, "User in the Jira cloud.");
    this.usersApi = usersApi;
  }

  public String uniqueColumn() {
    return "account_id";
  }

  public List<PathKey> getTablePathKeys() {
    return List.of(PathKey.newBuilder().addKeyColumns("account_id").build());
  }

  @Override
  public List<Row> bulkInsert(List<Row> rows) {
    return rows.stream().map(this::insert).toList();
  }

  @Override
  public Row insert(Row row) {
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
  public void delete(Value rowId) {
    try {
      usersApi.removeUser(extractValueFactory.extractValue(rowId).toString(), null, null);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DASExecuteResult execute(
      List<Qual> quals, List<String> columns, @Nullable List<SortKey> sortKeys) {
    try {
      List<User> users = usersApi.getAllUsers(0, withMaxResultOrLimit(null));
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
