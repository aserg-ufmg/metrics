package io.dropwizard.metrics.servlet;

import static io.dropwizard.metrics.MetricRegistry.name;
import io.dropwizard.metrics.CounterMetric;
import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Timer;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link Filter} implementation which captures request information and a breakdown of the response
 * codes being returned.
 */
public abstract class AbstractInstrumentedFilter implements Filter {
    static final String METRIC_PREFIX = "name-prefix";
	static final int OK = 200;
	static final int CREATED = 201;
	static final int NO_CONTENT = 204;
	static final int BAD_REQUEST = 400;
	static final int NOT_FOUND = 404;
	protected static final String NAME_PREFIX = "responseCodes.";
	static final int SERVER_ERROR = 500;

    protected static Map<Integer, String> createByStatusCode() {
	    final Map<Integer, String> meterNamesByStatusCode = new HashMap<Integer, String>(6);
	    meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
	    meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
	    meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
	    meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
	    meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
	    meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
	    return meterNamesByStatusCode;
	}

	private final String otherMetricName;
    private final Map<Integer, String> meterNamesByStatusCode;
    private final String registryAttribute;

    // initialized after call of init method
    private ConcurrentMap<Integer, Meter> metersByStatusCode;
    private Meter otherMeter;
    private Meter timeoutsMeter;
    private Meter errorsMeter;
    private CounterMetric activeRequests;
    private Timer requestTimer;


    /**
     * Creates a new instance of the filter.
     *
     * @param registryAttribute      the attribute used to look up the metrics registry in the
     *                               servlet context
     * @param meterNamesByStatusCode A map, keyed by status code, of meter names that we are
     *                               interested in.
     * @param otherMetricName        The name used for the catch-all meter.
     */
    protected AbstractInstrumentedFilter(String registryAttribute,
                                         Map<Integer, String> meterNamesByStatusCode,
                                         String otherMetricName) {
        this.registryAttribute = registryAttribute;
        this.otherMetricName = otherMetricName;
        this.meterNamesByStatusCode = meterNamesByStatusCode;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final MetricRegistry metricsRegistry = getMetricsFactory(filterConfig);

        String metricName = filterConfig.getInitParameter(METRIC_PREFIX);
        if(metricName == null || metricName.isEmpty()) {
            metricName = getClass().getName();
        }

        this.metersByStatusCode = new ConcurrentHashMap<Integer, Meter>(meterNamesByStatusCode
                .size());
        for (Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(),
                    metricsRegistry.meter(name(metricName, entry.getValue())));
        }
        this.otherMeter = metricsRegistry.meter(name(metricName,
                                                     otherMetricName));
        this.timeoutsMeter = metricsRegistry.meter(name(metricName,
                                                        "timeouts"));
        this.errorsMeter = metricsRegistry.meter(name(metricName,
                                                      "errors"));
        this.activeRequests = metricsRegistry.counter(name(metricName,
                                                           "activeRequests"));
        this.requestTimer = metricsRegistry.timer(name(metricName,
                                                       "requests"));

    }

    private MetricRegistry getMetricsFactory(FilterConfig filterConfig) {
        final MetricRegistry metricsRegistry;

        final Object o = filterConfig.getServletContext().getAttribute(this.registryAttribute);
        if (o instanceof MetricRegistry) {
            metricsRegistry = (MetricRegistry) o;
        } else {
            metricsRegistry = new MetricRegistry();
        }
        return metricsRegistry;
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final StatusExposingServletResponse wrappedResponse =
                new StatusExposingServletResponse((HttpServletResponse) response);
        activeRequests.inc();
        final Timer.Context context = requestTimer.time();
        boolean error = false;
        try {
            chain.doFilter(request, wrappedResponse);
        } catch (IOException e) {
            error = true;
            throw e;
        } catch (ServletException e) {
            error = true;
            throw e;
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {
            if (!error && request.isAsyncStarted()) {
                request.getAsyncContext().addListener(new AsyncResultListener(context));
            } else {
                context.stop();
                activeRequests.dec();
                if (error) {
                    errorsMeter.mark();
                } else {
                    markMeterForStatusCode(wrappedResponse.getStatus());
                }
            }
        }
    }

    private void markMeterForStatusCode(int status) {
        final Meter metric = metersByStatusCode.get(status);
        if (metric != null) {
            metric.mark();
        } else {
            otherMeter.mark();
        }
    }

    private static class StatusExposingServletResponse extends HttpServletResponseWrapper {
        // The Servlet spec says: calling setStatus is optional, if no status is set, the default is 200.
        private int httpStatus = 200;

        public StatusExposingServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void setStatus(int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            httpStatus = sc;
            super.setStatus(sc, sm);
        }

        public int getStatus() {
            return httpStatus;
        }
    }

    private class AsyncResultListener implements AsyncListener {
        private Timer.Context context;
        private boolean done = false;

        public AsyncResultListener(Timer.Context context) {
            this.context = context;
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            if (!done) {
                HttpServletResponse suppliedResponse = (HttpServletResponse) event.getSuppliedResponse();
                context.stop();
                activeRequests.dec();
                markMeterForStatusCode(suppliedResponse.getStatus());
            }
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            context.stop();
            activeRequests.dec();
            timeoutsMeter.mark();
            done = true;
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            context.stop();
            activeRequests.dec();
            errorsMeter.mark();
            done = true;
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {

        }
    }
}
