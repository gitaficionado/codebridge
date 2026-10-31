
package com.codebridge.backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldRejectEmptyProjectName() {

        ProjectRequest request = new ProjectRequest(
                "",
                "https://github.com/example/project-a",
                "https://github.com/example/project-b"
        );

        var violations = validator.validate(request);

        assertTrue(
                violations.stream().anyMatch(
                        violation ->
                                violation.getPropertyPath()
                                        .toString().equals("name")
                )
        );
    }

    @Test
    void shouldAcceptValidProjectRequest() {

        ProjectRequest request = new ProjectRequest(
                "Payment Integration",
                "https://github.com/example/project-a",
                "https://github.com/example/project-b"
        );

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
