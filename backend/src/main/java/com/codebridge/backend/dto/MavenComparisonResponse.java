
package com.codebridge.backend.dto;

import com.codebridge.backend.maven.MavenDependencyConflict;

import java.util.List;
import java.util.Objects;

public record MavenComparisonResponse(
        String existingRepository,
        String incomingRepository,
        int existingDependencyCount,
        int incomingDependencyCount,
        int potentialConflictCount,
        List<MavenDependencyConflict> conflicts,
        MavenSeveritySummary severitySummary
) {

    public MavenComparisonResponse {

        Objects.requireNonNull(
                existingRepository,
                "Existing repository is required"
        );

        Objects.requireNonNull(
                incomingRepository,
                "Incoming repository is required"
        );

        Objects.requireNonNull(
                conflicts,
                "Conflicts are required"
        );

        if (existingDependencyCount < 0
                || incomingDependencyCount < 0) {
            throw new IllegalArgumentException(
                    "Dependency counts cannot be negative"
            );
        }

        conflicts = List.copyOf(conflicts);

        if (potentialConflictCount != conflicts.size()) {
            throw new IllegalArgumentException(
                    "Conflict count must match the number of conflicts"
            );
        }

        // Always calculate the summary from the actual conflict list.
        severitySummary = MavenSeveritySummary.fromConflicts(conflicts);
    }

    // Preserve compatibility with the existing six-argument constructor.
    public MavenComparisonResponse(
            String existingRepository,
            String incomingRepository,
            int existingDependencyCount,
            int incomingDependencyCount,
            int potentialConflictCount,
            List<MavenDependencyConflict> conflicts
    ) {
        this(
                existingRepository,
                incomingRepository,
                existingDependencyCount,
                incomingDependencyCount,
                potentialConflictCount,
                conflicts,
                null
        );
    }
}
