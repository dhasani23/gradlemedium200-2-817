package com.gradlemedium200.aspect;

import com.gradlemedium200.notification.service.NotificationService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Aspect for centralized exception handling and logging across the application.
 * Catches exceptions thrown by service methods and orchestrates appropriate
 * logging and notification activities.
 */
@Aspect
@Component
public class ExceptionHandlingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingAspect.class);
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Pointcut definition for all service methods across the application.
     * Captures methods in classes within service packages across all modules.
     */
    @Pointcut("execution(* com.gradlemedium200.*.service.*.*(..))")
    public void serviceMethodExecution() {}
    
    /**
     * Pointcut definition for controller methods that should have exception handling.
     */
    @Pointcut("execution(* com.gradlemedium200.*.controller.*.*(..))")
    public void controllerMethodExecution() {}
    
    /**
     * Pointcut definition for client methods that interact with external services.
     */
    @Pointcut("execution(* com.gradlemedium200.client.*.*(..))")
    public void clientMethodExecution() {}
    
    /**
     * Combined pointcut for methods that should have exception handling applied.
     */
    @Pointcut("serviceMethodExecution() || controllerMethodExecution() || clientMethodExecution()")
    public void applicationMethodExecution() {}
    
    /**
     * Advice that executes when any exception is thrown from the methods
     * matched by the applicationMethodExecution() pointcut.
     * 
     * @param joinPoint The join point representing the intercepted method
     * @param exception The exception that was thrown
     */
    @AfterThrowing(pointcut = "applicationMethodExecution()", throwing = "exception")
    public void handleException(JoinPoint joinPoint, Exception exception) {
        String methodName = extractMethodName(joinPoint);
        
        // Log the exception with contextual information
        logException(exception, methodName);
        
        // Determine if we need to send notification for this exception
        if (shouldNotifyOnException(exception)) {
            try {
                // Send notification to appropriate channels based on exception type and severity
                notificationService.sendSystemAlert(
                    "Exception in " + methodName, 
                    buildExceptionSummary(exception, joinPoint)
                );
            } catch (Exception notificationException) {
                // If notification fails, just log it - avoid cascading failures
                logger.error("Failed to send exception notification: {}", notificationException.getMessage(), notificationException);
            }
        }
        
        // Note: We don't rethrow the exception here as AfterThrowing advice doesn't
        // interfere with the normal exception propagation - the exception will continue up the call stack
    }
    
    /**
     * Logs the exception details with proper context information.
     * 
     * @param exception The exception to log
     * @param methodName The method name where the exception occurred
     */
    public void logException(Exception exception, String methodName) {
        // Log different exception types with appropriate log levels
        if (isClientException(exception)) {
            // For exceptions from client calls (typically connectivity issues)
            logger.warn("Client exception in {}: {}", methodName, exception.getMessage(), exception);
        } else if (isSevereException(exception)) {
            // For severe exceptions that indicate serious system issues
            logger.error("Severe exception in {}: {}", methodName, exception.getMessage(), exception);
            
            // Additional logging for severe exceptions - stack trace analysis
            String stackSummary = Arrays.stream(exception.getStackTrace())
                .limit(5)  // Only take the top 5 stack frames for brevity
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(", "));
            logger.error("Stack trace summary: {}", stackSummary);
        } else {
            // For standard exceptions
            logger.error("Exception in {}: {}", methodName, exception.getMessage(), exception);
        }
        
        // Log any causal chain
        Throwable cause = exception.getCause();
        if (cause != null) {
            logger.error("Caused by: {}", cause.getMessage(), cause);
        }
    }
    
    /**
     * Determines if an exception should trigger notifications.
     * 
     * @param exception The exception to evaluate
     * @return true if notification should be sent, false otherwise
     */
    public boolean shouldNotifyOnException(Exception exception) {
        // Don't send notifications for expected exceptions like validation errors
        if (isExpectedException(exception)) {
            return false;
        }
        
        // Send notifications for severe exceptions that require immediate attention
        if (isSevereException(exception)) {
            return true;
        }
        
        // Send notifications for client exceptions only if they persist
        if (isClientException(exception)) {
            // TODO: Implement rate limiting logic to avoid notification spam
            // Only notify if this is a persistent issue (e.g., connection failure happening repeatedly)
            return false; // Default to false until rate limiting is implemented
        }
        
        // FIXME: Implement more sophisticated decision logic based on exception context
        // For now, notify on most unexpected exceptions
        return true;
    }
    
    /**
     * Builds a detailed summary of the exception including context information.
     */
    private String buildExceptionSummary(Exception exception, JoinPoint joinPoint) {
        StringBuilder summary = new StringBuilder();
        summary.append("Exception: ").append(exception.getClass().getName()).append("\n");
        summary.append("Message: ").append(exception.getMessage()).append("\n");
        summary.append("Method: ").append(extractMethodName(joinPoint)).append("\n");
        summary.append("Arguments: ").append(formatArguments(joinPoint.getArgs())).append("\n");
        
        // Add stack trace summary
        summary.append("Stack trace (top 3):\n");
        Arrays.stream(exception.getStackTrace())
            .limit(3)
            .forEach(element -> summary.append("  ").append(element.toString()).append("\n"));
            
        return summary.toString();
    }
    
    /**
     * Formats method arguments for logging, with special handling for sensitive data.
     */
    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "none";
        }
        
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) {
                    return "null";
                }
                // Mask potentially sensitive information
                if (isSensitiveObject(arg)) {
                    return "[MASKED]";
                }
                return arg.toString();
            })
            .collect(Collectors.joining(", "));
    }
    
    /**
     * Extracts a readable method name from the join point.
     */
    private String extractMethodName(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
    
    /**
     * Checks if an object potentially contains sensitive information that should be masked.
     */
    private boolean isSensitiveObject(Object obj) {
        // Check class names that might indicate sensitive information
        String className = obj.getClass().getSimpleName().toLowerCase();
        return className.contains("password") || 
               className.contains("credential") || 
               className.contains("secret") ||
               className.contains("token") ||
               className.contains("key");
    }
    
    /**
     * Determines if an exception is considered severe enough to warrant immediate attention.
     */
    private boolean isSevereException(Exception exception) {
        // Check for serious JVM-level issues
        String exceptionClassName = exception.getClass().getName();
        return exceptionClassName.contains("OutOfMemoryError") ||
               exceptionClassName.contains("ThreadDeath") ||
               (exception.getCause() != null && 
                exception.getCause().getClass().getName().contains("OutOfMemoryError"));
    }
    
    /**
     * Determines if an exception is related to client communication issues.
     */
    private boolean isClientException(Exception exception) {
        String exName = exception.getClass().getName().toLowerCase();
        return exName.contains("timeout") ||
               exName.contains("connection") ||
               exName.contains("connect") ||
               exName.contains("client");
    }
    
    /**
     * Determines if an exception is expected as part of normal application flow.
     */
    private boolean isExpectedException(Exception exception) {
        // Validation exceptions and other business-logic related exceptions
        // that are part of normal application flow
        return exception.getClass().getSimpleName().contains("Validation") ||
               exception.getClass().getSimpleName().contains("NotFound") ||
               exception.getClass().getSimpleName().contains("AlreadyExists");
    }
}