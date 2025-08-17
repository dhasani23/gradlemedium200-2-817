package com.gradlemedium200.common.util;

import java.util.*;

/**
 * Utility class for collection operations and manipulations.
 * Provides helper methods for common collection tasks such as checking emptiness,
 * calculating size safely, partitioning lists, and set operations.
 */
public final class CollectionUtil {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private CollectionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Checks if a collection is null or empty.
     *
     * @param collection the collection to check
     * @return true if the collection is null or empty, false otherwise
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a collection is not null and not empty.
     *
     * @param collection the collection to check
     * @return true if the collection is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Returns the size of a collection, handling null safely.
     * Returns 0 if the collection is null.
     *
     * @param collection the collection to get the size from
     * @return the size of the collection or 0 if the collection is null
     */
    public static int safeSize(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Partitions a list into sublists of the specified size.
     * The last partition may be smaller than the specified size if the list
     * size is not a multiple of the partition size.
     *
     * @param <T> the type of elements in the list
     * @param list the list to partition
     * @param size the size of each partition
     * @return a list of sublists
     * @throws IllegalArgumentException if the partition size is less than or equal to zero
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be greater than 0");
        }
        
        if (isEmpty(list)) {
            return Collections.emptyList();
        }
        
        List<List<T>> result = new ArrayList<>();
        
        // Calculate the number of partitions needed
        int totalSize = list.size();
        int numOfPartitions = (totalSize + size - 1) / size; // Ceiling division
        
        for (int i = 0; i < numOfPartitions; i++) {
            int fromIndex = i * size;
            int toIndex = Math.min(fromIndex + size, totalSize);
            
            // FIXME: Consider optimizing for large lists to avoid copying
            result.add(new ArrayList<>(list.subList(fromIndex, toIndex)));
        }
        
        return result;
    }

    /**
     * Returns the intersection of two sets.
     * The returned set contains all elements that are contained in both source sets.
     *
     * @param <T> the type of elements in the sets
     * @param set1 the first set
     * @param set2 the second set
     * @return a new set containing the intersection
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        if (isEmpty(set1) || isEmpty(set2)) {
            return Collections.emptySet();
        }
        
        // For efficiency, iterate over the smaller set
        Set<T> smallerSet = set1.size() <= set2.size() ? set1 : set2;
        Set<T> largerSet = set1.size() > set2.size() ? set1 : set2;
        
        Set<T> result = new HashSet<>();
        for (T element : smallerSet) {
            if (largerSet.contains(element)) {
                result.add(element);
            }
        }
        
        return result;
    }

    /**
     * Returns the union of two sets.
     * The returned set contains all elements that are contained in either set.
     *
     * @param <T> the type of elements in the sets
     * @param set1 the first set
     * @param set2 the second set
     * @return a new set containing the union
     */
    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        if (isEmpty(set1) && isEmpty(set2)) {
            return Collections.emptySet();
        }
        
        if (isEmpty(set1)) {
            return new HashSet<>(set2);
        }
        
        if (isEmpty(set2)) {
            return new HashSet<>(set1);
        }
        
        Set<T> result = new HashSet<>(set1);
        result.addAll(set2);
        
        return result;
    }

    // TODO: Add additional collection utility methods like difference, symmetric difference
    // TODO: Consider adding methods for Map operations
    // TODO: Add methods for finding duplicates in collections
}