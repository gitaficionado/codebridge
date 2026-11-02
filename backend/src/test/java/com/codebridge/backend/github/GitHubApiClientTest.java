
package com.codebridge.backend.github;

import com.codebridge.backend.exception.GitHubApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class GitHubApiClientTest {

    private GitHubApiClient githubApiClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        githubApiClient = new GitHubApiClient(builder.build());
    }

    @Test
    void shouldRejectNonGitHubUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://evil.com/example/repository"
                )
        );
    }

    @Test
    void shouldRejectHttpUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "http://github.com/example/repository"
                )
        );
    }

    @Test
    void shouldRejectAdditionalRepositoryPath() {
        assertThrows(
                IllegalArgumentException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/repository/tree/main"
                )
        );
    }

    @Test
    void shouldRejectNullUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> githubApiClient.getRepositoryMetadata(null)
        );
    }

    @Test
    void shouldReturnRepositoryMetadata() {
        String json = """
                {
                  "full_name": "example/my-repo",
                  "description": "Example repository",
                  "language": "Java",
                  "default_branch": "main",
                  "private": false,
                  "html_url": "https://github.com/example/my-repo"
                }
                """;

        mockServer.expect(
                requestTo("https://api.github.com/repos/example/my-repo")
        ).andRespond(
                withSuccess(json, MediaType.APPLICATION_JSON)
        );

        GitHubRepositoryMetadata result =
                githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/my-repo"
                );

        assertEquals("example/my-repo", result.fullName());
        assertEquals("Example repository", result.description());
        assertEquals("Java", result.language());
        assertEquals("main", result.defaultBranch());
        assertFalse(result.isPrivate());
        assertEquals(
                "https://github.com/example/my-repo",
                result.htmlUrl()
        );

        mockServer.verify();
    }

    @Test
    void shouldHandleRepositoryNotFound() {
        mockServer.expect(
                requestTo("https://api.github.com/repos/example/missing-repo")
        ).andRespond(
                withStatus(HttpStatus.NOT_FOUND)
        );

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/missing-repo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.NOT_FOUND,
                exception.getReason()
        );

        mockServer.verify();
    }

    @Test
    void shouldHandleRateLimit429() {
        mockServer.expect(
                requestTo("https://api.github.com/repos/example/my-repo")
        ).andRespond(
                withStatus(HttpStatus.TOO_MANY_REQUESTS)
        );

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/my-repo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.RATE_LIMITED,
                exception.getReason()
        );

        mockServer.verify();
    }

    @Test
    void shouldHandleRateLimit403() {
        mockServer.expect(
                requestTo("https://api.github.com/repos/example/my-repo")
        ).andRespond(
                withStatus(HttpStatus.FORBIDDEN)
                        .header("X-RateLimit-Remaining", "0")
        );

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/my-repo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.RATE_LIMITED,
                exception.getReason()
        );

        mockServer.verify();
    }

    @Test
    void shouldHandleServerError500() {
        mockServer.expect(
                requestTo("https://api.github.com/repos/example/my-repo")
        ).andRespond(
                withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/my-repo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.UPSTREAM_ERROR,
                exception.getReason()
        );

        mockServer.verify();
    }

    @Test
    void shouldHandleForbiddenWithoutRateLimitHeaders() {
        mockServer.expect(
                requestTo("https://api.github.com/repos/example/my-repo")
        ).andRespond(
                withStatus(HttpStatus.FORBIDDEN)
        );

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/my-repo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.UPSTREAM_ERROR,
                exception.getReason()
        );

        mockServer.verify();
    }

    @Test
    void shouldHandleEmptyResponse() {
        mockServer.expect(
                requestTo("https://api.github.com/repos/example/my-repo")
        ).andRespond(
                withSuccess("", MediaType.APPLICATION_JSON)
        );

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> githubApiClient.getRepositoryMetadata(
                        "https://github.com/example/my-repo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.UPSTREAM_ERROR,
                exception.getReason()
        );

        mockServer.verify();
    }
}
