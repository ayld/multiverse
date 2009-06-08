package org.multiverse.benchmark;

public class Main {

    public static void main(String[] args){
        Benchmark benchmark = new StackStressTestBenchmark();

        ResultRepository resultRepository = new ResultRepository();
        BenchmarkRunner runner = new BenchmarkRunner(resultRepository);
        runner.execute(benchmark);
    }
}
