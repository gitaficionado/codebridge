package com.codebridge.backend.maven;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MavenPomParser {

    private static final int MAX_XML_BYTES = 512 * 1024;
    private static final Pattern PROPERTY_PATTERN =
            Pattern.compile("\\$\\{([^}]+)}");
    private static final int MAX_PROPERTY_RESOLUTION_PASSES = 10;

    public List<MavenDependency> parseDependencies(String xml) {

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException(
                    "Maven pom.xml content is required"
            );
        }

        if (xml.getBytes(StandardCharsets.UTF_8).length > MAX_XML_BYTES) {
            throw new IllegalArgumentException(
                    "Maven pom.xml exceeds the allowed size"
            );
        }

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newDefaultInstance();

            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            factory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING,
                    true
            );

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    ""
            );

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(
                    new InputSource(new StringReader(xml))
            );

            Element root = document.getDocumentElement();

            if (root == null || !"project".equals(root.getLocalName())) {
                throw new IllegalArgumentException(
                        "XML is not a Maven POM"
                );
            }

            Map<String, String> properties = collectProperties(root);

            Element dependenciesElement = directChild(root, "dependencies");

            if (dependenciesElement == null) {
                return List.of();
            }

            List<MavenDependency> dependencies = new ArrayList<>();
            NodeList children = dependenciesElement.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);

                if (!(node instanceof Element element)
                        || !"dependency".equals(element.getLocalName())) {
                    continue;
                }

                String groupId = resolveProperties(
                        childText(element, "groupId"),
                        properties
                );

                String artifactId = resolveProperties(
                        childText(element, "artifactId"),
                        properties
                );

                String version = resolveProperties(
                        childText(element, "version"),
                        properties
                );

                String scope = resolveProperties(
                        childText(element, "scope"),
                        properties
                );

                if (groupId == null || artifactId == null) {
                    throw new IllegalArgumentException(
                            "Maven dependency is missing groupId or artifactId"
                    );
                }

                dependencies.add(new MavenDependency(
                        groupId,
                        artifactId,
                        version,
                        scope == null ? "compile" : scope
                ));
            }

            return List.copyOf(dependencies);

        } catch (ParserConfigurationException exception) {
            throw new IllegalStateException(
                    "Secure XML parser configuration failed",
                    exception
            );

        } catch (SAXException | IOException exception) {
            throw new IllegalArgumentException(
                    "Invalid or unsafe Maven pom.xml",
                    exception
            );
        }
    }

    private Map<String, String> collectProperties(Element root) {
        Map<String, String> properties = new HashMap<>();

        String projectGroupId = childText(root, "groupId");
        String projectArtifactId = childText(root, "artifactId");
        String projectVersion = childText(root, "version");

        if (projectGroupId != null) {
            properties.put("project.groupId", projectGroupId);
        }

        if (projectArtifactId != null) {
            properties.put("project.artifactId", projectArtifactId);
        }

        if (projectVersion != null) {
            properties.put("project.version", projectVersion);
        }

        Element propertiesElement = directChild(root, "properties");

        if (propertiesElement == null) {
            return properties;
        }

        NodeList children = propertiesElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (!(node instanceof Element element)) {
                continue;
            }

            String name = element.getLocalName();
            String value = element.getTextContent();

            if (name == null || value == null) {
                continue;
            }

            value = value.trim();

            if (!value.isEmpty()) {
                properties.put(name, value);
            }
        }

        return properties;
    }

    private String resolveProperties(
            String value,
            Map<String, String> properties) {

        if (value == null) {
            return null;
        }

        String resolved = value;

        for (int pass = 0; pass < MAX_PROPERTY_RESOLUTION_PASSES; pass++) {
            Matcher matcher = PROPERTY_PATTERN.matcher(resolved);
            StringBuffer buffer = new StringBuffer();
            boolean changed = false;

            while (matcher.find()) {
                String propertyName = matcher.group(1);
                String replacement = properties.get(propertyName);

                if (replacement == null) {
                    matcher.appendReplacement(
                            buffer,
                            Matcher.quoteReplacement(matcher.group(0))
                    );
                } else {
                    matcher.appendReplacement(
                            buffer,
                            Matcher.quoteReplacement(replacement)
                    );
                    changed = true;
                }
            }

            matcher.appendTail(buffer);

            if (!changed) {
                return resolved;
            }

            resolved = buffer.toString();
        }

        return resolved;
    }

    private Element directChild(Element parent, String name) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node instanceof Element element
                    && name.equals(element.getLocalName())) {
                return element;
            }
        }

        return null;
    }

    private String childText(Element parent, String name) {
        Element child = directChild(parent, name);

        if (child == null) {
            return null;
        }

        String value = child.getTextContent().trim();

        return value.isEmpty() ? null : value;
    }
}