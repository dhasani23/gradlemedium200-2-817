package com.gradlemedium200.integration;

import com.gradlemedium200.dto.ComponentHealth;
import com.gradlemedium200.dto.HealthStatus;
import com.gradlemedium200.service.HealthCheckService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration test for health check endpoints and functionality.
 * Tests various aspects of the health check system including basic health check,
 * detailed component statuses, AWS services health, and custom health indicators.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private HealthCheckService healthCheckService;

    private String baseUrl;

    @Before
    public void setUp() {
        this.baseUrl = "http://localhost:" + port;
        // Clear any cached health status before each test
        healthCheckService.invalidateCache();
    }

    /**
     * Tests basic health check endpoint.
     * Verifies that the basic health endpoint returns a successful response and 
     * that the system is reported as healthy.
     */
    @Test
    public void testBasicHealthCheck() {
        // Call the basic health endpoint
        ResponseEntity<HealthStatus> response = testRestTemplate.getForEntity(
                baseUrl + "/actuator/health", HealthStatus.class);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getStatus());
        
        // System should be healthy in the test environment
        assertEquals("UP", response.getBody().getStatus());
        assertTrue(response.getBody().isHealthy());
        
        // Basic health check should have a timestamp
        assertNotNull(response.getBody().getTimestamp());
        
        // Version information should be present
        assertNotNull(response.getBody().getVersion());
    }

    /**
     * Tests detailed health check with component status.
     * Verifies that the detailed health check includes status for all major components.
     */
    @Test
    public void testDetailedHealthCheck() {
        // Call the detailed health endpoint
        ResponseEntity<HealthStatus> response = testRestTemplate.getForEntity(
                baseUrl + "/actuator/health/detail", HealthStatus.class);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Detailed health should include component statuses
        Map<String, ComponentHealth> components = response.getBody().getComponents();
        assertNotNull(components);
        assertFalse(components.isEmpty());
        
        // Verify essential components are present
        assertTrue(components.containsKey("diskSpace") || 
                   components.containsKey("db") || 
                   components.containsKey("application"));
        
        // Check response structure for a component
        for (Map.Entry<String, ComponentHealth> entry : components.entrySet()) {
            ComponentHealth componentHealth = entry.getValue();
            assertNotNull(componentHealth.getStatus());
            // In test environment, we expect details to be populated
            assertNotNull(componentHealth.getDetails());
        }
    }

    /**
     * Tests AWS services health indicators.
     * Verifies that AWS-specific health indicators are present and correctly formatted.
     */
    @Test
    public void testAwsServicesHealth() {
        // Get AWS health directly from service (avoiding HTTP to isolate AWS-specific tests)
        HealthStatus awsHealth = healthCheckService.checkExternalDependencies();
        
        // AWS health should be populated
        assertNotNull(awsHealth);
        
        Map<String, ComponentHealth> components = awsHealth.getComponents();
        assertNotNull(components);
        
        // AWS components might include these services based on application configuration
        // Note: Test may need adjustment based on which AWS services are actually used
        boolean hasAwsComponents = components.keySet().stream()
                .anyMatch(key -> key.contains("aws") || 
                           key.contains("dynamodb") || 
                           key.contains("sns") || 
                           key.contains("sqs"));
        
        assertTrue("No AWS components found in health check", hasAwsComponents);
        
        // Validate AWS components structure
        components.entrySet().stream()
                .filter(entry -> entry.getKey().contains("aws") || 
                        entry.getKey().contains("dynamodb") || 
                        entry.getKey().contains("sns") || 
                        entry.getKey().contains("sqs"))
                .forEach(entry -> {
                    ComponentHealth health = entry.getValue();
                    assertNotNull("AWS component status is null: " + entry.getKey(), 
                            health.getStatus());
                    assertNotNull("AWS component details are null: " + entry.getKey(), 
                            health.getDetails());
                });
    }

    /**
     * Tests custom health indicators.
     * Verifies that custom health indicators defined by the application are functioning properly.
     */
    @Test
    public void testCustomHealthIndicators() {
        // Call the health endpoint with full details
        ResponseEntity<HealthStatus> response = testRestTemplate.getForEntity(
                baseUrl + "/actuator/health/full", HealthStatus.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, ComponentHealth> components = response.getBody().getComponents();
        assertNotNull(components);
        
        // Check for custom health indicators
        // Note: Update the expected indicator names based on actual custom health indicators
        boolean hasCustomIndicators = components.keySet().stream()
                .anyMatch(key -> key.contains("custom") || 
                           key.contains("dynamo") ||
                           key.contains("health"));
        
        assertTrue("No custom health indicators found", hasCustomIndicators);
        
        // Test a specific custom health indicator if available
        if (components.containsKey("customHealthIndicator")) {
            ComponentHealth customHealth = components.get("customHealthIndicator");
            assertNotNull(customHealth);
            assertNotNull(customHealth.getStatus());
            assertNotNull(customHealth.getDetails());
        }
        
        // FIXME: This test may need adjustments based on which custom health indicators
        // are actually implemented in the application
    }
}