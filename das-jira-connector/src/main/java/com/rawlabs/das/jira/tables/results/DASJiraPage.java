package com.rawlabs.das.jira.tables.results;

import java.util.List;

public record DASJiraPage<T>(List<T> result, Long total) {}
