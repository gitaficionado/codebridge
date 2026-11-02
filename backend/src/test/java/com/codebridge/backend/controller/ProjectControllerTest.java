package com.codebridge.backend.controller;

import com.codebridge.backend.dto.ProjectRequest;
import com.codebridge.backend.entity.Project;
import com.codebridge.backend.entity.ProjectStatus;
import com.codebridge.backend.service.ProjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectControllerTest {

    private ProjectService projectService;
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        projectController = new ProjectController(projectService);
    }

    @Test
    void shouldCreateProjectWithValidGitHubUrls() {

        ProjectRequest request = new ProjectRequest(
                "Valid Integration",
                "https://github.com/example/project-a",
                "https://github.com/example/project-b"
        );

        when(projectService.createProject(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = projectController.createProject(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Valid Integration", response.getBody().getName());
        assertEquals(ProjectStatus.PENDING, response.getBody().getStatus());

        verify(projectService, times(1))
                .createProject(any(Project.class));
    }

    @Test
    void shouldRejectInvalidGitHubDomainWithoutSaving() {

        ProjectRequest request = new ProjectRequest(
                "Invalid Integration",
                "https://evil.com/example/project-a",
                "https://github.com/example/project-b"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> projectController.createProject(request)
        );

        verify(projectService, never())
                .createProject(any(Project.class));
    }

    @Test
    void shouldRejectAdditionalGitHubPathWithoutSaving() {

        ProjectRequest request = new ProjectRequest(
                "Invalid GitHub Path",
                "https://github.com/example/project-a/tree/main",
                "https://github.com/example/project-b"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> projectController.createProject(request)
        );

        verify(projectService, never())
                .createProject(any(Project.class));
    }

    @Test
    void shouldRejectInvalidIncomingRepositoryWithoutSaving() {

        ProjectRequest request = new ProjectRequest(
                "Invalid Incoming Repository",
                "https://github.com/example/project-a",
                "https://github.com:8443/example/project-b"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> projectController.createProject(request)
        );

        verify(projectService, never())
                .createProject(any(Project.class));
    }
}
