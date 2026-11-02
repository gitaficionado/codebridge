
package com.codebridge.backend.github;

import com.codebridge.backend.exception.GitHubApiException;
import com.codebridge.backend.maven.MavenPomFile;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class GitHubPomClient {

    private static final int MAX_POM_BYTES = 512 * 1024;
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;

    private final GitHubUrlParser urlParser = new GitHubUrlParser();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public GitHubPomClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    // Package-private constructor for offline unit tests.
    GitHubPomClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public MavenPomFile getRootPom(String repositoryUrl) {

        GitHubRepository repository = urlParser.parse(repositoryUrl);

        String apiUrl = "https://api.github.com/repos/"
                + repository.owner()
                + "/"
                + repository.repository()
                + "/contents/pom.xml";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            try (InputStream body = response.body()) {

                int status = response.statusCode();

                if (status == 404) {
                    throw new GitHubApiException(
                            GitHubApiException.Reason.NOT_FOUND,
                            "Root pom.xml not found or not publicly accessible"
                    );
                }

                if (status == 429 ||
                        (status == 403 &&
                                ("0".equals(response.headers()
                                        .firstValue("X-RateLimit-Remaining")
                                        .orElse(null))
                                        || response.headers()
                                        .firstValue("Retry-After")
                                        .isPresent()))) {

                    throw new GitHubApiException(
                            GitHubApiException.Reason.RATE_LIMITED,
                            "GitHub API rate limit reached"
                    );
                }

                if (status != 200) {
                    throw new GitHubApiException(
                            GitHubApiException.Reason.UPSTREAM_ERROR,
                            "Unable to retrieve repository pom.xml"
                    );
                }

                byte[] responseBytes =
                        body.readNBytes(MAX_RESPONSE_BYTES + 1);

                if (responseBytes.length > MAX_RESPONSE_BYTES) {
                    throw new GitHubApiException(
                            GitHubApiException.Reason.UPSTREAM_ERROR,
                            "GitHub response exceeds the allowed size"
                    );
                }

                JsonNode json = objectMapper.readTree(responseBytes);

                if (json == null ||
                        !"file".equals(json.path("type").asText()) ||
                        !"base64".equals(json.path("encoding").asText()) ||
                        !"pom.xml".equals(json.path("path").asText())) {

                    throw new GitHubApiException(
                            GitHubApiException.Reason.UPSTREAM_ERROR,
                            "Unexpected GitHub file response"
                    );
                }

                if (json.path("size").asLong(-1) > MAX_POM_BYTES) {
                    throw new GitHubApiException(
                            GitHubApiException.Reason.UPSTREAM_ERROR,
                            "pom.xml exceeds the allowed size"
                    );
                }

                String encodedContent =
                        json.path("content").asText("");

                byte[] pomBytes = Base64.getMimeDecoder()
                        .decode(encodedContent);

                if (pomBytes.length > MAX_POM_BYTES) {
                    throw new GitHubApiException(
                            GitHubApiException.Reason.UPSTREAM_ERROR,
                            "pom.xml exceeds the allowed size"
                    );
                }

                return new MavenPomFile(
                        repository.owner() + "/" + repository.repository(),
                        "pom.xml",
                        new String(pomBytes, StandardCharsets.UTF_8)
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new GitHubApiException(
                    GitHubApiException.Reason.UPSTREAM_ERROR,
                    "GitHub request was interrupted"
            );

        } catch (IOException | IllegalArgumentException exception) {
            throw new GitHubApiException(
                    GitHubApiException.Reason.UPSTREAM_ERROR,
                    "Unable to read GitHub pom.xml response"
            );
        }
    }
}
