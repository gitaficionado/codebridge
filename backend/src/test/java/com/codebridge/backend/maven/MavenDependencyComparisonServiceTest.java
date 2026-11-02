package com.codebridge.backend.maven;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenDependencyComparisonServiceTest {

    private final MavenDependencyComparisonService service =
            new MavenDependencyComparisonService();

    @Test
    void shouldDetectDifferentVersions() {

        List<MavenDependency> existing = List.of(
                new MavenDependency(
                        "org.slf4j", "slf4j-api", "1.7.36", "compile"
                )
        );

        List<MavenDependency> incoming = List.of(
                new MavenDependency(
                        "org.slf4j", "slf4j-api", "2.0.17", "compile"
                )
        );

        List<MavenDependencyConflict> conflicts =
                service.compare(existing, incoming);

        assertEquals(1, conflicts.size());
        assertEquals("org.slf4j", conflicts.get(0).groupId());
        assertEquals("slf4j-api", conflicts.get(0).artifactId());
        assertEquals("1.7.36", conflicts.get(0).existingVersion());
        assertEquals("2.0.17", conflicts.get(0).incomingVersion());
    }

    @Test
    void shouldIgnoreMatchingVersions() {

        List<MavenDependency> existing = List.of(
                new MavenDependency(
                        "org.junit.jupiter", "junit-jupiter",
                        "5.12.0", "test"
                )
        );

        List<MavenDependency> incoming = List.of(
                new MavenDependency(
                        "org.junit.jupiter", "junit-jupiter",
                        "5.12.0", "test"
                )
        );

        assertTrue(service.compare(existing, incoming).isEmpty());
    }

    @Test
    void shouldIgnoreDependenciesNotShared() {

        List<MavenDependency> existing = List.of(
                new MavenDependency(
                        "org.slf4j", "slf4j-api",
                        "1.7.36", "compile"
                )
        );

        List<MavenDependency> incoming = List.of(
                new MavenDependency(
                        "com.google.guava", "guava",
                        "33.0.0-jre", "compile"
                )
        );

        assertTrue(service.compare(existing, incoming).isEmpty());
    }

    @Test
    void shouldSkipMissingVersions() {

        List<MavenDependency> existing = List.of(
                new MavenDependency(
                        "org.springframework", "spring-core",
                        null, "compile"
                )
        );

        List<MavenDependency> incoming = List.of(
                new MavenDependency(
                        "org.springframework", "spring-core",
                        "6.2.0", "compile"
                )
        );

        assertTrue(service.compare(existing, incoming).isEmpty());
    }

    @Test
    void shouldSkipUnresolvedProperties() {

        List<MavenDependency> existing = List.of(
                new MavenDependency(
                        "com.example", "library",
                        "${library.version}", "compile"
                )
        );

        List<MavenDependency> incoming = List.of(
                new MavenDependency(
                        "com.example", "library",
                        "2.0.0", "compile"
                )
        );

        assertTrue(service.compare(existing, incoming).isEmpty());
    }

    @Test
    void shouldDetectMultipleConflicts() {

        List<MavenDependency> existing = List.of(
                new MavenDependency(
                        "org.slf4j", "slf4j-api",
                        "1.7.36", "compile"
                ),
                new MavenDependency(
                        "com.fasterxml.jackson.core", "jackson-databind",
                        "2.17.0", "compile"
                )
        );

        List<MavenDependency> incoming = List.of(
                new MavenDependency(
                        "org.slf4j", "slf4j-api",
                        "2.0.17", "compile"
                ),
                new MavenDependency(
                        "com.fasterxml.jackson.core", "jackson-databind",
                        "2.18.0", "compile"
                )
        );

        List<MavenDependencyConflict> conflicts =
                service.compare(existing, incoming);

        assertEquals(2, conflicts.size());
    }

    @Test
    void shouldRejectNullDependencyLists() {

        assertThrows(
                NullPointerException.class,
                () -> service.compare(null, List.of())
        );

        assertThrows(
                NullPointerException.class,
                () -> service.compare(List.of(), null)
        );
    }
}
