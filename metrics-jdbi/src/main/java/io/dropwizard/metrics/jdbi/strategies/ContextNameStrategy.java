package io.dropwizard.metrics.jdbi.strategies;

import java.util.Arrays;


/**
 * Adds statistics for JDBI queries that set the {@link NameStrategies#STATEMENT_GROUP} and {@link
 * NameStrategies#STATEMENT_NAME} for group based display.
 */
public class ContextNameStrategy extends DelegatingStatementNameStrategy {
    public ContextNameStrategy() {
        super(NameStrategies.CHECK_EMPTY,
              NameStrategies.CHECK_RAW,
              NameStrategies.CONTEXT_NAME,
              NameStrategies.NAIVE_NAME);
    }

	protected void registerStrategies(StatementNameStrategy... strategies) {
	    this.strategies.addAll(Arrays.asList(strategies));
	}
}
