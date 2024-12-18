package com.rawlabs.das.jira.initializer;

import com.rawlabs.das.jira.initializer.auth.DASJiraOAuth2AuthStrategy;
import com.rawlabs.das.jira.initializer.auth.DasJiraBasicAuthStrategy;
import com.rawlabs.das.jira.rest.platform.ApiClient;
import com.rawlabs.das.jira.rest.platform.Configuration;
import com.rawlabs.das.jira.rest.platform.auth.Authentication;
import com.rawlabs.das.jira.rest.platform.auth.HttpBasicAuth;
import com.rawlabs.das.jira.rest.platform.auth.OAuth;
import okhttp3.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DAS Jira Initializer Test")
public class DASJiraInitializerTest {

  @Test
  @DisplayName("Initialization fails when auth not provided")
  public void testJiraInitializer() {
    assertThrows(
        IllegalArgumentException.class,
        () -> DASJiraInitializer.initializeSoftware(Map.of("base_url", "http://localhost:8080")));
  }

  @Test
  @DisplayName("Initialization succeeds when basic auth is provided")
  public void testJiraInitializerWithBasicAuth() {
    ApiClient platformApiClient =
        DASJiraInitializer.initializePlatform(
            Map.of("base_url", "http://localhost:8080", "username", "admin", "token", "password"));
    assertEquals("http://localhost:8080", platformApiClient.getBasePath());
    Authentication auth = platformApiClient.getAuthentication(DasJiraBasicAuthStrategy.NAME);
    assertInstanceOf(HttpBasicAuth.class, auth);
    HttpBasicAuth platformBasicAuth = (HttpBasicAuth) auth;
    assertEquals("admin", platformBasicAuth.getUsername());
    assertEquals("password", platformBasicAuth.getPassword());

    com.rawlabs.das.jira.rest.software.ApiClient softwareApiClient =
        DASJiraInitializer.initializeSoftware(
            Map.of("base_url", "http://localhost:8080", "username", "admin", "token", "password"));
    assertEquals("http://localhost:8080", softwareApiClient.getBasePath());
    com.rawlabs.das.jira.rest.software.auth.Authentication softwareAuth =
        softwareApiClient.getAuthentication(DasJiraBasicAuthStrategy.NAME);
    assertInstanceOf(com.rawlabs.das.jira.rest.software.auth.HttpBasicAuth.class, softwareAuth);
    Request.Builder reqBuilder = new Request.Builder();
    reqBuilder.url("http://localhost:8080");
    softwareApiClient.processHeaderParams(Map.of(), reqBuilder);
    assertTrue(
        Objects.requireNonNull(reqBuilder.build().header("Authorization")).startsWith("Basic"));
  }

  @Test
  @DisplayName("Initialization succeeds when token auth is provided")
  public void testJiraInitializerWithBearer() {
    ApiClient apiClient =
        DASJiraInitializer.initializePlatform(
            Map.of("base_url", "http://localhost:8080", "personal_access_token", "pat"));
    assertEquals("http://localhost:8080", apiClient.getBasePath());
    Authentication auth = apiClient.getAuthentication(DASJiraOAuth2AuthStrategy.NAME);
    assertInstanceOf(OAuth.class, auth);
    OAuth httpBearerAuth = (OAuth) auth;
    assertEquals("pat", ((OAuth) auth).getAccessToken());

    com.rawlabs.das.jira.rest.software.ApiClient softwareApiClient =
        DASJiraInitializer.initializeSoftware(
            Map.of("base_url", "http://localhost:8080", "personal_access_token", "pat"));
    assertEquals("http://localhost:8080", softwareApiClient.getBasePath());
    com.rawlabs.das.jira.rest.software.auth.Authentication softwareAuth =
        softwareApiClient.getAuthentication(DASJiraOAuth2AuthStrategy.NAME);
    assertInstanceOf(com.rawlabs.das.jira.rest.software.auth.OAuth.class, softwareAuth);
    com.rawlabs.das.jira.rest.software.auth.OAuth softwareBearerAuth =
        (com.rawlabs.das.jira.rest.software.auth.OAuth) softwareAuth;
    assertEquals("pat", softwareBearerAuth.getAccessToken());
  }
}
