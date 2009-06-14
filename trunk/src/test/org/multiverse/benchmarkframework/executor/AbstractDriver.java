package org.multiverse.benchmarkframework.executor;

import org.multiverse.benchmarkframework.TestCaseResult;

/**
 * A convenience {@link Driver} implementation that doesn't force one to implement optional
 * methods. Only the {@link Driver#run()} method needs to be implemented.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractDriver implements Driver{

    @Override
    public void preRun(TestCase testCase) {
    }

    @Override
    public void postRun(TestCaseResult caseResult) {
    }
}
