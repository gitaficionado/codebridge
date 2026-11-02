package com.codebridge.backend.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubUrlParserTest {

    private final GitHubUrlParser parser =
            new GitHubUrlParser();

    @Test
    void shouldParseValidGitHubUrl() {

        GitHubRepository result = parser.parse(
                "https://github.com/spring-projects/spring-boot"
        );

        assertEquals("spring-projects", result.owner());
        assertEquals("spring-boot", result.repository());
    }

    @Test
    void shouldAcceptTrailingSlash() {

        GitHubRepository result = parser.parse(
                "https://github.com/example/my-repo/"
        );

        assertEquals("example", result.owner());
        assertEquals("my-repo", result.repository());
    }

    @Test
    void shouldRejectHttpUrl() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "http://github.com/example/my-repo"
                )
        );
    }

    @Test
    void shouldRejectNonGitHubDomain() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://evil.com/example/my-repo"
                )
        );
    }

    @Test
    void shouldRejectGitHubSubdomain() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://github.com.evil.com/example/my-repo"
                )
        );
    }

    @Test
    void shouldRejectAdditionalPath() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://github.com/example/my-repo/tree/main"
                )
        );
    }

    @Test
    void shouldRejectQueryParameters() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://github.com/example/my-repo?token=secret"
                )
        );
    }

    @Test
    void shouldRejectUserInfo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://user@github.com/example/my-repo"
                )
        );
    }

    @Test
    void shouldRejectCustomPort() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://github.com:8443/example/my-repo"
                )
        );
    }

    @Test
    void shouldRejectEmptyUrl() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("")
        );
    }

    @Test
    void shouldRejectNullUrl() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(null)
        );
    }

    @Test
    void shouldRejectInvalidOwner() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(
                        "https://github.com/-invalid/my-repo"
                )
        );
    }
}
