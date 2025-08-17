package com.gradlemedium200.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for date and time operations, formatting, and conversion.
 * Provides common functionality for working with dates across the application.
 */
public class DateTimeUtil {

    /** Default date format - yyyy-MM-dd */
    public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /** Default date-time format - yyyy-MM-dd HH:mm:ss */
    public static final DateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /** UTC timezone instance */
    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    // Initialize time zones for the date formatters
    static {
        DEFAULT_DATE_FORMAT.setLenient(false);
        DEFAULT_DATETIME_FORMAT.setLenient(false);
    }

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private DateTimeUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }
    
    /**
     * Get current timestamp in milliseconds
     *
     * @return current timestamp as Long value
     */
    public static Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * Format date using specified pattern
     *
     * @param date the date to format
     * @param pattern the format pattern
     * @return formatted date string
     * @throws IllegalArgumentException if the pattern is invalid
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setLenient(false);
            return formatter.format(date);
        } catch (IllegalArgumentException e) {
            // FIXME: Consider using a logger instead of printing to console
            System.err.println("Invalid date pattern: " + pattern);
            throw new IllegalArgumentException("Invalid date pattern: " + pattern, e);
        }
    }
    
    /**
     * Parse date string using specified pattern
     *
     * @param dateString the string to parse
     * @param pattern the format pattern
     * @return parsed Date object
     * @throws IllegalArgumentException if parsing fails or pattern is invalid
     */
    public static Date parseDate(String dateString, String pattern) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setLenient(false);
            return formatter.parse(dateString);
        } catch (ParseException | IllegalArgumentException e) {
            // TODO: Add proper logging here
            throw new IllegalArgumentException("Failed to parse date: " + dateString 
                + " with pattern: " + pattern, e);
        }
    }
    
    /**
     * Add days to a date
     *
     * @param date the base date
     * @param days number of days to add (can be negative)
     * @return new date with days added
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
    
    /**
     * Check if a date is between start and end dates (inclusive)
     *
     * @param date the date to check
     * @param startDate the start date boundary
     * @param endDate the end date boundary
     * @return true if date is between start and end dates (inclusive), false otherwise
     */
    public static boolean isDateBetween(Date date, Date startDate, Date endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        
        // Ensure proper comparison by normalizing time portion if needed
        // FIXME: This comparison may cause issues with timezone differences
        return !date.before(startDate) && !date.after(endDate);
    }
    
    /**
     * Convert a date to UTC timezone
     * 
     * @param date the date to convert
     * @return the date in UTC timezone
     */
    public static Date convertToUTC(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        TimeZone localTimeZone = calendar.getTimeZone();
        int offset = localTimeZone.getOffset(date.getTime());
        
        // Adjust for timezone offset
        Calendar utcCalendar = Calendar.getInstance(UTC_TIMEZONE);
        utcCalendar.setTime(new Date(date.getTime() - offset));
        
        return utcCalendar.getTime();
    }
    
    /**
     * Get start of day for a given date
     * 
     * @param date the input date
     * @return date representing start of day (00:00:00)
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        return calendar.getTime();
    }
    
    /**
     * Get end of day for a given date
     * 
     * @param date the input date
     * @return date representing end of day (23:59:59.999)
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        
        return calendar.getTime();
    }
}