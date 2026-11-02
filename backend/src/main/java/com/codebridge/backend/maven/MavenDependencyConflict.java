
package com.codebridge.backend.maven;

import java.util.Objects;

public record MavenDependencyConflict(
        String groupId,
        String artifactId,
        String existingVersion,
        String incomingVersion,
        ConflictSeverity severity
) {

    public MavenDependencyConflict {
        Objects.requireNonNull(groupId, "groupId is required");
        Objects.requireNonNull(artifactId, "artifactId is required");
        Objects.requireNonNull(
                existingVersion,
                "existingVersion is required"
        );
        Objects.requireNonNull(
                incomingVersion,
                "incomingVersion is required"
        );
        Objects.requireNonNull(severity, "severity is required");
    }

    // Keeps the original constructor for existing comparison code and tests.
    // Automatically classify the versions when severity is not supplied.
    public MavenDependencyConflict(
            String groupId,
            String artifactId,
            String existingVersion,
            String incomingVersion
    ) {
        this(
                groupId,
                artifactId,
                existingVersion,
                incomingVersion,
                new MavenConflictSeverityClassifier().classify(
                        existingVersion,
                        incomingVersion
                )
        );
    }
}
