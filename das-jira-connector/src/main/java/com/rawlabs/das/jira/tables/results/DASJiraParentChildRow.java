package com.rawlabs.das.jira.tables.results;

import com.rawlabs.protocol.das.v1.tables.Row;

public record DASJiraParentChildRow<T>(Row parentRow, T childResult) {}
