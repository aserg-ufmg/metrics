package io.dropwizard.metrics;

import java.util.concurrent.ThreadLocalRandom;

public class ReservoirNextLong {
	static final int BITS_PER_LONG = 63;
	/**
	 * Get a pseudo-random long uniformly between 0 and n-1. Stolen from
	 * {@link java.util.Random#nextInt()}.
	 *
	 * @param n the bound
	 * @return a value select randomly from the range {@code [0..n)}.
	 */
	protected static long nextLong(long n) {
	    long bits, val;
	    do {
	        bits = ThreadLocalRandom.current().nextLong() & (~(1L << BITS_PER_LONG));
	        val = bits % n;
	    } while (bits - val + (n - 1) < 0L);
	    return val;
	}

	public ReservoirNextLong() {
		super();
	}

}