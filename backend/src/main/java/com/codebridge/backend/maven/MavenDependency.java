package com.codebridge.backend.maven;

public record MavenDependency(
        String groupId,
        String artifactId,
        String version,
        String scope
) {
}
