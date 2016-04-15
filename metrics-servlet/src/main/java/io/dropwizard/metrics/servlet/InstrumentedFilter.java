package io.dropwizard.metrics.servlet;


/**
 * Implementation of the {@link AbstractInstrumentedFilter} which provides a default set of response codes
 * to capture information about. <p>Use it in your servlet.xml like this:</p>
 * <pre>{@code
 * <filter>
 *     <filter-name>instrumentedFilter</filter-name>
 *     <filter-class>io.dropwizard.metrics.servlet.InstrumentedFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *     <filter-name>instrumentedFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }</pre>
 */
public class InstrumentedFilter extends AbstractInstrumentedFilter {
    public static final String REGISTRY_ATTRIBUTE = InstrumentedFilter.class.getName() + ".registry";

    /**
     * Creates a new instance of the filter.
     */
    public InstrumentedFilter() {
        super(REGISTRY_ATTRIBUTE, createByStatusCode(), NAME_PREFIX + "other");
    }
}
