package com.codebridge.backend.maven;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenPomParserTest {

    private final MavenPomParser parser = new MavenPomParser();

    @Test
    void shouldExtractDependencies() {
        String xml = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-api</artifactId>
                            <version>2.0.17</version>
                            <scope>compile</scope>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>5.12.0</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals(2, result.size());
        assertEquals("org.slf4j", result.get(0).groupId());
        assertEquals("slf4j-api", result.get(0).artifactId());
        assertEquals("2.0.17", result.get(0).version());
        assertEquals("compile", result.get(0).scope());
        assertEquals("org.junit.jupiter", result.get(1).groupId());
        assertEquals("test", result.get(1).scope());
    }

    @Test
    void shouldDefaultToCompileScope() {
        String xml = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                            <artifactId>demo-library</artifactId>
                            <version>1.0.0</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals(1, result.size());
        assertEquals("compile", result.get(0).scope());
    }

    @Test
    void shouldAllowMissingVersion() {
        String xml = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals(1, result.size());
        assertNull(result.get(0).version());
    }

    @Test
    void shouldIgnoreDependencyManagement() {
        String xml = """
                <project>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.example</groupId>
                                <artifactId>managed-library</artifactId>
                                <version>2.0.0</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                </project>
                """;

        assertTrue(parser.parseDependencies(xml).isEmpty());
    }

    @Test
    void shouldRejectMalformedXml() {
        String xml = """
                <project>
                    <dependencies>
                        <dependency>
                    </dependencies>
                </project>
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseDependencies(xml)
        );
    }

    @Test
    void shouldRejectXXEAttack() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE project [
                    <!ENTITY secret SYSTEM "file:///etc/passwd">
                ]>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>&secret;</groupId>
                            <artifactId>demo</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseDependencies(xml)
        );
    }

    @Test
    void shouldRejectOversizedXml() {
        String xml = "<project>"
                + "A".repeat(512 * 1024 + 1)
                + "</project>";

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseDependencies(xml)
        );
    }

    @Test
    void shouldRejectBlankXml() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseDependencies(" ")
        );
    }

    @Test
    void shouldReturnEmptyForPomWithoutDependencies() {
        String xml = """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                </project>
                """;

        assertTrue(parser.parseDependencies(xml).isEmpty());
    }

    @Test
    void shouldResolveDependencyVersionProperty() {
        String xml = """
                <project>
                    <properties>
                        <spring.version>6.2.0</spring.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-core</artifactId>
                            <version>${spring.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals(1, result.size());
        assertEquals("6.2.0", result.get(0).version());
    }

    @Test
    void shouldResolveNestedProperties() {
        String xml = """
                <project>
                    <properties>
                        <library.version>${release.version}</library.version>
                        <release.version>2.5.0</release.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                            <artifactId>library</artifactId>
                            <version>${library.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals("2.5.0", result.get(0).version());
    }

    @Test
    void shouldResolveProjectVersion() {
        String xml = """
                <project>
                    <groupId>com.example</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1.2.3</version>
                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                            <artifactId>shared-library</artifactId>
                            <version>${project.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals("1.2.3", result.get(0).version());
    }

    @Test
    void shouldPreserveUnknownProperty() {
        String xml = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                            <artifactId>library</artifactId>
                            <version>${unknown.version}</version>
                        </dependency>
                    </dependencies>
                </project>
                """;

        List<MavenDependency> result = parser.parseDependencies(xml);

        assertEquals("${unknown.version}", result.get(0).version());
    }
}
