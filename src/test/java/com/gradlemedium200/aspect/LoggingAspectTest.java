package com.gradlemedium200.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggingAspect}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;
    
    @Mock
    private Signature signature;
    
    @Mock
    private MethodSignature methodSignature;
    
    @Mock
    private Logger logger;
    
    private LoggingAspect loggingAspect;
    
    @Before
    public void setUp() throws Exception {
        // Initialize loggingAspect and set mocked logger via reflection
        loggingAspect = new LoggingAspect();
        
        // Use reflection to set the mocked logger
        java.lang.reflect.Field loggerField = LoggingAspect.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(loggingAspect, logger);
        
        // Set up common mocking behavior
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.TestClass");
        when(signature.getName()).thenReturn("testMethod");
    }
    
    @Test
    public void testLogExecutionTime_Success() throws Throwable {
        // Given
        Object expectedResult = "test result";
        when(proceedingJoinPoint.proceed()).thenReturn(expectedResult);
        loggingAspect.setEnablePerformanceLogging(true);
        
        // When
        Object result = loggingAspect.logExecutionTime(proceedingJoinPoint);
        
        // Then
        verify(logger).debug(eq("Performance: {}.{} executed in {} ms"), 
                eq("com.example.TestClass"), 
                eq("testMethod"), 
                any(Long.class));
    }
    
    @Test
    public void testLogExecutionTime_Exception() throws Throwable {
        // Given
        RuntimeException exception = new RuntimeException("Test exception");
        when(proceedingJoinPoint.proceed()).thenThrow(exception);
        loggingAspect.setEnablePerformanceLogging(true);
        
        // When
        try {
            loggingAspect.logExecutionTime(proceedingJoinPoint);
        } catch (Exception e) {
            // Expected
        }
        
        // Then
        verify(logger).warn(eq("Performance: {}.{} failed after {} ms"), 
                eq("com.example.TestClass"), 
                eq("testMethod"), 
                any(Long.class));
    }
    
    @Test
    public void testLogExecutionTime_PerformanceLoggingDisabled() throws Throwable {
        // Given
        Object expectedResult = "test result";
        when(proceedingJoinPoint.proceed()).thenReturn(expectedResult);
        loggingAspect.setEnablePerformanceLogging(false);
        
        // When
        Object result = loggingAspect.logExecutionTime(proceedingJoinPoint);
        
        // Then
        // Verify that no debug logging occurs
        verify(logger).debug(anyString(), any(Object[].class));
    }
    
    // TODO: Add more comprehensive tests for logMethodEntry and logMethodExit
    // FIXME: Test case for sensitive parameter handling in logMethodEntry
}