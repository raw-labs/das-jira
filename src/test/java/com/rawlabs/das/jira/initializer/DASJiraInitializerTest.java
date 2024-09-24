package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.DASJiraInitializer;
import com.rawlabs.das.rest.jira.ApiClient;
import com.rawlabs.das.rest.jira.Configuration;
import com.rawlabs.das.rest.jira.auth.Authentication;
import com.rawlabs.das.rest.jira.auth.HttpBasicAuth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DAS Jira Initializer Test")
public class DASJiraInitializerTest {

  @Test
  @DisplayName("Initialization fails when auth not provided")
  public void testJiraInitializer() {
    assertThrows(
        IllegalArgumentException.class,
        () -> DASJiraInitializer.initialize(Map.of("base_url", "http://localhost:8080")));
  }

  @Test
  @DisplayName("Initialization succeeds when auth is provided")
  public void testJiraInitializerWithAuth() {
    DASJiraInitializer.initialize(
        Map.of("base_url", "http://localhost:8080", "username", "admin", "token", "password"));
    ApiClient apiClient = Configuration.getDefaultApiClient();
    assertEquals("http://localhost:8080", apiClient.getBasePath());
    Authentication auth = apiClient.getAuthentication("basicAuth");
    assertInstanceOf(HttpBasicAuth.class, auth);
    HttpBasicAuth basicAuth = (HttpBasicAuth) auth;
    assertEquals("admin", basicAuth.getUsername());
    assertEquals("password", basicAuth.getPassword());
  }
}
