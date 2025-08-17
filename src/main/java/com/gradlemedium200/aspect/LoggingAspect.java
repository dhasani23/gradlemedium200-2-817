package com.gradlemedium200.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging method entry, exit, and execution time.
 * 
 * This aspect provides cross-cutting logging functionality across the application by:
 * - Logging method entries with parameter values
 * - Logging method exits with return values
 * - Measuring and logging method execution times
 * 
 * The performance logging can be enabled/disabled via configuration.
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Logger instance for the aspect.
     */
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    /**
     * Flag to enable or disable performance logging.
     * Can be configured via application properties.
     */
    @Value("${logging.performance.enabled:true}")
    private boolean enablePerformanceLogging;
    
    /**
     * Logs the execution time of methods.
     * This aspect wraps around methods to measure and log their execution time.
     * 
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("execution(* com.gradlemedium200..*.*(..)) && !execution(* com.gradlemedium200.aspect..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enablePerformanceLogging) {
            return joinPoint.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.debug("Performance: {}.{} executed in {} ms", 
                    className, 
                    methodName, 
                    (endTime - startTime));
            return result;
        } catch (Throwable ex) {
            long endTime = System.currentTimeMillis();
            logger.warn("Performance: {}.{} failed after {} ms", 
                    className, 
                    methodName, 
                    (endTime - startTime));
            throw ex;
        }
    }
    
    /**
     * Logs method entry with parameter values.
     * 
     * @param joinPoint the join point representing the intercepted method
     */
    @Before("execution(* com.gradlemedium200..*.*(..)) && !execution(* com.gradlemedium200.aspect..*.*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        
        // Get parameter names and values
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        
        StringBuilder params = new StringBuilder();
        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (i > 0) {
                    params.append(", ");
                }
                params.append(parameterNames[i]).append("=");
                
                // Avoid logging sensitive information
                if (parameterNames[i].toLowerCase().contains("password") || 
                    parameterNames[i].toLowerCase().contains("token") ||
                    parameterNames[i].toLowerCase().contains("secret")) {
                    params.append("*****");
                } else {
                    params.append(formatParameterValue(parameterValues[i]));
                }
            }
        }
        
        logger.debug("Entering: {}.{}({})", className, methodName, params);
    }
    
    /**
     * Logs method exit with return value.
     * 
     * @param joinPoint the join point representing the intercepted method
     * @param result the return value of the method
     */
    @AfterReturning(
        pointcut = "execution(* com.gradlemedium200..*.*(..)) && !execution(* com.gradlemedium200.aspect..*.*(..))",
        returning = "result"
    )
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        if (result == null) {
            logger.debug("Exiting: {}.{} with null result", className, methodName);
        } else {
            String returnTypeName = ((MethodSignature) joinPoint.getSignature()).getReturnType().getSimpleName();
            
            // Check if return type is a sensitive type that should not be logged in full
            if (methodName.toLowerCase().contains("password") ||
                methodName.toLowerCase().contains("token") ||
                methodName.toLowerCase().contains("secret") ||
                returnTypeName.toLowerCase().contains("password") ||
                returnTypeName.toLowerCase().contains("token") ||
                returnTypeName.toLowerCase().contains("credential")) {
                logger.debug("Exiting: {}.{} with {}: [PROTECTED]", className, methodName, returnTypeName);
            } else {
                logger.debug("Exiting: {}.{} with {}: {}", 
                        className, 
                        methodName, 
                        returnTypeName, 
                        formatReturnValue(result));
            }
        }
    }
    
    /**
     * Formats parameter values for logging to prevent excessive log output.
     * 
     * @param value the parameter value to format
     * @return a string representation of the parameter value
     */
    private String formatParameterValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        if (value.getClass().isArray()) {
            return formatArray(value);
        }
        
        // For large collections, just log the size
        if (value instanceof Iterable) {
            int size = 0;
            for (Object ignored : (Iterable<?>) value) {
                size++;
            }
            return String.format("Collection(size=%d)", size);
        }
        
        // For large objects, just log the class name
        String valueStr = value.toString();
        if (valueStr.length() > 100) {
            return String.format("%s@%s", value.getClass().getSimpleName(), 
                    Integer.toHexString(System.identityHashCode(value)));
        }
        
        return valueStr;
    }
    
    /**
     * Formats array values for logging.
     * 
     * @param array the array to format
     * @return a string representation of the array
     */
    private String formatArray(Object array) {
        if (array instanceof Object[]) {
            return Arrays.toString((Object[]) array);
        } else if (array instanceof int[]) {
            return Arrays.toString((int[]) array);
        } else if (array instanceof long[]) {
            return Arrays.toString((long[]) array);
        } else if (array instanceof double[]) {
            return Arrays.toString((double[]) array);
        } else if (array instanceof float[]) {
            return Arrays.toString((float[]) array);
        } else if (array instanceof boolean[]) {
            return Arrays.toString((boolean[]) array);
        } else if (array instanceof byte[]) {
            byte[] bytes = (byte[]) array;
            return String.format("byte[%d]", bytes.length);
        } else if (array instanceof char[]) {
            return Arrays.toString((char[]) array);
        } else if (array instanceof short[]) {
            return Arrays.toString((short[]) array);
        } else {
            return array.getClass().getSimpleName() + "(length unknown)";
        }
    }
    
    /**
     * Formats return values for logging to prevent excessive log output.
     * 
     * @param value the return value to format
     * @return a string representation of the return value
     */
    private String formatReturnValue(Object value) {
        // For large collections, just log the size
        if (value instanceof Iterable) {
            int size = 0;
            for (Object ignored : (Iterable<?>) value) {
                size++;
            }
            return String.format("Collection(size=%d)", size);
        }
        
        if (value.getClass().isArray()) {
            return formatArray(value);
        }
        
        // For large objects, just log the class name or truncate
        String valueStr = value.toString();
        if (valueStr.length() > 100) {
            return valueStr.substring(0, 97) + "...";
        }
        
        return valueStr;
    }
    
    /**
     * Sets the enablePerformanceLogging flag.
     * Primarily used for testing or runtime configuration.
     * 
     * @param enablePerformanceLogging whether to enable performance logging
     */
    public void setEnablePerformanceLogging(boolean enablePerformanceLogging) {
        this.enablePerformanceLogging = enablePerformanceLogging;
    }
}