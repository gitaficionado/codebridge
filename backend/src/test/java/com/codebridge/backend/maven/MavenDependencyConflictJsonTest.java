
package com.codebridge.backend.maven;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class MavenDependencyConflictJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldIncludeHighSeverityInJson() throws Exception {

        MavenDependencyConflict conflict =
                new MavenDependencyConflict(
                        "org.slf4j",
                        "slf4j-api",
                        "1.7.36",
                        "2.0.17"
                );

        String json = objectMapper.writeValueAsString(conflict);
        JsonNode result = objectMapper.readTree(json);

        assertEquals("org.slf4j", result.get("groupId").asText());
        assertEquals("slf4j-api", result.get("artifactId").asText());
        assertEquals("1.7.36", result.get("existingVersion").asText());
        assertEquals("2.0.17", result.get("incomingVersion").asText());
        assertEquals("HIGH", result.get("severity").asText());
    }

    @Test
    void shouldIncludeUnknownSeverityInJson() throws Exception {

        MavenDependencyConflict conflict =
                new MavenDependencyConflict(
                        "org.example",
                        "example-library",
                        "1.0-SNAPSHOT",
                        "2.0-custom"
                );

        String json = objectMapper.writeValueAsString(conflict);
        JsonNode result = objectMapper.readTree(json);

        assertEquals("UNKNOWN", result.get("severity").asText());
    }
}
