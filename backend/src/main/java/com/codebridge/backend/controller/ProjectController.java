
package com.codebridge.backend.controller;

import com.codebridge.backend.dto.ProjectRequest;
import com.codebridge.backend.entity.Project;
import com.codebridge.backend.github.GitHubUrlParser;
import com.codebridge.backend.service.ProjectService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final GitHubUrlParser githubUrlParser;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
        this.githubUrlParser = new GitHubUrlParser();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @Valid @RequestBody ProjectRequest request) {

        githubUrlParser.parse(request.existingRepoUrl());
        githubUrlParser.parse(request.incomingRepoUrl());

        Project project = new Project();
        project.setName(request.name());
        project.setExistingRepoUrl(request.existingRepoUrl());
        project.setIncomingRepoUrl(request.incomingRepoUrl());

        Project savedProject =
                projectService.createProject(project);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedProject);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(
            @PathVariable Long id) {

        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }
}
