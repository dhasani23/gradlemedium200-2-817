package com.gradlemedium200.productcatalog.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converter class for marshalling LocalDateTime objects to/from DynamoDB.
 * This class converts between LocalDateTime and String formats for DynamoDB storage.
 */
public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convert(LocalDateTime time) {
        return time == null ? null : time.format(FORMATTER);
    }

    @Override
    public LocalDateTime unconvert(String value) {
        return value == null ? null : LocalDateTime.parse(value, FORMATTER);
    }
}