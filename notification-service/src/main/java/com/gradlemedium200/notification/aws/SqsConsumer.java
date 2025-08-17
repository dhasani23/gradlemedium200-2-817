package com.gradlemedium200.notification.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradlemedium200.notification.model.Notification;
import com.gradlemedium200.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AWS SQS integration service for consuming messages from SQS queues for retry
 * and dead letter handling with automatic message processing and error handling.
 */
@Service
public class SqsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SqsConsumer.class);
    private static final String APPROXIMATE_NUMBER_OF_MESSAGES = "ApproximateNumberOfMessages";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AmazonSQS amazonSQS;
    private final NotificationService notificationService;

    @Value("${aws.sqs.notification.retry-queue}")
    private String queueUrl;

    @Value("${aws.sqs.notification.dead-letter-queue}")
    private String deadLetterQueueUrl;

    @Value("${aws.sqs.notification.max-receive-count:10}")
    private int maxReceiveCount;

    @Value("${aws.sqs.notification.visibility-timeout-seconds:30}")
    private int visibilityTimeoutSeconds;

    private final AtomicBoolean isProcessingEnabled = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;

    /**
     * Constructor for SqsConsumer
     *
     * @param amazonSQS           AWS SQS client
     * @param notificationService Notification service to handle received messages
     */
    @Autowired
    public SqsConsumer(AmazonSQS amazonSQS, NotificationService notificationService) {
        this.amazonSQS = amazonSQS;
        this.notificationService = notificationService;
    }

    /**
     * Initialize the consumer after bean creation
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing SQS consumer for queue: {}", queueUrl);
        executorService = Executors.newScheduledThreadPool(1);
        
        // TODO: Make this configurable via application properties
        // Auto-start processing on initialization
        startMessageProcessing();
    }

    /**
     * Clean up resources before bean destruction
     */
    @PreDestroy
    public void cleanup() {
        stopMessageProcessing();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.error("ExecutorService did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Starts the message processing loop
     */
    public void startMessageProcessing() {
        if (isProcessingEnabled.compareAndSet(false, true)) {
            logger.info("Starting SQS message processing for queue: {}", queueUrl);
            
            // Schedule message processing task to run periodically
            executorService.scheduleWithFixedDelay(
                    this::processMessages,
                    0,
                    5,  // FIXME: Make this configurable
                    TimeUnit.SECONDS
            );
        } else {
            logger.warn("Message processing is already running");
        }
    }

    /**
     * Stops the message processing loop
     */
    public void stopMessageProcessing() {
        if (isProcessingEnabled.compareAndSet(true, false)) {
            logger.info("Stopping SQS message processing");
        } else {
            logger.warn("Message processing is already stopped");
        }
    }

    /**
     * Processes messages from the SQS queue
     */
    @Async
    public void processMessages() {
        if (!isProcessingEnabled.get()) {
            return;
        }

        try {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(maxReceiveCount)
                    .withVisibilityTimeout(visibilityTimeoutSeconds)
                    .withWaitTimeSeconds(5)  // Enable long polling
                    .withMessageAttributeNames("All")
                    .withAttributeNames("All");

            ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(receiveMessageRequest);
            List<Message> messages = receiveMessageResult.getMessages();

            if (messages.isEmpty()) {
                logger.debug("No messages available in queue: {}", queueUrl);
                return;
            }

            logger.info("Received {} messages from SQS queue", messages.size());

            for (Message message : messages) {
                try {
                    boolean processedSuccessfully = handleMessage(message);

                    if (processedSuccessfully) {
                        deleteMessage(message.getReceiptHandle());
                    } else {
                        // Message will return to queue after visibility timeout expires
                        logger.warn("Failed to process message: {}, returning to queue after visibility timeout", 
                                message.getMessageId());
                    }
                } catch (Exception e) {
                    logger.error("Error processing message {}: {}", message.getMessageId(), e.getMessage(), e);
                    sendToDeadLetterQueue(message, e.getMessage());
                    // Delete from the original queue since we've moved it to the DLQ
                    deleteMessage(message.getReceiptHandle());
                }
            }
        } catch (Exception e) {
            logger.error("Error during SQS message processing: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles a single SQS message and returns success status
     *
     * @param message The SQS message to handle
     * @return True if message was processed successfully, false otherwise
     */
    public boolean handleMessage(Message message) {
        logger.debug("Processing message: {}", message.getMessageId());

        try {
            Optional<Notification> notification = parseNotificationFromMessage(message);
            
            if (!notification.isPresent()) {
                logger.warn("Could not parse notification from message: {}", message.getMessageId());
                return false;
            }
            
            // Process the notification using the notification service
            boolean success = notificationService.sendNotification(notification.get());
            
            if (success) {
                logger.info("Successfully processed notification: {}", notification.get().getId());
            } else {
                logger.warn("Failed to process notification: {}", notification.get().getId());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error handling message {}: {}", message.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Parses notification object from SQS message body
     *
     * @param message The SQS message
     * @return Optional containing the parsed Notification, or empty if parsing fails
     */
    public Optional<Notification> parseNotificationFromMessage(Message message) {
        try {
            // Try to deserialize the message body to a Notification object
            Notification notification = objectMapper.readValue(message.getBody(), Notification.class);
            return Optional.of(notification);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse notification from message {}: {}", 
                    message.getMessageId(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Deletes a processed message from the queue
     *
     * @param receiptHandle Receipt handle of the message to delete
     */
    public void deleteMessage(String receiptHandle) {
        try {
            amazonSQS.deleteMessage(new DeleteMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withReceiptHandle(receiptHandle));
            logger.debug("Successfully deleted message with receipt handle: {}", receiptHandle);
        } catch (Exception e) {
            logger.error("Error deleting message: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends a failed message to the dead letter queue
     *
     * @param message The original SQS message
     * @param error   Error description
     */
    public void sendToDeadLetterQueue(Message message, String error) {
        try {
            // Create a map of message attributes including the error information
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            
            // Include the original message attributes if any
            if (message.getMessageAttributes() != null) {
                messageAttributes.putAll(message.getMessageAttributes());
            }
            
            // Add error information
            messageAttributes.put("Error", new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(error));
            
            // Add original message ID for tracing
            messageAttributes.put("OriginalMessageId", new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(message.getMessageId()));
            
            // Send to the dead letter queue
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(deadLetterQueueUrl)
                    .withMessageBody(message.getBody())
                    .withMessageAttributes(messageAttributes);
            
            amazonSQS.sendMessage(sendMessageRequest);
            
            logger.info("Message {} moved to dead letter queue due to error: {}", 
                    message.getMessageId(), error);
        } catch (Exception e) {
            logger.error("Error sending message to dead letter queue: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets attributes of the specified queue
     *
     * @param queueUrl URL of the SQS queue
     * @return Map of queue attributes
     */
    public Map<String, String> getQueueAttributes(String queueUrl) {
        try {
            GetQueueAttributesRequest request = new GetQueueAttributesRequest()
                    .withQueueUrl(queueUrl)
                    .withAttributeNames(Arrays.asList("All"));
            
            GetQueueAttributesResult result = amazonSQS.getQueueAttributes(request);
            return result.getAttributes();
        } catch (Exception e) {
            logger.error("Error getting queue attributes for {}: {}", queueUrl, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Gets approximate number of messages in the queue
     *
     * @return Approximate message count
     */
    public int getApproximateMessageCount() {
        try {
            Map<String, String> attributes = getQueueAttributes(queueUrl);
            String count = attributes.get(APPROXIMATE_NUMBER_OF_MESSAGES);
            return count != null ? Integer.parseInt(count) : 0;
        } catch (Exception e) {
            logger.error("Error getting approximate message count: {}", e.getMessage(), e);
            return 0;
        }
    }
}