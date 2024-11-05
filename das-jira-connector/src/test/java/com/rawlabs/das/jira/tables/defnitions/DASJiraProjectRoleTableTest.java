package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectRolesApi;
import com.rawlabs.das.jira.rest.platform.model.ProjectRole;
import com.rawlabs.das.jira.tables.definitions.DASJiraProjectRoleTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("DAS Jira Project Role Table Test")
public class DASJiraProjectRoleTableTest extends BaseMockTest {
  @Mock static ProjectRolesApi projectRolesApi;

  @InjectMocks DASJiraProjectRoleTable dasJiraProjectRoleTable;

  private static List<ProjectRole> projectRoles;

  @BeforeAll
  static void beforeAll() throws IOException {
    ArrayNode node = (ArrayNode) loadJson("project-roles.json");

    projectRoles = new ArrayList<>();
    for (JsonNode jsonNode : node) {
      projectRoles.add(ProjectRole.fromJson(jsonNode.toString()));
    }
  }

  @BeforeEach
  void setUp() throws ApiException {
    when(projectRolesApi.getAllProjectRoles()).thenReturn(projectRoles);
  }

  @Test
  @DisplayName("Get project roles")
  @SuppressWarnings("unchecked")
  void testGetProjectRoles() throws IOException {
    try (var result = dasJiraProjectRoleTable.execute(List.of(), List.of(), null, null)) {
      assertNotNull(result);
      assertTrue(result.hasNext());
      var row = result.next();
      assertNotNull(row);
      assertEquals(10360L, extractValueFactory.extractValue(row, "id"));
      assertEquals("Developers", extractValueFactory.extractValue(row, "name"));
      assertEquals(
          "A project role that represents developers in a project",
          extractValueFactory.extractValue(row, "description"));
      List<String> array =
          (List<String>) extractValueFactory.extractValue(row, "actor_account_ids");
      assertEquals(2, array.size());
      assertFalse(result.hasNext());
    }
  }
}
