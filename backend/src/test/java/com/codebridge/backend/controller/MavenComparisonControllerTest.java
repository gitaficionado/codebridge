
package com.codebridge.backend.controller;

import com.codebridge.backend.dto.MavenComparisonRequest;
import com.codebridge.backend.dto.MavenComparisonResponse;
import com.codebridge.backend.maven.ConflictSeverity;
import com.codebridge.backend.maven.MavenDependencyConflict;
import com.codebridge.backend.maven.MavenRepositoryComparisonService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MavenComparisonControllerTest {

    private MavenRepositoryComparisonService comparisonService;
    private MavenComparisonController controller;

    private static final String EXISTING_URL =
            "https://github.com/example/existing-app";

    private static final String INCOMING_URL =
            "https://github.com/example/incoming-app";

    @BeforeEach
    void setUp() {
        comparisonService = mock(MavenRepositoryComparisonService.class);
        controller = new MavenComparisonController(comparisonService);
    }

    @Test
    void shouldReturnDependencyConflictsWithSeverity() {

        MavenComparisonRequest request = new MavenComparisonRequest(
                EXISTING_URL,
                INCOMING_URL
        );

        MavenDependencyConflict conflict = new MavenDependencyConflict(
                "org.slf4j",
                "slf4j-api",
                "1.7.36",
                "2.0.17"
        );

        MavenComparisonResponse expected = new MavenComparisonResponse(
                "example/existing-app",
                "example/incoming-app",
                12,
                15,
                1,
                List.of(conflict)
        );

        when(comparisonService.analyzeRepositories(
                EXISTING_URL,
                INCOMING_URL
        )).thenReturn(expected);

        ResponseEntity<MavenComparisonResponse> response =
                controller.compare(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        MavenComparisonResponse result = response.getBody();

        assertEquals(
                "example/existing-app",
                result.existingRepository()
        );
        assertEquals(
                "example/incoming-app",
                result.incomingRepository()
        );
        assertEquals(12, result.existingDependencyCount());
        assertEquals(15, result.incomingDependencyCount());
        assertEquals(1, result.potentialConflictCount());
        assertEquals(1, result.conflicts().size());

        MavenDependencyConflict actualConflict =
                result.conflicts().get(0);

        assertEquals("org.slf4j", actualConflict.groupId());
        assertEquals("slf4j-api", actualConflict.artifactId());
        assertEquals("1.7.36", actualConflict.existingVersion());
        assertEquals("2.0.17", actualConflict.incomingVersion());

        // Verify that the conflict has the correct severity.
        assertEquals(
                ConflictSeverity.HIGH,
                actualConflict.severity()
        );

        verify(comparisonService).analyzeRepositories(
                EXISTING_URL,
                INCOMING_URL
        );
    }

    @Test
    void shouldReturnEmptyConflictsWhenVersionsMatch() {

        MavenComparisonRequest request = new MavenComparisonRequest(
                EXISTING_URL,
                INCOMING_URL
        );

        MavenComparisonResponse expected = new MavenComparisonResponse(
                "example/existing-app",
                "example/incoming-app",
                5,
                7,
                0,
                List.of()
        );

        when(comparisonService.analyzeRepositories(
                EXISTING_URL,
                INCOMING_URL
        )).thenReturn(expected);

        ResponseEntity<MavenComparisonResponse> response =
                controller.compare(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(5, response.getBody().existingDependencyCount());
        assertEquals(7, response.getBody().incomingDependencyCount());
        assertEquals(0, response.getBody().potentialConflictCount());
        assertTrue(response.getBody().conflicts().isEmpty());
    }

    @Test
    void shouldIncludeMediumSeverityForMinorVersionDifference() {

        MavenDependencyConflict conflict = new MavenDependencyConflict(
                "org.example",
                "example-library",
                "2.0.0",
                "2.1.0"
        );

        assertEquals(ConflictSeverity.MEDIUM, conflict.severity());
    }

    @Test
    void shouldIncludeLowSeverityForPatchVersionDifference() {

        MavenDependencyConflict conflict = new MavenDependencyConflict(
                "org.example",
                "example-library",
                "2.0.16",
                "2.0.17"
        );

        assertEquals(ConflictSeverity.LOW, conflict.severity());
    }

    @Test
    void shouldIncludeUnknownSeverityForNonstandardVersions() {

        MavenDependencyConflict conflict = new MavenDependencyConflict(
                "org.example",
                "example-library",
                "1.0-SNAPSHOT",
                "2.0-custom"
        );

        assertEquals(ConflictSeverity.UNKNOWN, conflict.severity());
    }

    @Test
    void shouldRejectInvalidExistingRepositoryUrl() {

        MavenComparisonRequest request = new MavenComparisonRequest(
                "https://evil.com/example/repository",
                INCOMING_URL
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.compare(request)
        );

        verifyNoInteractions(comparisonService);
    }

    @Test
    void shouldRejectInvalidIncomingRepositoryUrl() {

        MavenComparisonRequest request = new MavenComparisonRequest(
                EXISTING_URL,
                "http://github.com/example/repository"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.compare(request)
        );

        verifyNoInteractions(comparisonService);
    }

    @Test
    void shouldRejectUnexpectedGitHubPaths() {

        MavenComparisonRequest request = new MavenComparisonRequest(
                "https://github.com/example/repository/tree/main",
                INCOMING_URL
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.compare(request)
        );

        verifyNoInteractions(comparisonService);
    }
}
