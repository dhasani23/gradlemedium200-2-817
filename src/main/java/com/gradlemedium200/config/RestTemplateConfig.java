package com.gradlemedium200.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration class for RestTemplate beans used in inter-service communication.
 * Provides configured instances of RestTemplate with appropriate timeout settings
 * and other common configurations needed for reliable service-to-service communication.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Connection timeout in milliseconds.
     * Default is 5 seconds if not specified in application properties.
     */
    @Value("${resttemplate.connection-timeout:5000}")
    private int connectTimeout;

    /**
     * Read timeout in milliseconds.
     * Default is 10 seconds if not specified in application properties.
     */
    @Value("${resttemplate.read-timeout:10000}")
    private int readTimeout;

    /**
     * Creates and configures a RestTemplate bean with the specified timeout settings.
     * This template is optimized for service-to-service communication within the application.
     *
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .requestFactory(this::clientHttpRequestFactory)
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    /**
     * Creates an HTTP request factory with configured timeout settings.
     * This factory is used by the RestTemplate for creating HTTP requests.
     *
     * @return Configured ClientHttpRequestFactory
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        
        // FIXME: Consider implementing a more robust request factory with connection pooling
        // using HttpComponentsClientHttpRequestFactory for production use
        
        return factory;
    }

    /**
     * Creates a RestTemplateBuilder with common configuration settings.
     * This builder can be used by other beans that need to customize RestTemplate
     * instances further based on specific requirements.
     *
     * @return Configured RestTemplateBuilder
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout));
                
        // TODO: Add default error handlers and interceptors for common functionality
        // such as logging, metrics collection, and circuit breaking
    }
}