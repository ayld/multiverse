package org.multiverse.benchmarkframework.executor;

import org.multiverse.benchmarkframework.BenchmarkResult;
import org.multiverse.benchmarkframework.TestCaseResult;
import org.multiverse.benchmarkframework.BenchmarkResultRepository;

import java.util.List;

/**
 * The BenchmarkRunner is responsible for executing benchmarks.
 * <p/>
 * A BenchmarkRunner is not multithreaded itself (responsibility of the Driver) so can
 * not be compared to the {@link java.util.concurrent.Executor}.
 *
 * @author Peter Veentjer.
 */
public class BenchmarkExecutor {

    private BenchmarkResultRepository resultRepository;

    public BenchmarkExecutor(BenchmarkResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public void execute(List<Benchmark> benchmarkList) {
        execute(benchmarkList.toArray(new Benchmark[]{}));
    }

    public void execute(Benchmark... benchmarks) {
        for (Benchmark benchmark : benchmarks) {
            for (TestCase testCase: benchmark.testCases()) {
                doRun(testCase);
            }
        }
    }

    //todo: the name to determine the testcase is very ugly
    private void doRun(TestCase testCase) {
        warmup(testCase);
        benchmark(testCase);
    }

    //todo: in the loop you also want to see the specific attempt.        
    private void benchmark(TestCase testCase) {
        System.out.println("Starting testcases for: " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());

        BenchmarkResult benchmarkResult = new BenchmarkResult();

        for (int k = 0; k < testCase.getRunCount(); k++) {
            TestCaseResult testCaseResult = run(testCase, k + 1);
            benchmarkResult.add(testCaseResult);
        }

        resultRepository.store(benchmarkResult);

        System.out.println("Finished all testcases for " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());
    }

    private TestCaseResult run(TestCase testCase, int attempt) {
        Driver driver = testCase.getDriver();

        driver.preRun(testCase);
        TestCaseResult caseResult = new TestCaseResult(testCase, attempt);
        System.out.println("Starting executing testcase: " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());

        long startMs = System.currentTimeMillis();
        long startNs = System.nanoTime();
        driver.run();
        long endNs = System.nanoTime();
        long endMs = System.currentTimeMillis();
        long durationNs = endNs - startNs;

        caseResult.put("duration(ns)", durationNs);
        caseResult.put("start(ms)", startMs);
        caseResult.put("end(ms)", endMs);
        driver.postRun(caseResult);

        System.out.println("Finished testcase: " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());
        return caseResult;
    }

    private void warmup(TestCase testCase) {
        System.out.println("Starting warmup runs for testcase: " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());

        for (int k = 0; k < testCase.getWarmupRunCount(); k++) {
            run(testCase, k);
        }

        System.out.println("Finished warmup runs for testcase: " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());
    }
}
