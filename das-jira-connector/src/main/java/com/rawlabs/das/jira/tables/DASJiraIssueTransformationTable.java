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
      Map<String, Object> fields,
      Map<String, String> names,
      Row.Builder rowBuilder,
      List<String> columns) {

    var maybeFields = Optional.ofNullable(fields);

    var project =
        maybeFields.map(f -> f.get(names.get("Project"))).map(p -> (Map<String, Object>) p);

    addToRow("project_key", rowBuilder, project.map(p -> p.get("key")).orElse(null), columns);
    addToRow("project_id", rowBuilder, project.map(p -> p.get("id")).orElse(null), columns);

    var status = maybeFields.map(f -> f.get(names.get("Status"))).map(p -> (Map<String, Object>) p);

    addToRow("status", rowBuilder, status.map(s -> s.get("name")).orElse(null), columns);

    var assignee =
        maybeFields.map(f -> f.get(names.get("Assignee"))).map(p -> (Map<String, Object>) p);

    addToRow(
        "assignee_account_id",
        rowBuilder,
        assignee.map(a -> a.get("accountId")).orElse(null),
        columns);

    addToRow(
        "assignee_email_address",
        rowBuilder,
        assignee.map(a -> a.get("emailAddress")).orElse(null),
        columns);

    addToRow(
        "assignee_display_name",
        rowBuilder,
        assignee.map(a -> a.get("displayName")).orElse(null),
        columns);

    addToRow(
        "created",
        rowBuilder,
        maybeFields.map(f -> f.get(names.get("Created"))).orElse(null),
        columns);

    var creator =
        maybeFields.map(f -> f.get(names.get("Creator"))).map(p -> (Map<String, Object>) p);

    addToRow(
        "creator_account_id",
        rowBuilder,
        creator.map(c -> c.get("accountId")).orElse(null),
        columns);

    addToRow(
        "creator_email_address",
        rowBuilder,
        creator.map(c -> c.get("emailAddress")).orElse(null),
        columns);

    addToRow(
        "creator_display_name",
        rowBuilder,
        creator.map(c -> c.get("displayName")).orElse(null),
        columns);

    var description = maybeFields.map(f -> f.get(names.get("Description"))).orElse(null);
    try {
      // 'description' is an object, so we need to serialize it. It's then sent as 'any'.
      addToRow("description", rowBuilder, objectMapper.writeValueAsString(description), columns);
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException("error processing 'description'", e);
    }

    addToRow(
        "due_date",
        rowBuilder,
        maybeFields.map(f -> f.get(names.get("Due date"))).orElse(null),
        columns);

    var priority =
        maybeFields.map(f -> f.get(names.get("Priority"))).map(p -> (Map<String, Object>) p);

    addToRow("priority", rowBuilder, priority.map(p -> p.get("name")).orElse(null), columns);

    var reporter =
        maybeFields.map(f -> f.get(names.get("Reporter"))).map(p -> (Map<String, Object>) p);

    addToRow(
        "reporter_account_id",
        rowBuilder,
        reporter.map(r -> r.get("accountId")).orElse(null),
        columns);
    addToRow(
        "reporter_display_name",
        rowBuilder,
        reporter.map(r -> r.get("displayName")).orElse(null),
        columns);

    addToRow(
        "summary",
        rowBuilder,
        maybeFields.map(f -> f.get(names.get("Summary"))).orElse(null),
        columns);

    var type =
        maybeFields
            .map(f -> (Map<String, Object>) f.get(names.get("Issue Type")))
            .map(i -> i.get("name"))
            .orElse(null);

    addToRow("type", rowBuilder, type, columns);

    addToRow(
        "updated",
        rowBuilder,
        maybeFields.map(f -> f.get(names.get("Updated"))).orElse(null),
        columns);

    var componentIds =
        maybeFields
            .map(f -> ((ArrayList<Object>) f.get(names.get("Components"))))
            .map(
                c -> {
                  List<String> cmpIds = new ArrayList<>();
                  c.forEach(comp -> cmpIds.add(((Map<String, Object>) comp).get("id").toString()));
                  return cmpIds;
                })
            .orElse(null);

    addToRow("components", rowBuilder, componentIds, columns);

    try {
      addToRow("fields", rowBuilder, objectMapper.writeValueAsString(fields), columns);
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException(e.getMessage());
    }

    addToRow(
        "labels",
        rowBuilder,
        maybeFields.map(f -> f.get(names.get("Labels"))).orElse(null),
        columns);

    // 'tags' is a map of labels, all mapped to boolean true. It is in the steampipe schema. We
    // advertise a
    // similar field in the DAS schema.
    Map<String, Boolean> tags =
        maybeFields
            .map(f -> (List<String>) f.get(names.get("Labels")))
            .map(
                l -> {
                  Map<String, Boolean> tagsMap = new HashMap<>();
                  l.forEach(label -> tagsMap.put(label, true));
                  return tagsMap;
                })
            .orElse(null);

    try {
      addToRow("tags", rowBuilder, objectMapper.writeValueAsString(tags), columns);
    } catch (JsonProcessingException e) {
      throw new DASSdkApiException(e.getMessage());
    }
  }
}
