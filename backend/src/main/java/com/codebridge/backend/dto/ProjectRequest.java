package com.codebridge.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name is too long")
        String name,

        @NotBlank(message = "Existing repository URL is required")
        @Pattern(
                regexp = "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$",
                message = "Enter a valid GitHub repository URL"
        )
        String existingRepoUrl,

        @NotBlank(message = "Incoming repository URL is required")
        @Pattern(
                regexp = "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$",
                message = "Enter a valid GitHub repository URL"
        )
        String incomingRepoUrl
) {}
