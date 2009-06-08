package org.multiverse.benchmark;

public interface Driver {

    void setUp();

    void tearDown();

    void initRun(TestCase testCase);

    void run(TestResult result);
}
