package com.gradlemedium200.orderservice.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gradlemedium200.orderservice.dto.InventoryCheckDto;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for inventory service integration.
 * Handles communication with the inventory service for checking, reserving,
 * releasing, and updating inventory.
 */
@Component
public class InventoryClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryClient.class);
    
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;
    
    private static final String CHECK_INVENTORY_PATH = "/api/inventory/check";
    private static final String RESERVE_INVENTORY_PATH = "/api/inventory/reserve";
    private static final String RELEASE_INVENTORY_PATH = "/api/inventory/release/{orderId}";
    private static final String UPDATE_INVENTORY_PATH = "/api/inventory/update";

    public InventoryClient(RestTemplate restTemplate, 
                          @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    /**
     * Checks product availability in inventory.
     *
     * @param inventoryCheck DTO containing product and quantity details to check
     * @return true if inventory is available, false otherwise
     */
    public boolean checkInventory(InventoryCheckDto inventoryCheck) {
        logger.info("Checking inventory for product: {} with quantity: {}", 
                inventoryCheck.getProductId(), inventoryCheck.getRequestedQuantity());
        
        try {
            String url = inventoryServiceUrl + CHECK_INVENTORY_PATH;
            
            ResponseEntity<InventoryCheckDto> response = restTemplate.postForEntity(
                    url, inventoryCheck, InventoryCheckDto.class);
            
            if (response.getBody() != null) {
                boolean isAvailable = response.getBody().getIsAvailable();
                logger.info("Product {} availability: {}", inventoryCheck.getProductId(), isAvailable);
                return isAvailable;
            }
            
            logger.warn("Received null response from inventory service check");
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error checking inventory: {} - {}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error checking inventory", e);
            return false;
        }
    }

    /**
     * Reserves inventory for order.
     *
     * @param orderId order ID for which inventory is being reserved
     * @param productId product ID to reserve
     * @param quantity quantity to reserve
     * @return true if reservation was successful, false otherwise
     */
    public boolean reserveInventory(String orderId, String productId, Integer quantity) {
        logger.info("Reserving inventory for order: {}, product: {}, quantity: {}", 
                orderId, productId, quantity);
        
        try {
            String url = inventoryServiceUrl + RESERVE_INVENTORY_PATH;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", orderId);
            requestBody.put("productId", productId);
            requestBody.put("quantity", quantity);
            
            ResponseEntity<Boolean> response = restTemplate.postForEntity(
                    url, requestBody, Boolean.class);
            
            if (response.getBody() != null) {
                boolean reserved = response.getBody();
                logger.info("Inventory reservation for order {} {}", 
                        orderId, reserved ? "successful" : "failed");
                return reserved;
            }
            
            logger.warn("Received null response from inventory service reservation");
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error reserving inventory: {} - {}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error reserving inventory", e);
            return false;
        }
    }

    /**
     * Releases reserved inventory for an order.
     * Typically called when an order is cancelled or failed.
     *
     * @param orderId order ID for which to release inventory
     */
    public void releaseInventory(String orderId) {
        logger.info("Releasing inventory for order: {}", orderId);
        
        try {
            Map<String, String> pathVariables = new HashMap<>();
            pathVariables.put("orderId", orderId);
            
            String url = inventoryServiceUrl + RELEASE_INVENTORY_PATH;
            
            restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    null, 
                    Void.class, 
                    pathVariables);
            
            logger.info("Inventory release request sent for order: {}", orderId);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error releasing inventory: {} - {}", e.getStatusCode(), e.getMessage());
            // Consider retrying or alerting operations team
            // FIXME: Implement retry mechanism for failed inventory releases
        } catch (Exception e) {
            logger.error("Unexpected error releasing inventory", e);
            // TODO: Add dead letter queue for failed inventory releases
        }
    }

    /**
     * Updates inventory after order fulfillment.
     * Typically called when an order has been shipped.
     *
     * @param productId product ID to update
     * @param quantity quantity to update (negative for deduction)
     */
    public void updateInventory(String productId, Integer quantity) {
        logger.info("Updating inventory for product: {}, quantity: {}", productId, quantity);
        
        try {
            String url = inventoryServiceUrl + UPDATE_INVENTORY_PATH;
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("productId", productId)
                .queryParam("quantity", quantity);
            
            HttpEntity<?> entity = HttpEntity.EMPTY;
            
            restTemplate.exchange(
                    builder.toUriString(), 
                    HttpMethod.PUT, 
                    entity, 
                    Void.class);
            
            logger.info("Inventory updated for product: {}", productId);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error updating inventory: {} - {}", e.getStatusCode(), e.getMessage());
            // TODO: Implement compensating transaction for inventory update failures
        } catch (Exception e) {
            logger.error("Unexpected error updating inventory", e);
            // FIXME: Add monitoring for repeated inventory update failures
        }
    }
}