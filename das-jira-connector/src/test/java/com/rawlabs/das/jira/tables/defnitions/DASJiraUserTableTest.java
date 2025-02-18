package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.UsersApi;
import com.rawlabs.das.jira.rest.platform.model.User;
import com.rawlabs.das.jira.tables.definitions.DASJiraUserTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira User Table Test")
public class DASJiraUserTableTest extends BaseMockTest {
  @Mock static UsersApi usersApi;

  @InjectMocks DASJiraUserTable dasJiraUserTable;

  private static List<User> users;

  @BeforeAll
  static void beforeAll() throws IOException {
    ArrayNode node = (ArrayNode) loadJson("users.json");

    users = new ArrayList<>();
    for (JsonNode jsonNode : node) {
      users.add(User.fromJson(jsonNode.toString()));
    }
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(usersApi.getAllUsers(any(), any())).thenReturn(users);
  }

  @Test
  @DisplayName("Get users")
  void testGetUsers() throws IOException {
    try (var result = dasJiraUserTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals("5b10a2844c20165700ede21g", extractValueFactory.extractValue(row, "account_id"));
      assertEquals("Mia Krystof", extractValueFactory.extractValue(row, "display_name"));
      assertEquals(false, extractValueFactory.extractValue(row, "active"));
      assertTrue(result.hasNext());
      row = result.next();
      assertNotNull(row);
      assertFalse(result.hasNext());
    }
  }
}
