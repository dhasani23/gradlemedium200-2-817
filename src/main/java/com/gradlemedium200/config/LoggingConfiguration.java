package com.gradlemedium200.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.async.AsyncAppender;
import ch.qos.logback.core.util.FileSize;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for setting up centralized logging across the application.
 * Provides beans for logger context, file appenders, and asynchronous appenders to optimize
 * logging performance while ensuring all important information is captured.
 */
@Configuration
public class LoggingConfiguration {

    @Value("${logging.level.root:INFO}")
    private String logLevel;
    
    @Value("${logging.file.path:/var/log/gradlemedium200}")
    private String logFileLocation;
    
    @Value("${logging.json.enabled:false}")
    private boolean enableJsonLogging;
    
    /**
     * Configures the main logger context for the application.
     * This context is the foundation for all logging operations.
     * 
     * @return LoggerContext configured with application defaults
     */
    @Bean
    public LoggerContext loggerContext() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Reset any existing configuration
        context.reset();
        
        // Set the context name for identification
        context.setName("GradleMedium200-LoggerContext");
        
        // Configure default properties
        context.putProperty("LOG_LEVEL", logLevel);
        context.putProperty("LOG_FILE_LOCATION", logFileLocation);
        context.putProperty("JSON_LOGGING", String.valueOf(enableJsonLogging));
        
        return context;
    }
    
    /**
     * Creates an asynchronous appender for improved logging performance.
     * Using async appenders prevents logging operations from blocking the main application thread.
     * 
     * @return AsyncAppender configured with optimal performance settings
     */
    @Bean
    public AsyncAppender asyncAppender() {
        LoggerContext context = loggerContext();
        
        AsyncAppender appender = new AsyncAppender();
        appender.setContext(context);
        appender.setName("ASYNC");
        
        // Prevent loss of messages on queue overflow
        appender.setDiscardingThreshold(0);
        
        // Queue size impacts memory usage and how many log events can be buffered
        appender.setQueueSize(512);
        
        // Never block when queue is full (discard messages if necessary)
        appender.setNeverBlock(true);
        
        // Create and attach a console appender as a default target
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE");
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        
        // Choose pattern based on JSON logging preference
        if (enableJsonLogging) {
            // Simple JSON format - in production would use a proper JSON formatter
            encoder.setPattern("{ \"timestamp\": \"%d\", \"level\": \"%level\", \"thread\": \"%thread\", \"logger\": \"%logger\", \"message\": \"%message\", \"exception\": \"%exception\" }%n");
        } else {
            // Standard logging pattern
            encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        }
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        // Add console appender to async appender
        appender.addAppender(consoleAppender);
        appender.start();
        
        return appender;
    }
    
    /**
     * Creates a file appender for persisting logs to disk.
     * This ensures logs are available for review even after application restart.
     * 
     * @return FileAppender configured to write to the specified location
     */
    @Bean
    public FileAppender<ILoggingEvent> fileAppender() {
        LoggerContext context = loggerContext();
        
        FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");
        
        // Construct file path
        String filePath = logFileLocation + "/application.log";
        appender.setFile(filePath);
        
        // Set file appender properties
        appender.setAppend(true);
        appender.setPrudent(false); // Set to true for multi-process safe logging (slower)
        
        // Configure with a reasonable buffer size for efficiency
        appender.setBufferSize(FileSize.valueOf("8KB"));
        
        // Create and configure encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        
        if (enableJsonLogging) {
            // Simple JSON format - in production would use a proper JSON formatter
            encoder.setPattern("{ \"timestamp\": \"%d\", \"level\": \"%level\", \"thread\": \"%thread\", \"logger\": \"%logger\", \"message\": \"%message\", \"exception\": \"%exception\" }%n");
        } else {
            // Standard logging pattern with more details for file logging
            encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        }
        encoder.start();
        
        appender.setEncoder(encoder);
        
        // TODO: Add log rotation policy to prevent log files from growing too large
        
        // FIXME: Current implementation doesn't handle log file rotation
        // Should add a RollingFileAppender with size and time based policies
        
        appender.start();
        
        return appender;
    }
    
    /**
     * Helper method to check if the log directory exists and is writable.
     * If directory doesn't exist, attempts to create it.
     * 
     * @return boolean indicating if the log directory is usable
     */
    private boolean ensureLogDirectoryExists() {
        java.io.File logDir = new java.io.File(logFileLocation);
        if (!logDir.exists()) {
            return logDir.mkdirs();
        }
        return logDir.isDirectory() && logDir.canWrite();
    }
}