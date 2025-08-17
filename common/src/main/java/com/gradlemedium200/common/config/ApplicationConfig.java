package com.gradlemedium200.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Main application configuration class that provides common beans and settings
 * used across the application. This class centralizes configuration of core
 * application components like task executors, REST templates, object mappers,
 * message sources, and event handling.
 */
@Configuration
public class ApplicationConfig {

    @Value("${application.name:GradleMedium200}")
    private String appName;
    
    @Value("${application.version:1.0.0}")
    private String appVersion;
    
    @Value("${application.environment:dev}")
    private String environment;
    
    /**
     * Creates a thread pool task executor for handling asynchronous tasks.
     * The executor is configured with reasonable defaults for a medium-sized application.
     * 
     * @return Configured TaskExecutor
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("app-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Add environment-specific configurations
        if ("prod".equals(environment)) {
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(20);
            executor.setQueueCapacity(50);
        }
        
        return executor;
    }
    
    /**
     * Creates a REST template with custom configuration for making HTTP requests.
     * The template is configured with connection/read timeouts and custom message converters.
     * 
     * @return Configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        
        // Configure message converters
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper());
        messageConverters.add(jsonConverter);
        
        restTemplate.setMessageConverters(messageConverters);
        
        return restTemplate;
    }
    
    /**
     * Helper method to create a ClientHttpRequestFactory with appropriate timeout settings
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Configure connection and read timeouts
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        
        // Add environment-specific configurations
        if ("prod".equals(environment)) {
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(7000);
        } else if ("test".equals(environment)) {
            factory.setConnectTimeout(2000);
            factory.setReadTimeout(4000);
        }
        
        return factory;
    }
    
    /**
     * Creates a Jackson ObjectMapper with custom settings for JSON serialization/deserialization.
     * 
     * @return Configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure serialization features
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, !isProdEnvironment());
        
        // Configure deserialization features
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // TODO: Register custom modules for handling Java 8 date/time types
        
        return mapper;
    }
    
    /**
     * Creates a message source for internationalization support.
     * Configures the base names for resource bundles, default encoding, and caching.
     * 
     * @return Configured MessageSource
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        
        messageSource.setBasenames(
            "messages/common",
            "messages/validation",
            "messages/error"
        );
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds(isProdEnvironment() ? -1 : 60);
        
        // FIXME: Consider using ReloadableResourceBundleMessageSource for hot reloading in dev
        
        return messageSource;
    }
    
    /**
     * Creates an asynchronous application event multicaster for handling application events.
     * Events will be processed asynchronously by the configured task executor.
     * 
     * @return Configured ApplicationEventMulticaster
     */
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(taskExecutor());
        
        // TODO: Add error handler for event processing
        
        return eventMulticaster;
    }
    
    /**
     * Helper method to check if the current environment is production
     * 
     * @return true if production environment, false otherwise
     */
    private boolean isProdEnvironment() {
        return "prod".equalsIgnoreCase(environment);
    }
}