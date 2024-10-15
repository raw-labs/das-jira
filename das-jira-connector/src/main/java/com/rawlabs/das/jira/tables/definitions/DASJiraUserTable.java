package com.rawlabs.das.jira.tables.definitions;

import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.DashboardsApi;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.api.UsersApi;
import com.rawlabs.das.jira.rest.platform.model.GroupName;
import com.rawlabs.das.jira.rest.platform.model.NewUserDetails;
import com.rawlabs.das.jira.rest.platform.model.User;
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
      return toRow(user);
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
      return fromRowIterator(users.stream().map(this::toRow).iterator());
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  private Row toRow(User user) {
    Row.Builder rowBuilder = Row.newBuilder();
    initRow(rowBuilder);
    addToRow("display_name", rowBuilder, user.getDisplayName());
    addToRow("account_id", rowBuilder, user.getAccountId());
    addToRow("email_address", rowBuilder, user.getEmailAddress());
    Optional.ofNullable(user.getAccountType())
        .ifPresent(accountType -> addToRow("account_type", rowBuilder, accountType.getValue()));
    addToRow("active", rowBuilder, user.getActive());
    Optional.ofNullable(user.getSelf())
        .ifPresent(self -> addToRow("self", rowBuilder, self.toString()));
    Optional.ofNullable(user.getAvatarUrls())
        .ifPresent(avatarUrls -> addToRow("avatar_urls", rowBuilder, avatarUrls.toJson()));
    Optional.ofNullable(user.getGroups())
        .flatMap(groups -> Optional.ofNullable(groups.getItems()))
        .ifPresent(
            group ->
                addToRow(
                    "group_names", rowBuilder, group.stream().map(GroupName::getName).toList()));
    return rowBuilder.build();
  }

  @Override
  protected Map<String, ColumnDefinition> buildColumnDefinitions() {
    Map<String, ColumnDefinition> columns = new HashMap<>();
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
