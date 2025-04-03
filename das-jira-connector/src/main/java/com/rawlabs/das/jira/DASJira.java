package com.rawlabs.das.jira;

import static com.rawlabs.das.jira.utils.ExceptionHandling.makeSdkException;

import com.rawlabs.das.jira.initializer.DASJiraInitializer;
import com.rawlabs.das.jira.rest.platform.ApiException;
import com.rawlabs.das.jira.rest.platform.api.MyselfApi;
import com.rawlabs.das.jira.tables.DASJiraTableManager;
import com.rawlabs.das.sdk.*;
import com.rawlabs.protocol.das.v1.functions.FunctionDefinition;
import com.rawlabs.protocol.das.v1.tables.TableDefinition;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DASJira implements DASSdk {

  private final DASJiraTableManager tableManager;

  protected DASJira(Map<String, String> options) {
    var apiClientPlatform = DASJiraInitializer.initializePlatform(options);
    var apiClientSoftware = DASJiraInitializer.initializeSoftware(options);
    checkAuthentication(apiClientPlatform);
    tableManager = new DASJiraTableManager(options, apiClientPlatform, apiClientSoftware);
  }

  private void checkAuthentication(com.rawlabs.das.jira.rest.platform.ApiClient apiClientPlatform) {
    MyselfApi api = new MyselfApi(apiClientPlatform);
    try {
      api.getCurrentUser(null);
    } catch (ApiException e) {
      throw makeSdkException(e);
    } catch (IllegalArgumentException e) {
      // That happens if the URL is not valid.
      throw new DASSdkInvalidArgumentException(e.getMessage(), e);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public List<TableDefinition> getTableDefinitions() {
    return tableManager.getTableDefinitions();
  }

  public List<FunctionDefinition> getFunctionDefinitions() {
    return List.of();
  }

  public Optional<DASTable> getTable(String name) {
    return tableManager.getTable(name).map(table -> (DASTable) table);
  }

  public Optional<DASFunction> getFunction(String name) {
    return Optional.empty();
  }
}
