
package com.codebridge.backend.maven;

import com.codebridge.backend.dto.MavenComparisonResponse;
import com.codebridge.backend.github.GitHubPomClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MavenRepositoryComparisonServiceTest {

    private GitHubPomClient githubPomClient;
    private MavenRepositoryComparisonService service;

    private static final String EXISTING_URL =
            "https://github.com/example/existing-app";

    private static final String INCOMING_URL =
            "https://github.com/example/incoming-app";

    @BeforeEach
    void setUp() {
        githubPomClient = mock(GitHubPomClient.class);

        service = new MavenRepositoryComparisonService(
                githubPomClient,
                new MavenPomParser(),
                new MavenDependencyComparisonService()
        );
    }

    @Test
    void shouldDetectDependencyConflictBetweenRepositories() {

        String existingXml = pomWithDependency(
                "org.slf4j", "slf4j-api", "1.7.36"
        );

        String incomingXml = pomWithDependency(
                "org.slf4j", "slf4j-api", "2.0.17"
        );

        mockPomFiles(existingXml, incomingXml);

        List<MavenDependencyConflict> conflicts =
                service.compareRepositories(EXISTING_URL, INCOMING_URL);

        assertEquals(1, conflicts.size());
        assertEquals("org.slf4j", conflicts.get(0).groupId());
        assertEquals("slf4j-api", conflicts.get(0).artifactId());
        assertEquals("1.7.36", conflicts.get(0).existingVersion());
        assertEquals("2.0.17", conflicts.get(0).incomingVersion());
    }

    @Test
    void shouldReturnNoConflictsWhenVersionsMatch() {

        String xml = pomWithDependency(
                "org.slf4j", "slf4j-api", "2.0.17"
        );

        mockPomFiles(xml, xml);

        assertTrue(
                service.compareRepositories(
                        EXISTING_URL,
                        INCOMING_URL
                ).isEmpty()
        );
    }

    @Test
    void shouldResolveMavenPropertiesBeforeComparison() {

        String existingXml = """
                <project>
                    <properties>
                        <slf4j.version>1.7.36</slf4j.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>${slf4j.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        String incomingXml = """
                <project>
                    <properties>
                        <slf4j.version>2.0.17</slf4j.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>${slf4j.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        mockPomFiles(existingXml, incomingXml);

        List<MavenDependencyConflict> conflicts =
                service.compareRepositories(EXISTING_URL, INCOMING_URL);

        assertEquals(1, conflicts.size());
        assertEquals("1.7.36", conflicts.get(0).existingVersion());
        assertEquals("2.0.17", conflicts.get(0).incomingVersion());
    }

    @Test
    void shouldReturnEmptyWhenNoDependenciesExist() {

        String xml = """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                </project>
                """;

        mockPomFiles(xml, xml);

        assertTrue(
                service.compareRepositories(
                        EXISTING_URL,
                        INCOMING_URL
                ).isEmpty()
        );
    }

    @Test
    void shouldRejectNullRepositoryUrlsWithoutFetching() {

        assertThrows(
                NullPointerException.class,
                () -> service.compareRepositories(null, INCOMING_URL)
        );

        assertThrows(
                NullPointerException.class,
                () -> service.compareRepositories(EXISTING_URL, null)
        );

        verifyNoInteractions(githubPomClient);
    }

    @Test
    void shouldRejectMalformedPomContent() {

        mockPomFiles(
                "<project><dependencies>",
                "<project></project>"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.compareRepositories(
                        EXISTING_URL,
                        INCOMING_URL
                )
        );
    }

    @Test
    void shouldReturnCorrectDependencyCounts() {

        String existingXml = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>1.7.36</version>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>5.12.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        String incomingXml = pomWithDependency(
                "org.slf4j", "slf4j-api", "2.0.17"
        );

        mockPomFiles(existingXml, incomingXml);

        MavenComparisonResponse result =
                service.analyzeRepositories(EXISTING_URL, INCOMING_URL);

        assertEquals("example/existing-app", result.existingRepository());
        assertEquals("example/incoming-app", result.incomingRepository());
        assertEquals(2, result.existingDependencyCount());
        assertEquals(1, result.incomingDependencyCount());
        assertEquals(1, result.potentialConflictCount());
        assertEquals(1, result.conflicts().size());
    }

    @Test
    void shouldReturnZeroCountsForEmptyPoms() {

        String emptyPom = "<project></project>";

        mockPomFiles(emptyPom, emptyPom);

        MavenComparisonResponse result =
                service.analyzeRepositories(EXISTING_URL, INCOMING_URL);

        assertEquals(0, result.existingDependencyCount());
        assertEquals(0, result.incomingDependencyCount());
        assertEquals(0, result.potentialConflictCount());
        assertTrue(result.conflicts().isEmpty());
    }

    @Test
    void shouldRejectInvalidUrlBeforeFetching() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.analyzeRepositories(
                        "https://evil.com/example/repo",
                        INCOMING_URL
                )
        );

        verifyNoInteractions(githubPomClient);
    }

    private String pomWithDependency(
            String groupId,
            String artifactId,
            String version) {

        return """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s</artifactId>
                            <version>%s</version>
                        </dependency>
                    </dependencies>
                </project>
                """.formatted(groupId, artifactId, version);
    }

    private void mockPomFiles(
            String existingXml,
            String incomingXml) {

        when(githubPomClient.getRootPom(EXISTING_URL))
                .thenReturn(new MavenPomFile(
                        "example/existing-app",
                        "pom.xml",
                        existingXml
                ));

        when(githubPomClient.getRootPom(INCOMING_URL))
                .thenReturn(new MavenPomFile(
                        "example/incoming-app",
                        "pom.xml",
                        incomingXml
                ));
    }
}
