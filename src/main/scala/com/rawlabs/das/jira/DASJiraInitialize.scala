package com.rawlabs.das.jira

import com.rawlabs.das.rest.jira.{ApiClient, Configuration}
import com.rawlabs.das.rest.jira.auth.{HttpBasicAuth, HttpBearerAuth}

object DASJiraInitialize {
  def apply(options: Map[String, String]): Unit = {
    if (Configuration.getDefaultApiClient == null) new DASJiraInitialize(options).initialize()
  }
}

class DASJiraInitialize(options: Map[String, String]) {
  private def initialize(): Unit = Configuration.setDefaultApiClient {
    val apiClient = new ApiClient().setBasePath(options("base_url"))
    setupAuthentication(apiClient)
    apiClient
  }

  private def setupAuthentication(apiClient: ApiClient): Unit =
    if (isBasicAuth) setupBasicAuth(apiClient)
    else if (isBearerAuth) setupBearerAuth(apiClient)
    else throw new IllegalArgumentException("Invalid authentication options")

  private def isBasicAuth: Boolean = options.contains("username") && options.contains("token")

  private def setupBasicAuth(apiClient: ApiClient): Unit = {
    val basic = apiClient.getAuthentication("basicAuth").asInstanceOf[HttpBasicAuth]
    basic.setUsername(options("username"))
    basic.setPassword(options("token"))
  }

  private def isBearerAuth: Boolean = options.contains("personal_access_token")

  private def setupBearerAuth(apiClient: ApiClient): Unit =
    apiClient.getAuthentication("OAuth2").asInstanceOf[HttpBearerAuth].setBearerToken(options("personal_access_token"))
}
