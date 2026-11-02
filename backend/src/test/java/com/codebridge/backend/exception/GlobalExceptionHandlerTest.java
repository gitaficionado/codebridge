package com.codebridge.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldReturn404WhenRepositoryNotFound() {

        GitHubApiException exception = new GitHubApiException(
                GitHubApiException.Reason.NOT_FOUND,
                "Repository not found"
        );

        ResponseEntity<Map<String, Object>> response =
                handler.handleGitHubApiException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals(
                "GitHub repository not found or not publicly accessible",
                response.getBody().get("message")
        );
    }

    @Test
    void shouldReturn503WhenRateLimited() {

        GitHubApiException exception = new GitHubApiException(
                GitHubApiException.Reason.RATE_LIMITED,
                "Rate limit reached"
        );

        ResponseEntity<Map<String, Object>> response =
                handler.handleGitHubApiException(exception);

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().get("status"));
    }

    @Test
    void shouldReturn502ForUpstreamError() {

        GitHubApiException exception = new GitHubApiException(
                GitHubApiException.Reason.UPSTREAM_ERROR,
                "GitHub connection failed"
        );

        ResponseEntity<Map<String, Object>> response =
                handler.handleGitHubApiException(exception);

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().get("status"));
    }

    @Test
    void shouldNotExposeInternalExceptionMessages() {

        GitHubApiException exception = new GitHubApiException(
                GitHubApiException.Reason.UPSTREAM_ERROR,
                "Sensitive internal connection information"
        );

        ResponseEntity<Map<String, Object>> response =
                handler.handleGitHubApiException(exception);

        assertNotNull(response.getBody());

        String responseText = response.getBody().toString();

        assertFalse(responseText.contains(
                "Sensitive internal connection information"
        ));
    }
}
