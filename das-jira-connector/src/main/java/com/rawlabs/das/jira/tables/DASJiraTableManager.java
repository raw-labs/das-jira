package com.rawlabs.das.jira.tables;

import com.rawlabs.das.jira.rest.platform.api.*;
import com.rawlabs.das.jira.rest.software.api.BoardApi;
import com.rawlabs.das.jira.rest.software.api.EpicApi;
import com.rawlabs.das.jira.rest.software.api.IssueApi;
import com.rawlabs.das.jira.rest.software.api.SprintApi;
import com.rawlabs.das.jira.tables.definitions.*;
import com.rawlabs.das.jira.tables.definitions.DASJiraAdvancedSettingsTable;
import com.rawlabs.das.jira.tables.definitions.DASJiraBoardTable;
import com.rawlabs.protocol.das.TableDefinition;

import java.util.List;
import java.util.Map;

public class DASJiraTableManager {
  private final List<DASJiraTable> tables;
  private final List<TableDefinition> tableDefinitions;

  public DASJiraTableManager(
      Map<String, String> options,
      com.rawlabs.das.jira.rest.platform.ApiClient apiClientPlatform,
      com.rawlabs.das.jira.rest.software.ApiClient apiClientSoftware) {
    tables =
        List.of(
            new DASJiraAdvancedSettingsTable(options, new JiraSettingsApi(apiClientPlatform)),
            new DASJiraBacklogIssueTable(options, new BoardApi(apiClientSoftware)),
            new DASJiraBoardTable(options, new BoardApi(apiClientSoftware)),
            new DASJiraComponentTable(
                options,
                new ProjectComponentsApi(apiClientPlatform),
                new ProjectsApi(apiClientPlatform)),
            new DASJiraDashboardTable(options, new DashboardsApi(apiClientPlatform)),
            new DASJiraEpicTable(options, new EpicApi(apiClientSoftware)),
            new DASJiraGlobalSettingTable(options, new JiraSettingsApi(apiClientPlatform)),
            new DASJiraGroupTable(options, new GroupsApi(apiClientPlatform)),
            new DASJiraIssueCommentTable(
                options,
                new IssueCommentsApi(apiClientPlatform),
                new IssueSearchApi(apiClientPlatform),
                new IssuesApi(apiClientPlatform)),
            new DASJiraIssueTable(
                options, new IssueSearchApi(apiClientPlatform), new IssuesApi(apiClientPlatform)),
            new DASJiraIssueTypeTable(options, new IssueTypesApi(apiClientPlatform)),
            new DASJiraIssueWorklogTable(
                options,
                new IssueWorklogsApi(apiClientPlatform),
                new IssueSearchApi(apiClientPlatform),
                new IssuesApi(apiClientPlatform)),
            new DASJiraProjectRoleTable(options, new ProjectRolesApi(apiClientPlatform)),
            new DASJiraProjectTable(options, new ProjectsApi(apiClientPlatform)),
            new DASJiraSprintTable(
                options, new SprintApi(apiClientSoftware), new BoardApi(apiClientSoftware)),
            new DASJiraUserTable(options, new UsersApi(apiClientPlatform)),
            new DASJiraWorkflowTable(options, new WorkflowsApi(apiClientPlatform)));
    tableDefinitions = tables.stream().map(DASJiraTable::getTableDefinition).toList();
  }

  public List<DASJiraTable> getTables() {
    return tables;
  }

  public List<TableDefinition> getTableDefinitions() {
    return tableDefinitions;
  }

  public DASJiraTable getTable(String tableName) {
    return tables.stream()
        .filter(table -> table.getTableName().equals(tableName))
        .findFirst()
        .orElse(null);
  }
}
