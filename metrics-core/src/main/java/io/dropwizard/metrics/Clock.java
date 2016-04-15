package io.dropwizard.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * An abstraction for how time passes. It is passed to {@link Timer} to track timing.
 */
public abstract class Clock {
    /**
     * Returns the current time tick.
     *
     * @return time tick in nanoseconds
     */
    public abstract long getTick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis();
    }

    long getTick(SlidingTimeWindowReservoir slidingTimeWindowReservoir) {
	    for (; ; ) {
	        final long oldTick = slidingTimeWindowReservoir.lastTick.get();
	        final long tick = getTick() * SlidingTimeWindowReservoir.COLLISION_BUFFER;
	        // ensure the tick is strictly incrementing even if there are duplicate ticks
	        final long newTick = tick - oldTick > 0 ? tick : oldTick + 1;
	        if (slidingTimeWindowReservoir.lastTick.compareAndSet(oldTick, newTick)) {
	            return newTick;
	        }
	    }
	}

	void tickIfNecessary(Meter meter) {
	    final long oldTick = meter.lastTick.get();
	    final long newTick = getTick();
	    final long age = newTick - oldTick;
	    if (age > Meter.TICK_INTERVAL) {
	        final long newIntervalStartTick = newTick - age % Meter.TICK_INTERVAL;
	        if (meter.lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
	            final long requiredTicks = age / Meter.TICK_INTERVAL;
	            for (long i = 0; i < requiredTicks; i++) {
	                meter.m1Rate.tick();
	                meter.m5Rate.tick();
	                meter.m15Rate.tick();
	            }
	        }
	    }
	}

	private static final Clock DEFAULT = new UserTimeClock();

    /**
     * The default clock to use.
     *
     * @return the default {@link Clock} instance
     *
     * @see Clock.UserTimeClock
     */
    public static Clock defaultClock() {
        return DEFAULT;
    }

    /**
     * A clock implementation which returns the current time in epoch nanoseconds.
     */
    public static class UserTimeClock extends Clock {
        @Override
        public long getTick() {
            return System.nanoTime();
        }
    }

    /**
     * A clock implementation which returns the current thread's CPU time.
     */
    public static class CpuTimeClock extends Clock {
        private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

        @Override
        public long getTick() {
            return THREAD_MX_BEAN.getCurrentThreadCpuTime();
        }
    }
}
