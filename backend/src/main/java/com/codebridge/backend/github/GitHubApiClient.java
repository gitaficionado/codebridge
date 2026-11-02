
package com.codebridge.backend.github;

import com.codebridge.backend.exception.GitHubApiException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Service
public class GitHubApiClient {

    private final GitHubUrlParser urlParser =
            new GitHubUrlParser();

    private final RestClient restClient;

    public GitHubApiClient() {
        this(createRestClient());
    }

    // Allows tests in this package to supply a mock HTTP client.
    GitHubApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    private static RestClient createRestClient() {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .requestFactory(requestFactory)
                .defaultHeader(
                        "Accept",
                        "application/vnd.github+json"
                )
                .defaultHeader(
                        "X-GitHub-Api-Version",
                        "2022-11-28"
                )
                .build();
    }

    public GitHubRepositoryMetadata getRepositoryMetadata(
            String repositoryUrl) {

        GitHubRepository repository =
                urlParser.parse(repositoryUrl);

        try {
            Map<?, ?> response = restClient.get()
                    .uri(
                            "/repos/{owner}/{repo}",
                            repository.owner(),
                            repository.repository()
                    )
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new GitHubApiException(
                        GitHubApiException.Reason.UPSTREAM_ERROR,
                        "GitHub returned an empty response"
                );
            }

            return new GitHubRepositoryMetadata(
                    getString(response, "full_name"),
                    getString(response, "description"),
                    getString(response, "language"),
                    getString(response, "default_branch"),
                    Boolean.TRUE.equals(response.get("private")),
                    getString(response, "html_url")
            );

        } catch (RestClientResponseException exception) {

            int status = exception.getStatusCode().value();

            if (status == 404) {
                throw new GitHubApiException(
                        GitHubApiException.Reason.NOT_FOUND,
                        "GitHub repository not found or not publicly accessible"
                );
            }

            if (status == 429 || isRateLimited(exception)) {
                throw new GitHubApiException(
                        GitHubApiException.Reason.RATE_LIMITED,
                        "GitHub API rate limit reached"
                );
            }

            throw new GitHubApiException(
                    GitHubApiException.Reason.UPSTREAM_ERROR,
                    "GitHub API returned an unsuccessful response"
            );

        } catch (RestClientException exception) {

            throw new GitHubApiException(
                    GitHubApiException.Reason.UPSTREAM_ERROR,
                    "Unable to retrieve GitHub repository information"
            );
        }
    }

    private boolean isRateLimited(
            RestClientResponseException exception) {

        if (exception.getStatusCode().value() != 403) {
            return false;
        }

        HttpHeaders headers = exception.getResponseHeaders();

        if (headers == null) {
            return false;
        }

        String remaining =
                headers.getFirst("X-RateLimit-Remaining");

        String retryAfter =
                headers.getFirst("Retry-After");

        return "0".equals(remaining) || retryAfter != null;
    }

    private String getString(
            Map<?, ?> response,
            String field) {

        Object value = response.get(field);

        return value instanceof String text ? text : null;
    }
}
