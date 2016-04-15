package com.codahale.metrics;

import io.dropwizard.metrics.Counting;

@Deprecated
public class Counter implements Metric, Counting {
	final io.dropwizard.metrics.CounterMetric counter;

	public Counter(io.dropwizard.metrics.CounterMetric counter) {
		this.counter = counter;
	}
	public Counter() {
		this.counter = new io.dropwizard.metrics.CounterMetric();
	}

	public void inc() {
		counter.inc(1);
	}

	public void inc(long n) {
		counter.inc(n);
	}

	public void dec() {
		counter.dec(1);
	}

	public void dec(long n) {
		counter.dec(-n);
	}

	@Override
	public long getCount() {
		return counter.getCount();
	}
}
