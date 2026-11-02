
package com.codebridge.backend.github;

import com.codebridge.backend.exception.GitHubApiException;
import com.codebridge.backend.maven.MavenPomFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GitHubPomClientTest {

    private HttpClient httpClient;
    private HttpResponse<InputStream> response;
    private GitHubPomClient pomClient;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        httpClient = mock(HttpClient.class);
        response = mock(HttpResponse.class);
        pomClient = new GitHubPomClient(httpClient);
    }

    private void mockResponse(int status, String body)
            throws Exception {

        when(response.statusCode()).thenReturn(status);

        when(response.body()).thenReturn(
                new ByteArrayInputStream(
                        body.getBytes(StandardCharsets.UTF_8)
                )
        );

        doReturn(response).when(httpClient).send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        );
    }

    @Test
    void shouldRetrieveValidPomFile() throws Exception {

        String pomXml = """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>1.0.0</version>
                </project>
                """;

        String encoded = Base64.getEncoder().encodeToString(
                pomXml.getBytes(StandardCharsets.UTF_8)
        );

        String json = """
                {
                    "type": "file",
                    "path": "pom.xml",
                    "encoding": "base64",
                    "size": %d,
                    "content": "%s"
                }
                """.formatted(
                pomXml.getBytes(StandardCharsets.UTF_8).length,
                encoded
        );

        mockResponse(200, json);

        MavenPomFile result = pomClient.getRootPom(
                "https://github.com/example/demo"
        );

        assertEquals("example/demo", result.repositoryFullName());
        assertEquals("pom.xml", result.filePath());
        assertEquals(pomXml, result.content());

        verify(httpClient).send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        );
    }

    @Test
    void shouldRejectInvalidRepositoryUrl() {

        assertThrows(
                IllegalArgumentException.class,
                () -> pomClient.getRootPom(
                        "https://evil.com/example/demo"
                )
        );

        verifyNoInteractions(httpClient);
    }

    @Test
    void shouldHandleMissingPomFile() throws Exception {

        mockResponse(404, "");

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> pomClient.getRootPom(
                        "https://github.com/example/missing"
                )
        );

        assertEquals(
                GitHubApiException.Reason.NOT_FOUND,
                exception.getReason()
        );
    }

    @Test
    void shouldHandleRateLimit() throws Exception {

        mockResponse(429, "");

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> pomClient.getRootPom(
                        "https://github.com/example/demo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.RATE_LIMITED,
                exception.getReason()
        );
    }

    @Test
    void shouldRejectOversizedResponse() throws Exception {

        String oversizedBody = "A".repeat(1024 * 1024 + 1);

        mockResponse(200, oversizedBody);

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> pomClient.getRootPom(
                        "https://github.com/example/demo"
                )
        );

        assertEquals(
                GitHubApiException.Reason.UPSTREAM_ERROR,
                exception.getReason()
        );
    }
}
