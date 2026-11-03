
package com.codebridge.backend.dto;

import com.codebridge.backend.maven.ConflictSeverity;
import com.codebridge.backend.maven.MavenDependencyConflict;

import java.util.List;
import java.util.Objects;

public record MavenSeveritySummary(
        int high,
        int medium,
        int low,
        int unknown
) {

    public MavenSeveritySummary {
        if (high < 0 || medium < 0 || low < 0 || unknown < 0) {
            throw new IllegalArgumentException(
                    "Severity counts cannot be negative"
            );
        }
    }

    public int total() {
        return high + medium + low + unknown;
    }

    public static MavenSeveritySummary fromConflicts(
            List<MavenDependencyConflict> conflicts
    ) {
        Objects.requireNonNull(conflicts, "conflicts are required");

        int high = 0;
        int medium = 0;
        int low = 0;
        int unknown = 0;

        for (MavenDependencyConflict conflict : conflicts) {
            Objects.requireNonNull(conflict, "conflict cannot be null");

            ConflictSeverity severity = conflict.severity();

            switch (severity) {
                case HIGH -> high++;
                case MEDIUM -> medium++;
                case LOW -> low++;
                case UNKNOWN -> unknown++;
            }
        }

        return new MavenSeveritySummary(
                high,
                medium,
                low,
                unknown
        );
    }
}
