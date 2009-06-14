package org.multiverse.benchmarkframework.executor;

import java.util.List;

/**
 * A {@link Benchmark} contains a List of testCases.
 *
 * @author Peter Veentjer.
 */
public interface Benchmark {

    /**
     * Returns an Iterator containing all the TestCases that should be executed
     * for this Benchmark.
     *
     * @return Iterator containing the testcases to execute.
     */
    List<TestCase> testCases();

}
