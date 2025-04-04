package com.rawlabs.das.jira;

import static org.junit.jupiter.api.Assertions.*;

import com.rawlabs.das.sdk.DASSdkInvalidArgumentException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("General DAS Jira Test")
public class DASJiraTest {

  @Test
  public void testGetDasType() {
    DASJiraBuilder dasJiraBuilder = new DASJiraBuilder();
    assertNotNull(dasJiraBuilder);
    assertEquals("jira", dasJiraBuilder.getDasType());
  }

  @Test
  public void testMalformedUrl() {
    DASJiraBuilder dasJiraBuilder = new DASJiraBuilder();
    DASSdkInvalidArgumentException exception =
        assertThrows(
            DASSdkInvalidArgumentException.class,
            () ->
                dasJiraBuilder.build(
                    Map.of("base_url", "tralala", "personal_access_token", "pat"), null));
    assertTrue(exception.getMessage().contains("no scheme was found"), exception.getMessage());
  }

  @Test
  public void testUnknownHost() {
    DASJiraBuilder dasJiraBuilder = new DASJiraBuilder();
    DASSdkInvalidArgumentException exception =
        assertThrows(
            DASSdkInvalidArgumentException.class,
            () ->
                dasJiraBuilder.build(
                    Map.of(
                        "base_url",
                        "https://this-doesn-t-exist.com",
                        "personal_access_token",
                        "pat"),
                    null));
    assertTrue(exception.getMessage().contains("this-doesn-t-exist.com"), exception.getMessage());
  }
}
