package io.dropwizard.metrics.jdbi.strategies;

import java.util.Arrays;

/**
 * Very simple strategy, can be used with any JDBI loader to build basic statistics.
 */
public class NaiveNameStrategy extends DelegatingStatementNameStrategy {
    public NaiveNameStrategy() {
        super(NameStrategies.CHECK_EMPTY,
              NameStrategies.CHECK_RAW,
              NameStrategies.NAIVE_NAME);
    }

	protected void registerStrategies(StatementNameStrategy... strategies) {
	    this.strategies.addAll(Arrays.asList(strategies));
	}
}
