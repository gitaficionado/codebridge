
package com.codebridge.backend.maven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MavenConflictSeverityClassifierTest {

    private MavenConflictSeverityClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new MavenConflictSeverityClassifier();
    }

    @Test
    void shouldClassifyMajorVersionDifferenceAsHigh() {
        assertEquals(
                ConflictSeverity.HIGH,
                classifier.classify("1.7.36", "2.0.17")
        );
    }

    @Test
    void shouldClassifyMinorVersionDifferenceAsMedium() {
        assertEquals(
                ConflictSeverity.MEDIUM,
                classifier.classify("2.0.0", "2.1.0")
        );
    }

    @Test
    void shouldClassifyPatchVersionDifferenceAsLow() {
        assertEquals(
                ConflictSeverity.LOW,
                classifier.classify("2.0.16", "2.0.17")
        );
    }

    @Test
    void shouldClassifyIdenticalVersionsAsUnknown() {
        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify("2.0.17", "2.0.17")
        );
    }

    @Test
    void shouldClassifyNonstandardVersionsAsUnknown() {
        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify("1.0-SNAPSHOT", "2.0-custom")
        );
    }

    @Test
    void shouldClassifyMissingVersionsAsUnknown() {
        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify(null, "2.0.17")
        );

        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify("2.0.17", null)
        );
    }

    @Test
    void shouldClassifyVeryLargeVersionNumbersAsUnknown() {
        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify(
                        "999999999999999999999.0.0",
                        "2.0.0"
                )
        );
    }

    @Test
    void shouldIgnoreSurroundingWhitespace() {
        assertEquals(
                ConflictSeverity.HIGH,
                classifier.classify(" 1.7.36 ", " 2.0.17 ")
        );
    }

    @Test
    void shouldClassifyTwoComponentVersionsAsUnknown() {
        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify("1.0", "2.0")
        );
    }

    @Test
    void shouldClassifyNonNumericVersionsAsUnknown() {
        assertEquals(
                ConflictSeverity.UNKNOWN,
                classifier.classify("latest", "release")
        );
    }

    @Test
    void shouldClassifyVersionDifferencesInEitherDirection() {
        assertEquals(
                ConflictSeverity.HIGH,
                classifier.classify("3.0.0", "2.0.0")
        );

        assertEquals(
                ConflictSeverity.LOW,
                classifier.classify("2.0.17", "2.0.16")
        );
    }
}
