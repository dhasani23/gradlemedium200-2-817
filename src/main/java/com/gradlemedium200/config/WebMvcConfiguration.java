package com.gradlemedium200.config;

import com.gradlemedium200.interceptor.RequestInterceptor;
import com.gradlemedium200.interceptor.ResponseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for interceptors and web-related settings.
 * This class registers interceptors and configures other web-related behaviors
 * for the application.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfiguration.class);
    
    private final RequestInterceptor requestInterceptor;
    private final ResponseInterceptor responseInterceptor;
    
    @Value("${app.logging.detailed-request-logging:false}")
    private boolean detailedRequestLogging;
    
    @Value("${app.logging.response-body-logging:false}")
    private boolean responseBodyLogging;
    
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;
    
    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    
    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;
    
    @Value("${app.cors.max-age:3600}")
    private long maxAge;
    
    @Value("${app.mvc.use-suffix-pattern-match:false}")
    private boolean useSuffixPatternMatch;
    
    @Value("${app.mvc.use-registered-suffix-pattern-match:true}")
    private boolean useRegisteredSuffixPatternMatch;
    
    /**
     * Constructs a new WebMvcConfiguration with the required interceptors.
     * 
     * @param requestInterceptor the interceptor for processing requests
     * @param responseInterceptor the interceptor for processing responses
     */
    @Autowired
    public WebMvcConfiguration(RequestInterceptor requestInterceptor, ResponseInterceptor responseInterceptor) {
        this.requestInterceptor = requestInterceptor;
        this.responseInterceptor = responseInterceptor;
        logger.info("WebMvcConfiguration initialized");
    }
    
    /**
     * Adds interceptors to the application's interceptor registry.
     * This registers both request and response interceptors to be applied to
     * all requests matching the specified patterns.
     * 
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Set logging configurations from properties
        requestInterceptor.setEnableDetailedLogging(detailedRequestLogging);
        responseInterceptor.setLogResponseBody(responseBodyLogging);
        
        // Register the request interceptor for all requests
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health/**", "/actuator/**", "/favicon.ico", "/error");
        
        // Register the response interceptor for all requests
        registry.addInterceptor(responseInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health/**", "/actuator/**", "/favicon.ico", "/error");
        
        logger.info("Interceptors registered - Request and Response monitoring is active");
    }
    
    /**
     * Configures Cross-Origin Resource Sharing (CORS) mappings.
     * This enables controlled access to resources located outside the requesting domain.
     * 
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(true)
                .maxAge(maxAge);
        
        logger.info("CORS configuration applied with allowed origins: {}", allowedOrigins);
    }
    
    /**
     * Configures path matching settings for URL handling.
     * This determines how Spring MVC matches request paths to controllers.
     * 
     * @param configurer the path match configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Configure URL path matching options
        configurer.setUseSuffixPatternMatch(useSuffixPatternMatch);
        configurer.setUseRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch);
        
        // URL matching preferences for trailing slashes
        // Treat URLs with and without trailing slash as distinct
        // This helps prevent duplicate content issues for SEO
        configurer.setUseTrailingSlashMatch(false);
        
        logger.info("Path matching configured - suffix pattern match: {}, registered suffix pattern match: {}, trailing slash match: false",
                useSuffixPatternMatch, useRegisteredSuffixPatternMatch);
        
        // TODO: Add custom path matchers for handling special URL formats if needed
    }
}