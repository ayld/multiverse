package org.multiverse.benchmark;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StackStressTestBenchmark implements Benchmark {

    @Override
    public Iterator<TestCase> testCases() {
        List<TestCase> cases = new LinkedList<TestCase>();

        for (int k = 0; k < 10; k++) {
            for (int l = 0; l < 10; l+=2) {
                TestCase testCase = new TestCase();
                testCase.setDescription("StackPerformance");
                testCase.setWarmupRunCount(1);
                testCase.setRunCount(3);
                testCase.setProperty("itemcount", k * 100000);
                testCase.setProperty("producercount",l);
                cases.add(testCase);
            }
        }

        return cases.iterator();
    }

    @Override
    public Driver getDriver() {
        return new StackStressTestDriver();
    }
}
