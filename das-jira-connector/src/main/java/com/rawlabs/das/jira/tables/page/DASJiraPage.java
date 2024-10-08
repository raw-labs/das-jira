package com.rawlabs.das.jira.tables.page;

import java.util.List;

public record DASJiraPage<T>(List<T> result, Long total) {}
