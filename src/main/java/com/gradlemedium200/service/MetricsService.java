package com.gradlemedium200.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for collecting and exposing application metrics using Micrometer.
 * This service provides methods to record API requests, response times, and custom metrics.
 */
@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer responseTimer;
    
    // Store endpoint-specific counters and timers
    private final Map<String, Counter> endpointCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> endpointTimers = new ConcurrentHashMap<>();
    
    /**
     * Constructs a new MetricsService with the provided MeterRegistry.
     *
     * @param meterRegistry the Micrometer registry for metrics collection
     */
    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize global counters and timers
        this.requestCounter = Counter.builder("api.requests.total")
                .description("Total number of API requests")
                .register(meterRegistry);
                
        this.responseTimer = Timer.builder("api.response.time")
                .description("API request response time")
                .register(meterRegistry);
                
        logger.info("MetricsService initialized with Micrometer registry");
    }
    
    /**
     * Increments the request counter for a specific endpoint and HTTP method.
     * 
     * @param endpoint the API endpoint path
     * @param method the HTTP method (GET, POST, etc.)
     */
    public void incrementRequestCounter(String endpoint, String method) {
        // Increment global counter
        requestCounter.increment();
        
        // Get or create and increment endpoint-specific counter
        String counterKey = createEndpointMethodKey(endpoint, method);
        Counter endpointCounter = endpointCounters.computeIfAbsent(counterKey, key -> 
            Counter.builder("api.requests")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .description("Number of requests to " + endpoint)
                .register(meterRegistry)
        );
        
        endpointCounter.increment();
        logger.debug("Request counter incremented for {}", counterKey);
    }
    
    /**
     * Records response time for a specific endpoint.
     * 
     * @param endpoint the API endpoint path
     * @param duration response time duration in milliseconds
     */
    public void recordResponseTime(String endpoint, long duration) {
        // Record in global timer
        responseTimer.record(duration, TimeUnit.MILLISECONDS);
        
        // Get or create and record in endpoint-specific timer
        Timer endpointTimer = endpointTimers.computeIfAbsent(endpoint, key -> 
            Timer.builder("api.endpoint.response.time")
                .tag("endpoint", endpoint)
                .description("Response time for " + endpoint)
                .register(meterRegistry)
        );
        
        endpointTimer.record(duration, TimeUnit.MILLISECONDS);
        logger.debug("Response time recorded for {}: {} ms", endpoint, duration);
    }
    
    /**
     * Records a custom metric value.
     * 
     * @param metricName the name of the metric
     * @param value the metric value
     */
    public void recordCustomMetric(String metricName, double value) {
        // Register or update a gauge for the custom metric
        // FIXME: This implementation creates a new gauge each time which is not ideal.
        // Should use a more permanent storage for gauge values
        Gauge.builder("custom." + metricName, () -> value)
             .description("Custom metric: " + metricName)
             .register(meterRegistry);
             
        logger.debug("Custom metric recorded: {}={}", metricName, value);
    }
    
    /**
     * Returns a summary of collected metrics.
     * 
     * @return map containing metric summaries
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Get total request count
        summary.put("totalRequests", requestCounter.count());
        
        // Get per-endpoint request counts
        Map<String, Double> endpointCounts = new HashMap<>();
        endpointCounters.forEach((key, counter) -> endpointCounts.put(key, counter.count()));
        summary.put("endpointRequests", endpointCounts);
        
        // Get response time statistics
        summary.put("averageResponseTimeMs", responseTimer.mean(TimeUnit.MILLISECONDS));
        summary.put("maxResponseTimeMs", responseTimer.max(TimeUnit.MILLISECONDS));
        
        // TODO: Add additional metrics as needed, such as error rates, system metrics, etc.
        
        logger.info("Generated metrics summary with {} endpoint metrics", endpointCounts.size());
        return summary;
    }
    
    /**
     * Creates a key for storing endpoint and method specific metrics.
     *
     * @param endpoint the API endpoint path
     * @param method the HTTP method
     * @return a combined key
     */
    private String createEndpointMethodKey(String endpoint, String method) {
        return endpoint + ":" + method;
    }
}