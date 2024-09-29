package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class MockTest {
  public static JsonNode loadJson(String fileName) throws IOException {
    // Create ObjectMapper instance
    ObjectMapper objectMapper = new ObjectMapper();

    // Get class loader
    ClassLoader classLoader = JsonReader.class.getClassLoader();

    try (InputStream inputStream = classLoader.getResourceAsStream("mock-data/" + fileName)) {
      // Parse the JSON into a JsonNode
      return objectMapper.readTree(inputStream);
    }
  }
}
