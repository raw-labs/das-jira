package com.rawlabs.das.jira.tables.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.ProjectsApi;
import com.rawlabs.das.jira.rest.platform.model.*;
import com.rawlabs.das.jira.tables.DASJiraTable;
import com.rawlabs.das.jira.tables.results.DASJiraPage;
import com.rawlabs.das.jira.tables.results.DASJiraPaginatedResult;
import com.rawlabs.das.sdk.java.DASExecuteResult;
import com.rawlabs.das.sdk.java.KeyColumns;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.das.sdk.java.exceptions.DASSdkException;
import com.rawlabs.protocol.das.ColumnDefinition;
import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.Row;
import com.rawlabs.protocol.das.SortKey;
import com.rawlabs.protocol.raw.Value;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.rawlabs.das.sdk.java.utils.factory.qual.ExtractQualFactory.extractEqDistinct;
import static com.rawlabs.das.sdk.java.utils.factory.table.ColumnFactory.createColumn;
import static com.rawlabs.das.sdk.java.utils.factory.type.TypeFactory.*;

public class DASJiraProjectTable extends DASJiraTable {

  public static final String TABLE_NAME = "jira_project";

  private ProjectsApi projectsApi = new ProjectsApi();

  private final String expand = "description,lead,issueTypes,url,projectKeys,permissions,insight";

  public DASJiraProjectTable(Map<String, String> options) {
    super(options, TABLE_NAME, "Project is a collection of issues (stories, bugs, tasks, etc).");
  }

  public DASJiraProjectTable(Map<String, String> options, ProjectsApi projectsApi) {
    this(options);
    this.projectsApi = projectsApi;
  }

  @Override
  public List<SortKey> canSort(List<SortKey> sortKeys) {
    List<String> availableForSorting = List.of("name", "title", "key");
    return sortKeys.stream()
        .filter(sortKey -> availableForSorting.contains(sortKey.getName()))
        .toList();
  }

  @Override
  public List<KeyColumns> getPathKeys() {
    return List.of(new KeyColumns(List.of("id"), 1), new KeyColumns(List.of("key"), 1));
  }

  @Override
  public String getUniqueColumn() {
    return "id";
  }

  @Override
  public Row insertRow(Row row) {
    try {
      CreateProjectDetails createProjectDetails = new CreateProjectDetails();
      createProjectDetails.setName((String) extractValueFactory.extractValue(row, "name"));
      createProjectDetails.setDescription(
          (String) extractValueFactory.extractValue(row, "description"));
      createProjectDetails.setLeadAccountId(
          (String) extractValueFactory.extractValue(row, "lead_account_id"));
      createProjectDetails.setProjectTypeKey(
          CreateProjectDetails.ProjectTypeKeyEnum.fromValue(
              (String) extractValueFactory.extractValue(row, "project_type_key")));
      createProjectDetails.setUrl((String) extractValueFactory.extractValue(row, "url"));

      ProjectIdentifiers projectIdentifiers = projectsApi.createProject(createProjectDetails);
      Row.Builder rowBuilder = Row.newBuilder();
      addToRow("id", rowBuilder, projectIdentifiers.getId(), List.of());
      addToRow("key", rowBuilder, projectIdentifiers.getKey(), List.of());
      addToRow("self", rowBuilder, projectIdentifiers.getSelf(), List.of());
      return rowBuilder.build();
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteRow(Value rowId) {
    try {
      projectsApi.deleteProject((String) extractValueFactory.extractValue(rowId), true);
    } catch (ApiException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }

  @Override
  public List<Row> insertRows(List<Row> rows) {
    return rows.stream().map(this::insertRow).toList();
  }

  @Override
  public Row updateRow(Value rowId, Row newValues) {
    try {
      UpdateProjectDetails updateProjectDetails = new UpdateProjectDetails();
      updateProjectDetails.setName((String) extractValueFactory.extractValue(newValues, "name"));
      updateProjectDetails.setDescription(
          (String) extractValueFactory.extractValue(newValues, "description"));
      updateProjectDetails.setLeadAccountId(
          (String) extractValueFactory.extractValue(newValues, "lead_account_id"));
      updateProjectDetails.setUrl((String) extractValueFactory.extractValue(newValues, "url"));

      Project result =
          projectsApi.updateProject(
              (String) extractValueFactory.extractValue(rowId), updateProjectDetails, expand);
      return toRow(result, List.of());
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

    Set<Long> ids =
        Optional.ofNullable(extractEqDistinct(quals, "id"))
            .map(i -> Set.of(Long.parseLong((String) i)))
            .orElse(null);

    Set<String> keys =
        Optional.ofNullable(extractEqDistinct(quals, "key"))
            .map(i -> Set.of((String) i))
            .orElse(null);

    return new DASJiraPaginatedResult<Project>(limit) {

      @Override
      public Row next() {
        Project project = this.getNext();
        return toRow(project, columns);
      }

      @Override
      public DASJiraPage<Project> fetchPage(long offset) {
        try {
          PageBeanProject searchResult =
              projectsApi.searchProjects(
                  offset,
                  withMaxResultOrLimit(limit),
                  withOrderBy(sortKeys),
                  ids,
                  keys,
                  null,
                  null,
                  null,
                  null,
                  expand,
                  null,
                  null,
                  null);
          return new DASJiraPage<>(searchResult.getValues(), searchResult.getTotal());
        } catch (ApiException e) {
          throw new DASSdkApiException(e.getMessage(), e);
        }
      }
    };
  }

  private Row toRow(Project project, List<String> columns) {
    Row.Builder rowBuilder = Row.newBuilder();
    this.addToRow("id", rowBuilder, project.getId(), columns);
    this.addToRow("name", rowBuilder, project.getName(), columns);
    this.addToRow("key", rowBuilder, project.getKey(), columns);

    var self = Optional.ofNullable(project.getSelf()).map(Object::toString).orElse(null);
    this.addToRow("self", rowBuilder, self, columns);

    this.addToRow("description", rowBuilder, project.getDescription(), columns);
    this.addToRow("email", rowBuilder, project.getEmail(), columns);

    var lead = Optional.ofNullable(project.getLead());
    this.addToRow(
        "lead_account_id", rowBuilder, lead.map(User::getAccountId).orElse(null), columns);
    this.addToRow(
        "lead_display_name", rowBuilder, lead.map(User::getDisplayName).orElse(null), columns);

    var projectTypeKey = Optional.ofNullable(project.getProjectTypeKey());
    this.addToRow("project_type_key", rowBuilder, projectTypeKey.orElse(null), columns);

    var url = Optional.ofNullable(project.getUrl());
    this.addToRow("url", rowBuilder, url.orElse(null), columns);

    var componentIds =
        Optional.ofNullable(project.getComponents())
            .map(components -> components.stream().map(ProjectComponent::getId).toList());
    this.addToRow("component_ids", rowBuilder, componentIds.orElse(null), columns);

    var properties =
        Optional.ofNullable(project.getProperties())
            .map(
                p -> {
                  try {
                    return objectMapper.writeValueAsString(p);
                  } catch (JsonProcessingException e) {
                    throw new DASSdkException(e.getMessage(), e);
                  }
                })
            .orElse(null);

    this.addToRow("properties", rowBuilder, properties, columns);

    var issueTypes =
        Optional.ofNullable(project.getIssueTypes())
            .map(l -> l.stream().map(IssueTypeDetails::toJson).toList())
            .orElse(null);

    this.addToRow("issue_types", rowBuilder, issueTypes, columns);

    var projectCategory =
        Optional.ofNullable(project.getProjectCategory()).map(ProjectCategory::toJson);
    this.addToRow("project_category", rowBuilder, projectCategory.orElse(null), columns);
    this.addToRow("title", rowBuilder, project.getName(), columns);
    return rowBuilder.build();
  }

  @Override
  protected LinkedHashMap<String, ColumnDefinition> buildColumnDefinitions() {
    LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();
    columns.put("id", createColumn("id", "The ID of the project.", createStringType()));
    columns.put("name", createColumn("name", "The name of the project.", createStringType()));
    columns.put("key", createColumn("key", "The key of the project.", createStringType()));
    columns.put(
        "self", createColumn("self", "The URL of the project details.", createStringType()));
    columns.put(
        "description",
        createColumn("description", "A brief description of the project.", createStringType()));
    columns.put(
        "email",
        createColumn("email", "An email address associated with the project.", createStringType()));
    columns.put(
        "lead_account_id",
        createColumn(
            "lead_account_id", "The user account id of the project lead.", createStringType()));
    columns.put(
        "lead_display_name",
        createColumn(
            "lead_display_name", "The user display name of the project lead.", createStringType()));
    columns.put(
        "project_type_key",
        createColumn(
            "project_type_key",
            "The project type of the project. Valid values are software, service_desk and business.",
            createStringType()));
    columns.put(
        "url",
        createColumn(
            "url",
            "A link to information about this project, such as project documentation.",
            createStringType()));
    columns.put(
        "component_ids",
        createColumn(
            "component_ids",
            "List of the components contained in the project.",
            createListType(createStringType())));
    columns.put(
        "properties",
        createColumn(
            "properties",
            "This resource represents project properties, which provide for storing custom data against a project.",
            createAnyType()));
    columns.put(
        "issue_types",
        createColumn(
            "issue_types",
            "List of the issue types available in the project.",
            createListType(createAnyType())));
    columns.put(
        "project_category",
        createColumn(
            "project_category", "The category the project belongs to.", createStringType()));
    columns.put("title", createColumn("title", "The name of the project.", createStringType()));
    return columns;
  }
}
