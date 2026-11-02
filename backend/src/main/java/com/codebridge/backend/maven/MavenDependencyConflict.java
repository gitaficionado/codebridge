package com.codebridge.backend.maven;

public record MavenDependencyConflict(
        String groupId,
        String artifactId,
        String existingVersion,
        String incomingVersion
) {
}
