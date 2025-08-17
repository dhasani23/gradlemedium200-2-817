package com.gradlemedium200.interceptor;

import com.gradlemedium200.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.UUID;

/**
 * HTTP request interceptor that provides logging and monitoring for incoming requests.
 * This interceptor tracks request metrics, generates unique request IDs, and 
 * provides detailed logging of request information when configured.
 * 
 * @author gradlemedium200
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);
    
    private final MetricsService metricsService;
    
    /**
     * Flag to enable detailed request logging. When set to true, the interceptor will
     * log detailed information about incoming requests including headers and parameters.
     */
    private boolean enableDetailedLogging = false;
    
    /**
     * Request attribute name for storing the request start time
     */
    private static final String REQUEST_START_TIME = "requestStartTime";
    
    /**
     * Request attribute name for storing the request ID
     */
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";
    
    /**
     * Header name for the request ID in the response
     */
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    /**
     * Constructs a new RequestInterceptor with the specified metrics service.
     * 
     * @param metricsService service for recording request metrics
     */
    @Autowired
    public RequestInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.info("RequestInterceptor initialized");
    }
    
    /**
     * Pre-processes incoming requests before they are handled by controllers.
     * This method generates a request ID, records request start time, and 
     * logs request details based on configuration.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler object that will process the request
     * @return true if the request should be processed, false otherwise
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate unique ID for this request
        String requestId = generateRequestId();
        
        // Store the request ID and start time as request attributes
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        
        // Set the request ID in the response headers for client traceability
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        // Record the start time of this request
        recordRequestStart(requestId);
        
        // Log detailed information if enabled
        if (enableDetailedLogging) {
            logRequestDetails(request);
        } else {
            // Log basic request information
            logger.info("Request received: [{}] {} {}", requestId, request.getMethod(), request.getRequestURI());
        }
        
        // Track the request in metrics
        metricsService.incrementRequestCounter(request.getRequestURI(), request.getMethod());
        
        // Continue with request processing
        return true;
    }
    
    /**
     * Post-processes requests after they are handled but before view rendering.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler that processed the request
     * @param modelAndView the ModelAndView object for the rendered view
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Implementation can be added if needed
    }
    
    /**
     * Performs actions after request processing is complete, including view rendering.
     * This method calculates request duration and records metrics.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler that processed the request
     * @param ex any exception thrown during processing
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Get the request ID from the attributes
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        
        // Calculate request duration
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Record the response time for this endpoint
            metricsService.recordResponseTime(request.getRequestURI(), duration);
            
            // Log completion with timing information
            if (ex == null) {
                logger.info("Request completed: [{}] {} {} - {} ms ({})", 
                    requestId, request.getMethod(), request.getRequestURI(), 
                    duration, response.getStatus());
            } else {
                logger.error("Request failed: [{}] {} {} - {} ms ({}) - Error: {}", 
                    requestId, request.getMethod(), request.getRequestURI(), 
                    duration, response.getStatus(), ex.getMessage());
                
                // TODO: Add error metrics tracking
            }
        } else {
            logger.warn("Unable to calculate request duration for [{}] - start time not found", requestId);
        }
    }
    
    /**
     * Logs detailed information about the incoming request.
     * This includes URI, method, client info, headers, and parameters.
     * 
     * @param request the HTTP request to log details for
     */
    public void logRequestDetails(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        
        sb.append("\n========== Request Details [").append(requestId).append("] ==========\n");
        sb.append("URI: ").append(request.getRequestURI()).append("\n");
        sb.append("Method: ").append(request.getMethod()).append("\n");
        sb.append("Client: ").append(request.getRemoteAddr()).append("\n");
        
        // Log headers
        sb.append("--- Headers ---\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip sensitive headers like authorization tokens
            if (!"authorization".equalsIgnoreCase(headerName) && !"cookie".equalsIgnoreCase(headerName)) {
                sb.append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
            } else {
                sb.append(headerName).append(": [REDACTED]\n");
            }
        }
        
        // Log parameters (if not multipart/form-data)
        if (!request.getContentType().contains("multipart/form-data")) {
            sb.append("--- Parameters ---\n");
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                // FIXME: Consider redacting sensitive parameter values
                sb.append(paramName).append(": ").append(request.getParameter(paramName)).append("\n");
            }
        } else {
            sb.append("--- Parameters ---\n");
            sb.append("[Multipart form data - parameters not logged]\n");
        }
        
        sb.append("====================================================");
        
        logger.debug(sb.toString());
    }
    
    /**
     * Generates a unique identifier for the request.
     * 
     * @return a unique request ID string
     */
    public String generateRequestId() {
        // Use UUID to generate a unique request identifier
        // TODO: Consider adding timestamp prefix or custom format for better sorting/readability
        return UUID.randomUUID().toString();
    }
    
    /**
     * Records the start time of the request for later duration calculation.
     * 
     * @param requestId the unique identifier for the request
     */
    public void recordRequestStart(String requestId) {
        // Store the current timestamp for this request
        Long startTime = System.currentTimeMillis();
        
        // TODO: Potentially add this to a request context that's more thread-safe
        
        // Record custom metric for monitoring active requests
        // This implementation is basic - a more sophisticated solution might use AtomicInteger
        // to track concurrent requests
        metricsService.recordCustomMetric("request.started", startTime);
        
        logger.debug("Request start recorded: [{}] at {}", requestId, startTime);
    }
    
    /**
     * Sets whether detailed request logging is enabled.
     * 
     * @param enableDetailedLogging true to enable detailed logging, false otherwise
     */
    public void setEnableDetailedLogging(boolean enableDetailedLogging) {
        this.enableDetailedLogging = enableDetailedLogging;
        logger.info("Detailed request logging {}", enableDetailedLogging ? "enabled" : "disabled");
    }
    
    /**
     * Gets whether detailed request logging is enabled.
     * 
     * @return true if detailed logging is enabled, false otherwise
     */
    public boolean isEnableDetailedLogging() {
        return enableDetailedLogging;
    }
}