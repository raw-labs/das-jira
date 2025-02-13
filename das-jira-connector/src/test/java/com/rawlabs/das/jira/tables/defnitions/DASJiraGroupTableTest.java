package com.rawlabs.das.jira.tables.defnitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.GroupsApi;
import com.rawlabs.das.jira.rest.platform.model.PageBeanGroupDetails;
import com.rawlabs.das.jira.rest.platform.model.PageBeanUserDetails;
import com.rawlabs.das.jira.tables.definitions.DASJiraGroupTable;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DAS Jira Group Table")
public class DASJiraGroupTableTest extends BaseMockTest {
  @Mock static GroupsApi groupsApi;

  @InjectMocks DASJiraGroupTable DASJiraGroupTable;

  private static PageBeanUserDetails pageBeanUserDetails;
  private static PageBeanGroupDetails pageBeanGroupDetails;

  @BeforeAll
  static void beforeAll() throws IOException {
    JsonNode node = loadJson("groups.json");
    pageBeanGroupDetails = PageBeanGroupDetails.fromJson(node.toString());

    JsonNode userForGroups = loadJson("users-for-groups.json");
    pageBeanUserDetails = PageBeanUserDetails.fromJson(userForGroups.toString());
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(groupsApi.getUsersFromGroup(any(), any(), any(), any(), any()))
        .thenReturn(pageBeanUserDetails);
    when(groupsApi.bulkGetGroups(any(), any(), any(), any(), any(), any()))
        .thenReturn(pageBeanGroupDetails);
  }

  @Test
  @DisplayName("Get groups")
  @SuppressWarnings("unchecked")
  void testGetGroups() throws IOException {
    try (var result = DASJiraGroupTable.execute(List.of(), List.of(), List.of(), null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals(
          "276f955c-63d7-42c8-9520-92d01dca0625", extractValueFactory.extractValue(row, "id"));
      assertEquals("jdog-developers", extractValueFactory.extractValue(row, "name"));
      List<String> userIds = (List<String>) extractValueFactory.extractValue(row, "member_ids");
      assertEquals("5b10a2844c20165700ede21g", userIds.getFirst());
      assertTrue(result.hasNext());
      result.next();
      assertFalse(result.hasNext());
    }
  }
}
