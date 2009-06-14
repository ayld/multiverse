package org.multiverse.benchmarkframework.executor;

import org.multiverse.benchmarkframework.TestCaseResult;

/**
 * Contains the unparametrized algorithm you want to benchmark. All the variable parameters
 * are stored in the {@link TestCase}.
 * <p/>
 * It looks a lot like a basic unit testcase with the familiar lifecyle methods. For a more
 * convenient implementation to extend from, look at the {@link AbstractDriver}.
 *
 * @author Peter Veentjer.
 */
public interface Driver {

    void preRun(TestCase testCase);

    void run();

    void postRun(TestCaseResult caseResult);
}
