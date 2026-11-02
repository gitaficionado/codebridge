
package com.codebridge.backend.dto;

import com.codebridge.backend.maven.MavenDependencyConflict;

import java.util.List;

public record MavenComparisonResponse(
        String existingRepository,
        String incomingRepository,
        int existingDependencyCount,
        int incomingDependencyCount,
        int potentialConflictCount,
        List<MavenDependencyConflict> conflicts
) {

    public MavenComparisonResponse {
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
    }
}
