package io.dropwizard.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughputs.
 *
 * @see EWMA
 */
public class Meter implements Metered {
    static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    final EWMA m1Rate = EWMA.oneMinuteEWMA();
    final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final LongAdder count = LongAdderFactory.create();
    private final long startTime;
    final AtomicLong lastTick;
    private final Clock clock;

    /**
     * Creates a new {@link Meter}.
     */
    public Meter() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock      the clock to use for the meter ticks
     */
    public Meter(Clock clock) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        clock.tickIfNecessary(this);
        count.add(n);
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    @Override
    public long getCount() {
        return count.sum();
    }

    @Override
    public double getFifteenMinuteRate() {
        clock.tickIfNecessary(this);
        return m15Rate.getRate(TimeUnit.SECONDS);
    }

    @Override
    public double getFiveMinuteRate() {
        clock.tickIfNecessary(this);
        return m5Rate.getRate(TimeUnit.SECONDS);
    }

    @Override
    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final double elapsed = (clock.getTick() - startTime);
            return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
        }
    }

    @Override
    public double getOneMinuteRate() {
        clock.tickIfNecessary(this);
        return m1Rate.getRate(TimeUnit.SECONDS);
    }

	void printMeter(ConsolePrinter consolePrinter) {
	    consolePrinter.output.printf(consolePrinter.locale, "             count = %d%n", getCount());
	    consolePrinter.output.printf(consolePrinter.locale, "         mean rate = %2.2f events/%s%n", consolePrinter.convertRate(getMeanRate()), consolePrinter.getRateUnit());
	    consolePrinter.output.printf(consolePrinter.locale, "     1-minute rate = %2.2f events/%s%n", consolePrinter.convertRate(getOneMinuteRate()), consolePrinter.getRateUnit());
	    consolePrinter.output.printf(consolePrinter.locale, "     5-minute rate = %2.2f events/%s%n", consolePrinter.convertRate(getFiveMinuteRate()), consolePrinter.getRateUnit());
	    consolePrinter.output.printf(consolePrinter.locale, "    15-minute rate = %2.2f events/%s%n", consolePrinter.convertRate(getFifteenMinuteRate()), consolePrinter.getRateUnit());
	}
}
