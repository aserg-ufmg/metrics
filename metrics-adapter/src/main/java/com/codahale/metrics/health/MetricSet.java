package com.codahale.metrics.health;

import java.util.Map;

import com.codahale.metrics.Metric;

@Deprecated
public interface MetricSet {
	Map<String, Metric> getMetrics();
}
