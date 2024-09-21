package com.rawlabs.das.jira.connector

import com.rawlabs.das.jira.DASJiraConnector
import com.rawlabs.das.rest.jira.Configuration
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class JiraConnectorTest extends AnyFlatSpec with should.Matchers with PrivateMethodTester {

  "connector" should "fail if no user credentials are specified" in {
    a[IllegalArgumentException] should be thrownBy {
      val options = Map("base_url" -> "http://localhost:8080")
      new DASJiraConnector(options)
    }
  }

  it should "be created with proper auth data" in {
    val options = Map("base_url" -> "http://localhost:8080", "username" -> "user", "token" -> "password")
    val connector = new DASJiraConnector(options)
    connector should not be null
    val apiClient = Configuration.getDefaultApiClient
    apiClient.getBasePath should be("http://localhost:8080")
    val auth = apiClient.getAuthentication("basicAuth")
    auth should not be null
    auth.isInstanceOf[com.rawlabs.das.rest.jira.auth.HttpBasicAuth] should be(true)
    val basic = auth.asInstanceOf[com.rawlabs.das.rest.jira.auth.HttpBasicAuth]
    basic.getUsername should be("user")
    basic.getPassword should be("password")
  }

}
