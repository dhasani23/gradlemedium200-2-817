package com.gradlemedium200.interceptor;

import com.gradlemedium200.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;

/**
 * HTTP response interceptor for logging and monitoring outgoing responses.
 * This interceptor captures response information, performs logging based on configuration,
 * and records response metrics for monitoring purposes.
 * 
 * @author gradlemedium200
 */
@Component
public class ResponseInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseInterceptor.class);
    
    private final MetricsService metricsService;
    
    /**
     * Flag to enable detailed response body logging.
     * When set to true, the interceptor will attempt to log response body content.
     */
    private boolean logResponseBody = false;
    
    /**
     * Request attribute name for storing the request ID
     */
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";
    
    /**
     * Request attribute name for storing the request start time
     */
    private static final String REQUEST_START_TIME = "requestStartTime";
    
    /**
     * Constructs a new ResponseInterceptor with the specified metrics service.
     * 
     * @param metricsService service for recording response metrics
     */
    @Autowired
    public ResponseInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.info("ResponseInterceptor initialized");
    }
    
    /**
     * Post-processes responses after controller handling but before view rendering.
     * This method captures response information for later processing and metrics recording.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler object that processed the request
     * @param modelAndView the ModelAndView that will be rendered
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        
        // Log response status at INFO level
        logger.info("Response prepared: [{}] {} {} - Status: {}", 
                requestId, request.getMethod(), request.getRequestURI(), response.getStatus());
        
        // If the response status is an error (4xx or 5xx), log at WARN or ERROR level
        if (response.getStatus() >= 400 && response.getStatus() < 500) {
            logger.warn("Client error response: [{}] Status {} for {} {}", 
                    requestId, response.getStatus(), request.getMethod(), request.getRequestURI());
        } else if (response.getStatus() >= 500) {
            logger.error("Server error response: [{}] Status {} for {} {}", 
                    requestId, response.getStatus(), request.getMethod(), request.getRequestURI());
        }
        
        // Log detailed response information if configured
        if (logResponseBody) {
            logResponseDetails(response);
        }
        
        // Record metrics about the response status
        metricsService.recordCustomMetric("response.status." + response.getStatus(), 1);
    }
    
    /**
     * Executes after the complete request has been processed, including view rendering.
     * This method calculates final request duration and records all response metrics.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler object that processed the request
     * @param ex any exception that occurred during processing (may be null)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Record response time for this request
            recordResponseTime(requestId, duration);
            
            // Log completion with additional details
            if (ex == null) {
                logger.debug("Response completed: [{}] {} {} - {} ms - Status: {}", 
                        requestId, request.getMethod(), request.getRequestURI(), 
                        duration, response.getStatus());
            } else {
                // If an exception occurred during processing
                logger.error("Response completed with exception: [{}] {} {} - {} ms - Status: {} - Error: {}", 
                        requestId, request.getMethod(), request.getRequestURI(), 
                        duration, response.getStatus(), ex.getMessage(), ex);
                
                // Track error metrics
                metricsService.recordCustomMetric("response.error", 1);
                String errorType = ex.getClass().getSimpleName();
                metricsService.recordCustomMetric("response.error." + errorType, 1);
            }
            
            // Track response status code metrics
            String statusCategory = getStatusCategory(response.getStatus());
            metricsService.recordCustomMetric("response." + statusCategory, 1);
        } else {
            logger.warn("Unable to calculate response time for [{}] - start time not found", requestId);
        }
    }
    
    /**
     * Logs detailed response information including headers and potentially body content.
     * 
     * @param response the HTTP response to log details for
     */
    public void logResponseDetails(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n========== Response Details ==========\n");
        sb.append("Status: ").append(response.getStatus()).append("\n");
        
        // Log headers
        sb.append("--- Headers ---\n");
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            sb.append(headerName).append(": ").append(response.getHeader(headerName)).append("\n");
        }
        
        // Note: Logging the actual response body is challenging with standard interceptors
        // as the body has likely already been written to the output stream
        sb.append("--- Body ---\n");
        sb.append("[Response body logging requires custom filter implementation]\n");
        
        sb.append("====================================");
        
        logger.debug(sb.toString());
    }
    
    /**
     * Records response time for metrics collection and analysis.
     * 
     * @param requestId the unique identifier for the request
     * @param duration the response time in milliseconds
     */
    public void recordResponseTime(String requestId, long duration) {
        // Track response time as a custom metric
        metricsService.recordCustomMetric("response.time.last", duration);
        
        // Log the timing information
        if (duration > 5000) {
            // Log slow responses as warnings
            logger.warn("Slow response detected: [{}] - {} ms", requestId, duration);
        }
        
        // TODO: Implement percentile tracking for response times
        // This would require more sophisticated statistics in the metrics service
        
        logger.debug("Response time recorded: [{}] - {} ms", requestId, duration);
    }
    
    /**
     * Gets the category of the HTTP status code (1xx, 2xx, etc.).
     * 
     * @param status the HTTP status code
     * @return a string representation of the status category
     */
    private String getStatusCategory(int status) {
        if (status >= 100 && status < 200) return "1xx";
        if (status >= 200 && status < 300) return "2xx";
        if (status >= 300 && status < 400) return "3xx";
        if (status >= 400 && status < 500) return "4xx";
        if (status >= 500) return "5xx";
        return "unknown";
    }
    
    /**
     * Sets whether response body logging is enabled.
     * 
     * @param logResponseBody true to enable response body logging, false otherwise
     */
    public void setLogResponseBody(boolean logResponseBody) {
        this.logResponseBody = logResponseBody;
        logger.info("Response body logging {}", logResponseBody ? "enabled" : "disabled");
    }
    
    /**
     * Gets whether response body logging is enabled.
     * 
     * @return true if response body logging is enabled, false otherwise
     */
    public boolean isLogResponseBody() {
        return logResponseBody;
    }
}