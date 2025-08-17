package com.gradlemedium200.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.gradlemedium200.dto.ApiResponse;

/**
 * Circuit Breaker implementation for fault tolerance.
 * Helps prevent cascading failures and allows recovery during service outages.
 */
public class CircuitBreaker {
    
    private enum State {
        CLOSED,      // Normal operation - requests pass through
        OPEN,        // Failing - requests immediately fail without attempting execution
        HALF_OPEN    // Testing recovery - limited requests pass through to test if service is back
    }
    
    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final AtomicInteger failureCount;
    private final AtomicLong lastFailureTime;
    private volatile State state;
    
    /**
     * Creates a new CircuitBreaker instance
     * 
     * @param name Name of the circuit breaker
     * @param failureThreshold Number of failures before tripping the circuit
     * @param resetTimeoutMs Time in milliseconds before attempting to reset from OPEN to HALF_OPEN
     */
    public CircuitBreaker(String name, int failureThreshold, long resetTimeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.failureCount = new AtomicInteger(0);
        this.lastFailureTime = new AtomicLong(0);
        this.state = State.CLOSED;
    }
    
    /**
     * Execute the given operation with circuit breaker protection
     * 
     * @param <T> Return type of the operation
     * @param operation Operation to execute
     * @param fallback Fallback function to use when circuit is open
     * @return Result of the operation or fallback
     */
    public <T> T execute(Supplier<T> operation, Supplier<T> fallback) {
        if (isOpen()) {
            // Check if we should attempt reset
            if (shouldAttemptReset()) {
                // Try to move to HALF_OPEN state and test with a single request
                synchronized (this) {
                    if (state == State.OPEN) {
                        state = State.HALF_OPEN;
                    }
                }
            }
            
            // Still open, use fallback
            if (state == State.OPEN) {
                return fallback.get();
            }
        }
        
        // Circuit is CLOSED or HALF_OPEN, attempt to execute the operation
        try {
            T result = operation.get();
            
            // If we were in HALF_OPEN and succeeded, reset to CLOSED
            if (state == State.HALF_OPEN) {
                reset();
            }
            
            return result;
        } catch (Exception e) {
            // Record failure
            recordFailure();
            return fallback.get();
        }
    }
    
    /**
     * Execute operation with circuit breaker protection and default null fallback
     * 
     * @param <T> Return type of the operation
     * @param operation Operation to execute
     * @return Result of the operation or null on failure
     */
    public <T> T execute(Supplier<T> operation) {
        return execute(operation, () -> null);
    }
    
    /**
     * Execute operation that returns ApiResponse with specific handling for that type
     *
     * @param operation Operation that returns ApiResponse
     * @param fallback Fallback function that returns ApiResponse when circuit is open
     * @return ApiResponse from operation or fallback
     */
    public ApiResponse executeApiResponse(Supplier<ApiResponse> operation, Supplier<ApiResponse> fallback) {
        if (isOpen()) {
            // Check if we should attempt reset
            if (shouldAttemptReset()) {
                // Try to move to HALF_OPEN state and test with a single request
                synchronized (this) {
                    if (state == State.OPEN) {
                        state = State.HALF_OPEN;
                    }
                }
            }
            
            // Still open, use fallback
            if (state == State.OPEN) {
                return fallback.get();
            }
        }
        
        // Circuit is CLOSED or HALF_OPEN, attempt to execute the operation
        try {
            ApiResponse result = operation.get();
            
            // If we were in HALF_OPEN and succeeded, reset to CLOSED
            if (state == State.HALF_OPEN) {
                reset();
            }
            
            return result;
        } catch (Exception e) {
            // Record failure
            recordFailure();
            return fallback.get();
        }
    }
    
    /**
     * Record a failure and potentially trip the circuit
     */
    public void recordFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        
        if (state == State.CLOSED) {
            if (failureCount.incrementAndGet() >= failureThreshold) {
                tripBreaker();
            }
        } else if (state == State.HALF_OPEN) {
            // Any failure in half-open state should trip the circuit again
            tripBreaker();
        }
    }
    
    /**
     * Reset the circuit breaker to closed state
     */
    public void reset() {
        synchronized (this) {
            failureCount.set(0);
            state = State.CLOSED;
        }
    }
    
    /**
     * Get the current state of the circuit breaker
     * 
     * @return Current state
     */
    public String getState() {
        return state.name();
    }
    
    /**
     * Get the name of this circuit breaker
     * 
     * @return Circuit breaker name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Check if the circuit breaker is currently open (not allowing requests)
     * 
     * @return true if open, false otherwise
     */
    private boolean isOpen() {
        return state == State.OPEN;
    }
    
    /**
     * Trip the circuit breaker to OPEN state
     */
    private synchronized void tripBreaker() {
        state = State.OPEN;
    }
    
    /**
     * Check if we should attempt to reset the circuit
     * 
     * @return true if enough time has elapsed since the last failure
     */
    private boolean shouldAttemptReset() {
        long lastFailure = lastFailureTime.get();
        return lastFailure > 0 && System.currentTimeMillis() - lastFailure > resetTimeoutMs;
    }
}