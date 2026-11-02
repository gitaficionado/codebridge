package com.codebridge.backend.maven;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MavenDependencyComparisonService {

    public List<MavenDependencyConflict> compare(
            List<MavenDependency> existingDependencies,
            List<MavenDependency> incomingDependencies) {

        Objects.requireNonNull(
                existingDependencies,
                "Existing dependencies cannot be null"
        );

        Objects.requireNonNull(
                incomingDependencies,
                "Incoming dependencies cannot be null"
        );

        Map<String, MavenDependency> existingMap =
                new HashMap<>();

        for (MavenDependency dependency : existingDependencies) {

            String key = dependency.groupId()
                    + ":"
                    + dependency.artifactId();

            existingMap.put(key, dependency);
        }

        List<MavenDependencyConflict> conflicts =
                new ArrayList<>();

        for (MavenDependency incoming : incomingDependencies) {

            String key = incoming.groupId()
                    + ":"
                    + incoming.artifactId();

            MavenDependency existing = existingMap.get(key);

            if (existing == null) {
                continue;
            }

            String existingVersion = existing.version();
            String incomingVersion = incoming.version();

            // Skip comparisons when either version is unknown.
            if (existingVersion == null || incomingVersion == null) {
                continue;
            }

            // Skip unresolved Maven property expressions.
            if (existingVersion.contains("${")
                    || incomingVersion.contains("${")) {
                continue;
            }

            if (!existingVersion.equals(incomingVersion)) {

                conflicts.add(new MavenDependencyConflict(
                        incoming.groupId(),
                        incoming.artifactId(),
                        existingVersion,
                        incomingVersion
                ));
            }
        }

        return List.copyOf(conflicts);
    }
}
