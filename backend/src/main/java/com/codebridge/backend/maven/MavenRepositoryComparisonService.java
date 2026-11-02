
package com.codebridge.backend.maven;

import com.codebridge.backend.dto.MavenComparisonResponse;
import com.codebridge.backend.github.GitHubPomClient;
import com.codebridge.backend.github.GitHubRepository;
import com.codebridge.backend.github.GitHubUrlParser;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MavenRepositoryComparisonService {

    private final GitHubPomClient githubPomClient;
    private final MavenPomParser mavenPomParser;
    private final MavenDependencyComparisonService comparisonService;

    private final GitHubUrlParser urlParser = new GitHubUrlParser();

    public MavenRepositoryComparisonService(
            GitHubPomClient githubPomClient,
            MavenPomParser mavenPomParser,
            MavenDependencyComparisonService comparisonService) {

        this.githubPomClient = githubPomClient;
        this.mavenPomParser = mavenPomParser;
        this.comparisonService = comparisonService;
    }

    public MavenComparisonResponse analyzeRepositories(
            String existingRepositoryUrl,
            String incomingRepositoryUrl) {

        Objects.requireNonNull(
                existingRepositoryUrl,
                "Existing repository URL is required"
        );

        Objects.requireNonNull(
                incomingRepositoryUrl,
                "Incoming repository URL is required"
        );

        // Validate repository URLs before making network requests.
        GitHubRepository existingRepository =
                urlParser.parse(existingRepositoryUrl);

        GitHubRepository incomingRepository =
                urlParser.parse(incomingRepositoryUrl);

        // Retrieve the root pom.xml from each repository.
        MavenPomFile existingPom =
                githubPomClient.getRootPom(existingRepositoryUrl);

        MavenPomFile incomingPom =
                githubPomClient.getRootPom(incomingRepositoryUrl);

        // Extract directly declared Maven dependencies.
        List<MavenDependency> existingDependencies =
                mavenPomParser.parseDependencies(existingPom.content());

        List<MavenDependency> incomingDependencies =
                mavenPomParser.parseDependencies(incomingPom.content());

        // Identify potential dependency version conflicts.
        List<MavenDependencyConflict> conflicts =
                comparisonService.compare(
                        existingDependencies,
                        incomingDependencies
                );

        return new MavenComparisonResponse(
                existingRepository.owner() + "/"
                        + existingRepository.repository(),
                incomingRepository.owner() + "/"
                        + incomingRepository.repository(),
                existingDependencies.size(),
                incomingDependencies.size(),
                conflicts.size(),
                conflicts
        );
    }

    // Preserve the existing method used by earlier tests and code.
    public List<MavenDependencyConflict> compareRepositories(
            String existingRepositoryUrl,
            String incomingRepositoryUrl) {

        return analyzeRepositories(
                existingRepositoryUrl,
                incomingRepositoryUrl
        ).conflicts();
    }
}
