package com.rawlabs.das.jira;

/**
 * DASJiraApiException wraps an ApiException (from either the platform or software API) to propagate
 * only the response body as the error message. The original ApiException message often includes
 * extensive HTTP header details that are not useful for DAS clients.
 */
public class DASJiraUnexpectedError extends RuntimeException {

  public DASJiraUnexpectedError(Throwable t) {
    super("unexpected error", t);
  }

}
