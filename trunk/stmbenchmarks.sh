#!/bin/bash

function runbenchmark(){
java -Xbootclasspath/a:lib/provided/boot.jar -javaagent:target/multiverse-0.1.jar -classpath target/classes/test:lib/support/* org.benchy.executor.BenchmarkMain ~/benchmarks <<< $1 EOF
}

function createDiagram(){
java -classpath target/classes/test:lib/support/* org.benchy.graph.GraphMain ~/benchmarks /tmp/out.dat $1 $2 $3
}

runbenchmark '{"benchmarkName":"baseline/stm/NoSharedStmNoSharedDataAndManualBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.NoSharedStmNoSharedDataAndManualDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","incCount":"1000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/stm/SharedStmNoSharedDataAndManualBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.SharedStmNoSharedDataAndManualDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","incCount":"1000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/stm/SharedStmNoSharedDataBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.SharedStmNoSharedDataDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","incCount":"1000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/stm/SharedStmSharedDataBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.SharedStmSharedDataDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","incCount":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","incCount":"1000000","warmupRunCount":"1"}
	]}'


createDiagram 'baseline/stm/NoSharedStmNoSharedDataAndManualBenchmark;baseline/stm/SharedStmNoSharedDataAndManualBenchmark;baseline/stm/SharedStmNoSharedDataBenchmark;baseline/stm/SharedStmSharedBenchmark' 'threadCount' 'transactions/second'
gnuplot <<< '
set title ""
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/stm_completerangeofsharedandunshared.png"
plot "/tmp/out.dat" using 1:2 title "nothing shared & manual" with linespoint, \
     "/tmp/out.dat" using 1:3 title "shared stm no shared state & manual" with linespoint, \
     "/tmp/out.dat" using 1:4 title "shared stm no shared state" with linespoint, \
     "/tmp/out.dat" using 1:5 title "shared stm, shared data" with linespoint'
