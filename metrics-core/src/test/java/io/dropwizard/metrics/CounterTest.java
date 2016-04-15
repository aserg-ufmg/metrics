package io.dropwizard.metrics;

import org.junit.Test;

import io.dropwizard.metrics.CounterMetric;

import static org.assertj.core.api.Assertions.assertThat;

public class CounterTest {
    private final CounterMetric counter = new CounterMetric();

    @Test
    public void startsAtZero() throws Exception {
        assertThat(counter.getCount())
                .isZero();
    }

    @Test
    public void incrementsByOne() throws Exception {
        counter.inc();

        assertThat(counter.getCount())
                .isEqualTo(1);
    }

    @Test
    public void incrementsByAnArbitraryDelta() throws Exception {
        counter.inc(12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }

    @Test
    public void decrementsByOne() throws Exception {
        counter.dec();

        assertThat(counter.getCount())
                .isEqualTo(-1);
    }

    @Test
    public void decrementsByAnArbitraryDelta() throws Exception {
        counter.dec(12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }
}
