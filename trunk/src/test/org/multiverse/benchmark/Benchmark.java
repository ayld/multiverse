package org.multiverse.benchmark;

import java.util.Iterator;

public interface Benchmark {

    Iterator<TestCase> testCases();

    Driver getDriver();
}
