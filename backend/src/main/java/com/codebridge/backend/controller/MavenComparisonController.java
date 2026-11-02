
package com.codebridge.backend.controller;

import com.codebridge.backend.dto.MavenComparisonRequest;
import com.codebridge.backend.dto.MavenComparisonResponse;
import com.codebridge.backend.github.GitHubUrlParser;
import com.codebridge.backend.maven.MavenRepositoryComparisonService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maven")
public class MavenComparisonController {

    private final MavenRepositoryComparisonService comparisonService;
    private final GitHubUrlParser urlParser = new GitHubUrlParser();

    public MavenComparisonController(
            MavenRepositoryComparisonService comparisonService) {

        this.comparisonService = comparisonService;
    }

    @PostMapping("/compare")
    public ResponseEntity<MavenComparisonResponse> compare(
            @Valid @RequestBody MavenComparisonRequest request) {

        urlParser.parse(request.existingRepositoryUrl());
        urlParser.parse(request.incomingRepositoryUrl());

        MavenComparisonResponse response =
                comparisonService.analyzeRepositories(
                        request.existingRepositoryUrl(),
                        request.incomingRepositoryUrl()
                );

        return ResponseEntity.ok(response);
    }
}
