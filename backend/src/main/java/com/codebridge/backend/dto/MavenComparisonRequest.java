package com.codebridge.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MavenComparisonRequest(

        @NotBlank(message = "Existing repository URL is required")
        @Pattern(
                regexp = "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$",
                message = "Enter a valid existing GitHub repository URL"
        )
        String existingRepositoryUrl,

        @NotBlank(message = "Incoming repository URL is required")
        @Pattern(
                regexp = "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$",
                message = "Enter a valid incoming GitHub repository URL"
        )
        String incomingRepositoryUrl
) {
}
