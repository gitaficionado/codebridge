package com.codebridge.backend.github;

public record GitHubRepositoryMetadata(
        String fullName,
        String description,
        String language,
        String defaultBranch,
        boolean isPrivate,
        String htmlUrl
) {
}
