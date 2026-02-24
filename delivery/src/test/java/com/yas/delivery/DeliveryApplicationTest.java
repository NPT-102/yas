package com.yas.delivery;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for DeliveryApplication.
 * This test ensures that the Spring Boot application context loads successfully.
 */
@SpringBootTest
class DeliveryApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads without errors
        // It's a smoke test to ensure proper configuration
    }

    @Test
    void testApplicationMainMethod_shouldNotBeNull() {
        // Verify that the main method exists and can be accessed
        assertDoesNotThrow(() -> DeliveryApplication.class.getDeclaredMethod("main", String[].class));
    }

    @Test
    void testApplicationClass_shouldHaveSpringBootApplicationAnnotation() {
        // Verify Spring Boot configuration
        assertTrue(DeliveryApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void testApplicationInstance_shouldBeCreatable() {
        // Verify class can be instantiated
        assertDoesNotThrow(() -> new DeliveryApplication());
    }
}
