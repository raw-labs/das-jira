package com.rawlabs.das.jira.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rawlabs.das.sdk.java.exceptions.DASSdkApiException;
import com.rawlabs.protocol.das.Row;

import java.util.*;

@SuppressWarnings("unchecked")
public abstract class DASJiraIssueTransformationTable extends DASJiraTable {
  protected DASJiraIssueTransformationTable(
      Map<String, String> options, String table, String description) {
    super(options, table, description);
  }

  protected void processFields(
      Map<String, Object> fields, Map<String, String> names, Row.Builder rowBuilder) {
    Optional.ofNullable(fields.get(names.get("Project")))
        .ifPresent(
            p -> {
              addToRow("project_key", rowBuilder, ((Map<String, Object>) p).get("key"));
              addToRow("project_id", rowBuilder, ((Map<String, Object>) p).get("id"));
            });

    Optional.ofNullable(fields.get(names.getOrDefault("Status", "Status")))
        .ifPresent(
            s -> {
              Map<String, Object> status = (Map<String, Object>) s;
              addToRow("status", rowBuilder, status.get("name"));

              Map<String, Object> statusCategory =
                  (Map<String, Object>) status.get("statusCategory");
              if (statusCategory != null) {
                addToRow("status_category", rowBuilder, statusCategory.get("name"));
              }
            });

    Optional.ofNullable(fields.get(names.get("Assignee")))
        .ifPresent(
            a -> {
              addToRow(
                  "assignee_account_id", rowBuilder, ((Map<String, Object>) a).get("accountId"));
              addToRow(
                  "assignee_display_name",
                  rowBuilder,
                  ((Map<String, Object>) a).get("displayName"));
            });

    addToRow("created", rowBuilder, fields.get(names.get("Created")));

    Optional.ofNullable(fields.get(names.get("Creator")))
        .ifPresent(
            c -> {
              addToRow(
                  "creator_account_id", rowBuilder, ((Map<String, Object>) c).get("accountId"));
              addToRow(
                  "creator_display_name", rowBuilder, ((Map<String, Object>) c).get("displayName"));
            });

    addToRow("description", rowBuilder, fields.get("Description"));
    addToRow("due_date", rowBuilder, fields.get(names.get("Due date")));

    Optional.ofNullable(fields.get(names.get("Priority")))
        .ifPresent(p -> addToRow("priority", rowBuilder, ((Map<String, Object>) p).get("name")));

    Optional.ofNullable(fields.get(names.get("Reporter")))
        .ifPresent(
            r -> {
              addToRow(
                  "reporter_account_id", rowBuilder, ((Map<String, Object>) r).get("accountId"));
              addToRow(
                  "reporter_display_name",
                  rowBuilder,
                  ((Map<String, Object>) r).get("displayName"));
            });

    addToRow("summary", rowBuilder, fields.get("Summary"));

    Optional.ofNullable(fields.get(names.get("Issue Type")))
        .ifPresent(t -> addToRow("type", rowBuilder, ((Map<String, Object>) t).get("name")));

    addToRow("updated", rowBuilder, fields.get(names.get("Updated")));

    ArrayList<Object> components = (ArrayList<Object>) fields.get(names.get("Components"));
    Optional.ofNullable(components)
        .ifPresent(
            _ -> {
              List<String> componentIds = new ArrayList<>();
              components.forEach(
                  comp -> componentIds.add(((Map<String, Object>) comp).get("id").toString()));
              addToRow("components", rowBuilder, componentIds);
            });
    try {
      addToRow("fields", rowBuilder, objectMapper.writeValueAsString(fields));
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException(e.getMessage());
    }

    addToRow("labels", rowBuilder, fields.get(names.get("Labels")));
    Optional.ofNullable(fields.get(names.get("Labels")))
        .ifPresent(
            l -> {
              List<String> labels = (List<String>) l;
              Map<String, Boolean> tags = new HashMap<>();
              labels.forEach(label -> tags.put(label, true));
              try {
                addToRow("tags", rowBuilder, objectMapper.writeValueAsString(tags));
              } catch (JsonProcessingException e) {
                throw new DASSdkApiException(e.getMessage());
              }
            });
  }
}
