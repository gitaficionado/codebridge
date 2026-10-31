
package com.codebridge.backend.service;

import com.codebridge.backend.entity.Project;
import com.codebridge.backend.repository.ProjectRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldCreateProject() {

        Project project = new Project();
        project.setName("Payment Integration");
        project.setExistingRepoUrl(
                "https://github.com/example/project-a");
        project.setIncomingRepoUrl(
                "https://github.com/example/project-b");

        when(projectRepository.save(project))
                .thenReturn(project);

        Project result = projectService.createProject(project);

        assertEquals("Payment Integration", result.getName());
        verify(projectRepository).save(project);
    }

    @Test
    void shouldGetAllProjects() {

        Project project = new Project();
        project.setName("Sample Project");

        when(projectRepository.findAll())
                .thenReturn(List.of(project));

        List<Project> results = projectService.getAllProjects();

        assertEquals(1, results.size());
        assertEquals("Sample Project", results.get(0).getName());

        verify(projectRepository).findAll();
    }

    @Test
    void shouldGetProjectById() {

        Project project = new Project();
        project.setName("Existing Project");

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        Optional<Project> result =
                projectService.getProjectById(1L);

        assertTrue(result.isPresent());
        assertEquals("Existing Project",
                result.get().getName());

        verify(projectRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyForUnknownProject() {

        when(projectRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Project> result =
                projectService.getProjectById(999L);

        assertTrue(result.isEmpty());
        verify(projectRepository).findById(999L);
    }
}
