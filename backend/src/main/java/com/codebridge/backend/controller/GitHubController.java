package com.codebridge.backend.controller;

import com.codebridge.backend.github.GitHubApiClient;
import com.codebridge.backend.github.GitHubRepositoryMetadata;
import com.codebridge.backend.github.GitHubUrlParser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github/repositories")
public class GitHubController {

    private final GitHubApiClient githubApiClient;
    private final GitHubUrlParser urlParser = new GitHubUrlParser();

    public GitHubController(GitHubApiClient githubApiClient) {
        this.githubApiClient = githubApiClient;
    }

    @GetMapping("/{owner}/{repository}")
    public GitHubRepositoryMetadata getRepositoryMetadata(
            @PathVariable String owner,
            @PathVariable String repository) {

        String repositoryUrl =
                "https://github.com/" + owner + "/" + repository;

        // Validate the repository identifier before calling GitHub.
        urlParser.parse(repositoryUrl);

        GitHubRepositoryMetadata metadata =
                githubApiClient.getRepositoryMetadata(repositoryUrl);

        if (metadata.isPrivate()) {
            throw new IllegalArgumentException(
                    "Only public GitHub repositories are supported"
            );
        }

        return metadata;
    }
}
