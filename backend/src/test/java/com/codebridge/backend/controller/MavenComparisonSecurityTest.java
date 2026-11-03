
package com.codebridge.backend.controller;

import com.codebridge.backend.config.SecurityConfig;
import com.codebridge.backend.dto.MavenComparisonResponse;
import com.codebridge.backend.maven.MavenRepositoryComparisonService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MavenComparisonController.class)
@Import(SecurityConfig.class)
class MavenComparisonSecurityTest {

    private static final String EXISTING_URL =
            "https://github.com/junit-team/junit4";

    private static final String INCOMING_URL =
            "https://github.com/apache/commons-lang";

    private static final String VALID_REQUEST = """
            {
              "existingRepositoryUrl":
                "https://github.com/junit-team/junit4",
              "incomingRepositoryUrl":
                "https://github.com/apache/commons-lang"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MavenRepositoryComparisonService comparisonService;

    @Test
    void shouldRejectUnauthenticatedPost() throws Exception {

        mockMvc.perform(
                post("/api/maven/compare")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST)
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(comparisonService);
    }

    @Test
    void shouldRejectAuthenticatedPostWithoutCsrf()
            throws Exception {

        mockMvc.perform(
                post("/api/maven/compare")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST)
        ).andExpect(status().isForbidden());

        verifyNoInteractions(comparisonService);
    }

    @Test
    void shouldRejectAuthenticatedPostWithInvalidCsrf()
            throws Exception {

        mockMvc.perform(
                post("/api/maven/compare")
                        .with(user("test-user"))
                        .with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST)
        ).andExpect(status().isForbidden());

        verifyNoInteractions(comparisonService);
    }

    @Test
    void shouldAcceptAuthenticatedPostWithValidCsrf()
            throws Exception {

        MavenComparisonResponse response =
                new MavenComparisonResponse(
                        "junit-team/junit4",
                        "apache/commons-lang",
                        2,
                        7,
                        0,
                        List.of()
                );

        when(comparisonService.analyzeRepositories(
                EXISTING_URL,
                INCOMING_URL
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/maven/compare")
                                .with(user("test-user"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.existingRepository"
                ).value("junit-team/junit4"))
                .andExpect(jsonPath(
                        "$.incomingRepository"
                ).value("apache/commons-lang"))
                .andExpect(jsonPath(
                        "$.existingDependencyCount"
                ).value(2))
                .andExpect(jsonPath(
                        "$.incomingDependencyCount"
                ).value(7))
                .andExpect(jsonPath(
                        "$.potentialConflictCount"
                ).value(0))
                .andExpect(jsonPath(
                        "$.severitySummary.high"
                ).value(0))
                .andExpect(jsonPath(
                        "$.severitySummary.medium"
                ).value(0))
                .andExpect(jsonPath(
                        "$.severitySummary.low"
                ).value(0))
                .andExpect(jsonPath(
                        "$.severitySummary.unknown"
                ).value(0));

        verify(comparisonService).analyzeRepositories(
                EXISTING_URL,
                INCOMING_URL
        );
    }

    @Test
    void shouldRejectInvalidRepositoryUrlWithValidCsrf()
            throws Exception {

        String invalidRequest = """
                {
                  "existingRepositoryUrl":
                    "https://evil.com/example/repository",
                  "incomingRepositoryUrl":
                    "https://github.com/apache/commons-lang"
                }
                """;

        mockMvc.perform(
                post("/api/maven/compare")
                        .with(user("test-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
        ).andExpect(status().isBadRequest());

        verifyNoInteractions(comparisonService);
    }

    @Test
    void shouldRejectUnauthenticatedGetToProtectedApi()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(comparisonService);
    }
}
