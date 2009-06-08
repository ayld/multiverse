package org.multiverse.benchmark;

import java.util.Iterator;

public class BenchmarkRunner {

    private ResultRepository resultRepository;

    public BenchmarkRunner(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public void execute(Benchmark benchmark) {
        benchmark.getDriver().setUp();

        for (Iterator<TestCase> it = benchmark.testCases(); it.hasNext();) {
            doRun(benchmark.getDriver(), it.next());
        }

        benchmark.getDriver().tearDown();
    }

    private void doRun(Driver driver, TestCase testCase) {
        for (int k = 0; k < testCase.getWarmupRunCount(); k++) {
            driver.initRun(testCase);
            driver.run(new TestResult(testCase));
        }

        for (int k = 0; k < testCase.getRunCount(); k++) {
            driver.initRun(testCase);
            TestResult result = new TestResult(testCase);
            long startMs = System.currentTimeMillis();
            long startNs = System.nanoTime();
            driver.run(result);
            long endNs = System.nanoTime();
            long endMs = System.currentTimeMillis();
            long durationNs = endNs - startNs;
                        
            result.put("durationNs", durationNs);
            result.put("startMs", startMs);
            result.put("endMs", endMs);
            result.setX(k+1);

            resultRepository.save(result);
        }
    }
}
