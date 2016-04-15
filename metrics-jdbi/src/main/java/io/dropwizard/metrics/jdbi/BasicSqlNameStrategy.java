package io.dropwizard.metrics.jdbi;

import java.util.Arrays;

import io.dropwizard.metrics.jdbi.strategies.DelegatingStatementNameStrategy;
import io.dropwizard.metrics.jdbi.strategies.NameStrategies;
import io.dropwizard.metrics.jdbi.strategies.StatementNameStrategy;

public class BasicSqlNameStrategy extends DelegatingStatementNameStrategy {
    public BasicSqlNameStrategy() {
        super(NameStrategies.CHECK_EMPTY,
              NameStrategies.SQL_OBJECT);
    }

	protected void registerStrategies(StatementNameStrategy... strategies) {
	    this.strategies.addAll(Arrays.asList(strategies));
	}
}
