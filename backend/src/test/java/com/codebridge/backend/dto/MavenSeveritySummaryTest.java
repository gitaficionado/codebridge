
package com.codebridge.backend.dto;

import com.codebridge.backend.maven.ConflictSeverity;
import com.codebridge.backend.maven.MavenDependencyConflict;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenSeveritySummaryTest {

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

    @Test
    void shouldCountEverySeverityLevel() {

        List<MavenDependencyConflict> conflicts = List.of(
                conflict("1.0.0", "2.0.0"),
                conflict("2.0.0", "3.0.0"),
                conflict("2.0.0", "2.1.0"),
                conflict("2.0.1", "2.0.2"),
                conflict("1.0-SNAPSHOT", "2.0-custom")
        );

        MavenSeveritySummary summary =
                MavenSeveritySummary.fromConflicts(conflicts);

        assertEquals(2, summary.high());
        assertEquals(1, summary.medium());
        assertEquals(1, summary.low());
        assertEquals(1, summary.unknown());
        assertEquals(5, summary.total());
    }

    @Test
    void shouldReturnZeroCountsForEmptyConflictList() {

        MavenSeveritySummary summary =
                MavenSeveritySummary.fromConflicts(List.of());

        assertEquals(0, summary.high());
        assertEquals(0, summary.medium());
        assertEquals(0, summary.low());
        assertEquals(0, summary.unknown());
        assertEquals(0, summary.total());
    }

    @Test
    void shouldCountMultipleLowSeverityConflicts() {

        List<MavenDependencyConflict> conflicts = List.of(
                conflict("1.0.0", "1.0.1"),
                conflict("2.3.4", "2.3.5"),
                conflict("3.0.2", "3.0.1")
        );

        MavenSeveritySummary summary =
                MavenSeveritySummary.fromConflicts(conflicts);

        assertEquals(0, summary.high());
        assertEquals(0, summary.medium());
        assertEquals(3, summary.low());
        assertEquals(0, summary.unknown());
        assertEquals(3, summary.total());
    }

    @Test
    void shouldRejectNegativeSeverityCounts() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenSeveritySummary(-1, 0, 0, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenSeveritySummary(0, -1, 0, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenSeveritySummary(0, 0, -1, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MavenSeveritySummary(0, 0, 0, -1)
        );
    }

    @Test
    void shouldRejectNullConflictList() {

        assertThrows(
                NullPointerException.class,
                () -> MavenSeveritySummary.fromConflicts(null)
        );
    }

    @Test
    void shouldRejectNullConflictInsideList() {

        List<MavenDependencyConflict> conflicts =
                new java.util.ArrayList<>();

        conflicts.add(
                conflict("1.0.0", "2.0.0")
        );
        conflicts.add(null);

        assertThrows(
                NullPointerException.class,
                () -> MavenSeveritySummary.fromConflicts(conflicts)
        );
    }

    @Test
    void shouldCountExplicitUnknownSeverity() {

        MavenDependencyConflict unknownConflict =
                new MavenDependencyConflict(
                        "org.example",
                        "example-library",
                        "1.0.0",
                        "2.0.0",
                        ConflictSeverity.UNKNOWN
                );

        MavenSeveritySummary summary =
                MavenSeveritySummary.fromConflicts(
                        List.of(unknownConflict)
                );

        assertEquals(0, summary.high());
        assertEquals(0, summary.medium());
        assertEquals(0, summary.low());
        assertEquals(1, summary.unknown());
        assertEquals(1, summary.total());
    }
}
