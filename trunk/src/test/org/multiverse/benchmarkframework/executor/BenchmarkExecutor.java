package org.multiverse.benchmarkframework.executor;

import org.multiverse.benchmarkframework.BenchmarkResult;
import org.multiverse.benchmarkframework.BenchmarkResultRepository;
import org.multiverse.benchmarkframework.TestCaseResult;

import java.util.LinkedList;
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
            List<TestCaseResult> resultList = new LinkedList<TestCaseResult>();

            System.out.println("Starting with benchmark: " + benchmark.benchmarkName);

            for (TestCase testCase : benchmark.testCaseList) {
                warmup(benchmark, testCase);
                for (int attempt = 1; attempt <= testCase.getRunCount(); attempt++) {
                    TestCaseResult testCaseResult = run(benchmark, testCase, attempt);
                    resultList.add(testCaseResult);
                }
            }

            BenchmarkResult benchmarkResult = new BenchmarkResult(benchmark.benchmarkName, resultList);
            resultRepository.store(benchmarkResult);
            System.out.println("Finished with benchmark: " + benchmark.benchmarkName + ", result was stored");
        }
    }

    private TestCaseResult run(Benchmark benchmark, TestCase testCase, int attempt) {
        Driver driver = benchmark.getDriver();

        driver.preRun(testCase);
        TestCaseResult caseResult = new TestCaseResult(benchmark, testCase, attempt);
        System.out.println("Starting executing testcase: " + benchmark.benchmarkName + " " + testCase.getPropertiesDescription());

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

        System.out.println("Finished testcase: " + benchmark.benchmarkName + " " + testCase.getPropertiesDescription());
        return caseResult;
    }

    private void warmup(Benchmark benchmark, TestCase testCase) {
        System.out.println("Starting warmup runs for testcase: " + benchmark.benchmarkName + " " + testCase.getPropertiesDescription());

        for (int k = 0; k < testCase.getWarmupRunCount(); k++) {
            run(benchmark, testCase, k + 1);
        }

        System.out.println("Finished warmup runs for testcase: " + benchmark.benchmarkName + " " + testCase.getPropertiesDescription());
    }
}
