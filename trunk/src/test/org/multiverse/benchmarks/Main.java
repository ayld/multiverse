package org.multiverse.benchmarks;

import org.multiverse.benchmarkframework.FileBasedBenchmarkResultRepository;
import org.multiverse.benchmarkframework.BenchmarkResultRepository;
import org.multiverse.benchmarkframework.executor.Benchmark;
import org.multiverse.benchmarkframework.executor.BenchmarkExecutor;
import org.multiverse.benchmarkframework.executor.Driver;
import org.multiverse.benchmarkframework.executor.TestCase;
import org.multiverse.benchmarks.drivers.oldschool.cas.ContendedCasDriver;
import org.multiverse.benchmarks.drivers.shared.NoSharedStmNoSharedDataAndManualDriver;
import org.multiverse.benchmarks.drivers.shared.SharedStmNoSharedDataAndManualDriver;
import org.multiverse.benchmarks.drivers.shared.SharedStmNoSharedDataDriver;
import org.multiverse.benchmarks.drivers.shared.SharedStmSharedDataDriver;
import org.multiverse.benchmarks.drivers.stack.ContendedTmStackDriver;
import org.multiverse.benchmarks.drivers.stack.UncontendedTmStackDriver;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Peter Veentjer
 */
public class Main {

    public static void main(String[] args) {
        File benchmarkDir = new File(args[0]);

        BenchmarkResultRepository resultRepository = new FileBasedBenchmarkResultRepository(benchmarkDir);

        BenchmarkExecutor executor = new BenchmarkExecutor(resultRepository);
        executor.execute(new SharedStmNoSharedDataBenchmark());
        executor.execute(new SharedStmSharedDataBenchmark());
        executor.execute(new SharedStmNoSharedDataAndManualBenchmark());
        executor.execute(new NoSharedStmNoSharedDataAndManualBenchmark());
        //executor.execute(new ContendedCasBenchmark());
        //executor.execute(new ContendedTmStackBenchmark());
        //executor.execute(new UncontendedTmStackBenchmark());
    }
}

class UncontendedTmStackBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new UncontendedTmStackDriver();
        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            //for (int l = 0; l < 10; l+=2) {
                TestCase testCase = new TestCase(driver);
                testCase.setBenchmarkname("UncontendedTmStackBenchmark");
                testCase.setWarmupRunCount(1);
                testCase.setRunCount(1);
                testCase.setProperty("itemCount", 2*1000*1000);
                testCase.setProperty("producerCount",k);
                cases.add(testCase);
            //}
        }

        return cases;
    }
}

class ContendedTmStackBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new ContendedTmStackDriver();
        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            //for (int l = 0; l < 10; l+=2) {
                TestCase testCase = new TestCase(driver);
                testCase.setBenchmarkname("ContendedTmStackBenchmark");
                testCase.setWarmupRunCount(1);
                testCase.setRunCount(1);
                testCase.setProperty("itemCount", 2*1000*1000);
                testCase.setProperty("producerCount",k);
                cases.add(testCase);
            //}
        }

        return cases;
    }
}

class ContendedCasBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new ContendedCasDriver();

        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            TestCase testCase = new TestCase(driver);
            testCase.setBenchmarkname("ContendedCasBenchmark");
            testCase.setWarmupRunCount(1);
            testCase.setRunCount(1);
            testCase.setProperty("count", 1000 * 1000);
            testCase.setProperty("threadCount", k);
            cases.add(testCase);
        }

        return cases;
    }
}


class SharedStmSharedDataBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new   SharedStmSharedDataDriver();

        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            TestCase testCase = new TestCase(driver);
            testCase.setBenchmarkname("SharedStmSharedDataBenchmark");
            testCase.setWarmupRunCount(1);
            testCase.setRunCount(1);
            testCase.setProperty("incCount", 1000 * 1000);
            testCase.setProperty("threadCount", k);
            cases.add(testCase);
        }

        return cases;
    }
}


class SharedStmNoSharedDataBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new SharedStmNoSharedDataDriver();

        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors();k++) {
            TestCase testCase = new TestCase(driver);
            testCase.setBenchmarkname("SharedStmNoSharedDataBenchmark");
            testCase.setWarmupRunCount(1);
            testCase.setRunCount(1);
            testCase.setProperty("incCount", 1000 * 1000);
            testCase.setProperty("threadCount", k);
            cases.add(testCase);
        }

        return cases;
    }
}

class NoSharedStmNoSharedDataAndManualBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new NoSharedStmNoSharedDataAndManualDriver();

        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            TestCase testCase = new TestCase(driver);
            testCase.setBenchmarkname("NoSharedStmNoSharedDataAndManualBenchmark");
            testCase.setWarmupRunCount(1);
            testCase.setRunCount(1);
            testCase.setProperty("incCount", 1000 * 1000);
            testCase.setProperty("threadCount", k);
            cases.add(testCase);
        }

        return cases;
    }
}


class SharedStmNoSharedDataAndManualBenchmark implements Benchmark {

    @Override
    public List<TestCase> testCases() {
        Driver driver = new SharedStmNoSharedDataAndManualDriver();

        List<TestCase> cases = new LinkedList<TestCase>();
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            TestCase testCase = new TestCase(driver);
            testCase.setBenchmarkname("SharedStmNoSharedDataAndManualBenchmark");
            testCase.setWarmupRunCount(1);
            testCase.setRunCount(1);
            testCase.setProperty("incCount", 1000 * 1000);
            testCase.setProperty("threadCount", k);
            cases.add(testCase);
        }

        return cases;
    }
}


