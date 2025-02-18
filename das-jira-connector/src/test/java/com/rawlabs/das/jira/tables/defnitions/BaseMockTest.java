package com.rawlabs.das.jira.tables.defnitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.stream.JsonReader;
import com.rawlabs.das.jira.utils.factory.value.DefaultExtractValueFactory;
import com.rawlabs.das.jira.utils.factory.value.DefaultValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ExtractValueFactory;
import com.rawlabs.das.jira.utils.factory.value.ValueFactory;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class BaseMockTest {

  protected final ValueFactory valueFactory = new DefaultValueFactory();
  protected final ExtractValueFactory extractValueFactory = new DefaultExtractValueFactory();

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
