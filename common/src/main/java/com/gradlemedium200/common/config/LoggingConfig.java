package com.gradlemedium200.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up different types of loggers and managing logging configuration.
 * This class provides custom loggers for different purposes like audit logging, 
 * performance monitoring, and security events.
 */
@Configuration
public class LoggingConfig {

    /**
     * Default log level for the application
     */
    @Value("${logging.level.root:INFO}")
    private String logLevel;
    
    /**
     * Pattern format for log messages
     */
    @Value("${logging.pattern.console:%d{yyyy-MM-dd HH:mm:ss} - %msg%n}")
    private String logPattern;
    
    /**
     * Path where log files will be stored
     */
    @Value("${logging.file.path:logs}")
    private String logPath;
    
    /**
     * Creates a logger dedicated for audit events.
     * Audit logs capture system events that need to be recorded for compliance or tracking purposes.
     *
     * @return Logger configured for audit events
     */
    @Bean
    public Logger auditLogger() {
        Logger logger = LoggerFactory.getLogger("auditLogger");
        // TODO: Add additional audit logger configuration if needed
        return logger;
    }
    
    /**
     * Creates a logger dedicated for performance monitoring.
     * Performance logs track execution times and system resource usage for optimization.
     *
     * @return Logger configured for performance monitoring
     */
    @Bean
    public Logger performanceLogger() {
        Logger logger = LoggerFactory.getLogger("performanceLogger");
        // TODO: Consider adding MDC context for tracking performance metrics
        return logger;
    }
    
    /**
     * Creates a logger dedicated for security events.
     * Security logs track authentication, authorization, and other security-related events.
     *
     * @return Logger configured for security events
     */
    @Bean
    public Logger securityLogger() {
        Logger logger = LoggerFactory.getLogger("securityLogger");
        // FIXME: Security logger should use a separate appender with encryption
        return logger;
    }
    
    /**
     * Dynamically configures the log level for a specific logger.
     * This method allows runtime modification of logging levels without application restart.
     *
     * @param loggerName the name of the logger to configure
     * @param level the log level to set (e.g., "DEBUG", "INFO", "WARN", "ERROR")
     */
    public void configureLogLevel(String loggerName, String level) {
        if (loggerName == null || level == null) {
            throw new IllegalArgumentException("Logger name and level must not be null");
        }
        
        try {
            // Using reflection to access the underlying logging implementation
            // This is a workaround as SLF4J doesn't provide direct access to change log levels
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) 
                LoggerFactory.getLogger(loggerName);
            
            ch.qos.logback.classic.Level logbackLevel = ch.qos.logback.classic.Level.toLevel(level);
            logger.setLevel(logbackLevel);
            
            // Log the change
            LoggerFactory.getLogger(LoggingConfig.class)
                .info("Log level for '{}' changed to '{}'", loggerName, level);
        } catch (Exception e) {
            // FIXME: Improve error handling for unsupported logging implementations
            LoggerFactory.getLogger(LoggingConfig.class)
                .error("Failed to set log level for logger '{}' to '{}': {}", 
                    loggerName, level, e.getMessage());
            throw new RuntimeException("Failed to configure log level", e);
        }
    }
    
    /**
     * Returns the configured default log level
     * 
     * @return the default log level
     */
    public String getLogLevel() {
        return logLevel;
    }
    
    /**
     * Returns the configured log pattern
     * 
     * @return the log pattern format
     */
    public String getLogPattern() {
        return logPattern;
    }
    
    /**
     * Returns the configured log file path
     * 
     * @return the log file path
     */
    public String getLogPath() {
        return logPath;
    }
}