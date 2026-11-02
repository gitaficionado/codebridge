package com.codebridge.backend.maven;

public record MavenPomFile(
        String repositoryFullName,
        String filePath,
        String content
) {
}
