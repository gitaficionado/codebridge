
package com.codebridge.backend.dto;

import com.codebridge.backend.maven.ConflictSeverity;
import com.codebridge.backend.maven.MavenDependencyConflict;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenComparisonResponseTest {

    private static final String EXISTING_REPOSITORY =
            "example/existing-app";

    private static final String INCOMING_REPOSITORY =
            "example/incoming-app";

    @Test
    void shouldCalculateSeverityCountsFromConflicts() {

        List<MavenDependencyConflict> conflicts = List.of(
                conflict("1.0.0", "2.0.0"),
                conflict("2.0.0", "2.1.0"),
                conflict("3.0.1", "3.0.2"),
                conflict("1.0-SNAPSHOT", "2.0-custom")
        );

        MavenComparisonResponse response =
                createResponse(conflicts);

        assertEquals(4, response.potentialConflictCount());
        assertEquals(4, response.conflicts().size());

        MavenSeveritySummary summary =
                response.severitySummary();

        assertNotNull(summary);
        assertEquals(1, summary.high());
        assertEquals(1, summary.medium());
        assertEquals(1, summary.low());
        assertEquals(1, summary.unknown());
        assertEquals(4, summary.total());
    }

    @Test
    void shouldReturnZeroSeverityCountsWhenNoConflictsExist() {

        MavenComparisonResponse response =
                createResponse(List.of());

        MavenSeveritySummary summary =
                response.severitySummary();

        assertEquals(0, response.potentialConflictCount());
        assertEquals(0, summary.high());
        assertEquals(0, summary.medium());
        assertEquals(0, summary.low());
        assertEquals(0, summary.unknown());
        assertEquals(0, summary.total());
    }

    @Test
    void shouldMaintainConsistencyBetweenTotalAndConflictCount() {

        List<MavenDependencyConflict> conflicts = List.of(
                conflict("1.0.0", "2.0.0"),
                conflict("2.0.0", "2.1.0"),
                conflict("2.0.0", "2.0.1")
        );

        MavenComparisonResponse response =
                createResponse(conflicts);

        assertEquals(
                response.potentialConflictCount(),
                response.severitySummary().total()
        );
    }

    @Test
    void shouldRejectIncorrectConflictCount() {

        List<MavenDependencyConflict> conflicts = List.of(
                conflict("1.0.0", "2.0.0")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenComparisonResponse(
                        EXISTING_REPOSITORY,
                        INCOMING_REPOSITORY,
                        5,
                        6,
                        2,
                        conflicts
                )
        );
    }

    @Test
    void shouldRejectNegativeDependencyCounts() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenComparisonResponse(
                        EXISTING_REPOSITORY,
                        INCOMING_REPOSITORY,
                        -1,
                        6,
                        0,
                        List.of()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenComparisonResponse(
                        EXISTING_REPOSITORY,
                        INCOMING_REPOSITORY,
                        5,
                        -1,
                        0,
                        List.of()
                )
        );
    }

    @Test
    void shouldIgnoreSuppliedSeveritySummaryAndRecalculate() {

        MavenDependencyConflict conflict =
                conflict("1.0.0", "2.0.0");

        MavenSeveritySummary incorrectSummary =
                new MavenSeveritySummary(0, 0, 10, 0);

        MavenComparisonResponse response =
                new MavenComparisonResponse(
                        EXISTING_REPOSITORY,
                        INCOMING_REPOSITORY,
                        5,
                        6,
                        1,
                        List.of(conflict),
                        incorrectSummary
                );

        assertEquals(1, response.severitySummary().high());
        assertEquals(0, response.severitySummary().low());
        assertEquals(1, response.severitySummary().total());
    }

    @Test
    void shouldReturnAnImmutableConflictList() {

        MavenComparisonResponse response =
                createResponse(List.of(
                        conflict("1.0.0", "2.0.0")
                ));

        assertThrows(
                UnsupportedOperationException.class,
                () -> response.conflicts().clear()
        );
    }

    @Test
    void shouldPreserveConflictSeverity() {

        MavenComparisonResponse response =
                createResponse(List.of(
                        conflict("1.0.0", "2.0.0")
                ));

        assertEquals(
                ConflictSeverity.HIGH,
                response.conflicts().get(0).severity()
        );
    }

    private MavenComparisonResponse createResponse(
            List<MavenDependencyConflict> conflicts
    ) {
        return new MavenComparisonResponse(
                EXISTING_REPOSITORY,
                INCOMING_REPOSITORY,
                5,
                6,
                conflicts.size(),
                conflicts
        );
    }

    private MavenDependencyConflict conflict(
            String existingVersion,
            String incomingVersion
    ) {
        return new MavenDependencyConflict(
                "org.example",
                "example-library",
                existingVersion,
                incomingVersion
        );
    }
}
