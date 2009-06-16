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

            for (TestCase testCase : benchmark.testCases()) {
                warmup(testCase);
                for (int attempt = 1; attempt <= testCase.getRunCount(); attempt++) {
                    TestCaseResult testCaseResult = run(testCase, attempt);
                    resultList.add(testCaseResult);
                }
            }

            BenchmarkResult benchmarkResult = new BenchmarkResult(benchmark.getBenchmarkName(), resultList);
            resultRepository.store(benchmarkResult);
        }
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
            run(testCase, k + 1);
        }

        System.out.println("Finished warmup runs for testcase: " + testCase.getBenchmarkName() + " " + testCase.getPropertiesDescription());
    }
}
