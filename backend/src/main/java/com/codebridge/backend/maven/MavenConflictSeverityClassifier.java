
package com.codebridge.backend.maven;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MavenConflictSeverityClassifier {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    public ConflictSeverity classify(
            String existingVersion,
            String incomingVersion) {

        if (existingVersion == null || incomingVersion == null) {
            return ConflictSeverity.UNKNOWN;
        }

        Matcher existingMatcher =
                VERSION_PATTERN.matcher(existingVersion.trim());

        Matcher incomingMatcher =
                VERSION_PATTERN.matcher(incomingVersion.trim());

        // Do not guess when version formats are unfamiliar.
        if (!existingMatcher.matches() || !incomingMatcher.matches()) {
            return ConflictSeverity.UNKNOWN;
        }

        try {
            long existingMajor =
                    Long.parseLong(existingMatcher.group(1));
            long existingMinor =
                    Long.parseLong(existingMatcher.group(2));
            long existingPatch =
                    Long.parseLong(existingMatcher.group(3));

            long incomingMajor =
                    Long.parseLong(incomingMatcher.group(1));
            long incomingMinor =
                    Long.parseLong(incomingMatcher.group(2));
            long incomingPatch =
                    Long.parseLong(incomingMatcher.group(3));

            if (existingMajor != incomingMajor) {
                return ConflictSeverity.HIGH;
            }

            if (existingMinor != incomingMinor) {
                return ConflictSeverity.MEDIUM;
            }

            if (existingPatch != incomingPatch) {
                return ConflictSeverity.LOW;
            }

            // Identical versions do not represent a conflict.
            // UNKNOWN is used because the enum has no NONE value.
            return ConflictSeverity.UNKNOWN;

        } catch (NumberFormatException exception) {
            // Extremely large version numbers cannot be parsed as long.
            return ConflictSeverity.UNKNOWN;
        }
    }
}
