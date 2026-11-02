package com.codebridge.backend.github;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class GitHubUrlParser {

    private static final Pattern OWNER_PATTERN =
            Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})");

    private static final Pattern REPOSITORY_PATTERN =
            Pattern.compile("[A-Za-z0-9_.-]+");

    public GitHubRepository parse(String url) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub repository URL is required"
            );
        }

        try {
            URI uri = new URI(url);

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"github.com".equalsIgnoreCase(uri.getHost())
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || uri.getQuery() != null
                    || uri.getFragment() != null) {

                throw new IllegalArgumentException(
                        "Only HTTPS GitHub repository URLs are supported"
                );
            }

            String path = uri.getPath();

            if (path == null) {
                throw new IllegalArgumentException(
                        "Invalid GitHub repository path"
                );
            }

            String[] parts = path.split("/", -1);

            if (parts.length < 3 || parts.length > 4
                    || !parts[0].isEmpty()
                    || (parts.length == 4
                    && !parts[3].isEmpty())) {

                throw new IllegalArgumentException(
                        "Expected https://github.com/owner/repository"
                );
            }

            String owner = parts[1];
            String repository = parts[2];

            if (!OWNER_PATTERN.matcher(owner).matches()
                    || owner.startsWith("-")
                    || owner.endsWith("-")
                    || !REPOSITORY_PATTERN.matcher(repository).matches()
                    || repository.equals(".")
                    || repository.equals("..")) {

                throw new IllegalArgumentException(
                        "Invalid GitHub owner or repository name"
                );
            }

            return new GitHubRepository(owner, repository);

        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL",
                    exception
            );
        }
    }
}
