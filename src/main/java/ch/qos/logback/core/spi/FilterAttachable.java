package ch.qos.logback.core.spi;

/**
 * Interface for objects that can have filters attached to them.
 * @param <E> The type of events that can be filtered
 */
public interface FilterAttachable<E> {
    /**
     * Add a filter to the filter chain.
     * @param filter the filter to add
     */
    void addFilter(Object filter);

    /**
     * Clear all filters.
     */
    void clearAllFilters();

    /**
     * Get the first filter in the filter chain.
     * @return the first filter
     */
    Object getFirstFilter();

    /**
     * Get a copy of the list of attached filters.
     */
    void getCopyOfAttachedFiltersList();
}