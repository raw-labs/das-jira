package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.auth.DASJiraOAuth2AuthStrategy;
import com.rawlabs.das.jira.initializer.auth.DasJiraBasicAuthStrategy;
import com.rawlabs.das.rest.jira.ApiClient;
import com.rawlabs.das.rest.jira.Configuration;
import com.rawlabs.das.rest.jira.auth.Authentication;
import com.rawlabs.das.rest.jira.auth.HttpBasicAuth;
import com.rawlabs.das.rest.jira.auth.OAuth;
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
  @DisplayName("Initialization succeeds when basic auth is provided")
  public void testJiraInitializerWithBasicAuth() {
    DASJiraInitializer.initialize(
        Map.of("base_url", "http://localhost:8080", "username", "admin", "token", "password"));
    ApiClient apiClient = Configuration.getDefaultApiClient();
    assertEquals("http://localhost:8080", apiClient.getBasePath());
    Authentication auth = apiClient.getAuthentication(DasJiraBasicAuthStrategy.NAME);
    assertInstanceOf(HttpBasicAuth.class, auth);
    HttpBasicAuth basicAuth = (HttpBasicAuth) auth;
    assertEquals("admin", basicAuth.getUsername());
    assertEquals("password", basicAuth.getPassword());
  }

  @Test
  @DisplayName("Initialization succeeds when token auth is provided")
  public void testJiraInitializerWithOAuth() {
    DASJiraInitializer.initialize(
        Map.of("base_url", "http://localhost:8080", "personal_access_token", "pat"));
    ApiClient apiClient = Configuration.getDefaultApiClient();
    assertEquals("http://localhost:8080", apiClient.getBasePath());
    Authentication auth = apiClient.getAuthentication(DASJiraOAuth2AuthStrategy.NAME);
    assertInstanceOf(OAuth.class, auth);
    OAuth oauth = (OAuth) auth;
    assertEquals("pat", oauth.getAccessToken());
  }
}
