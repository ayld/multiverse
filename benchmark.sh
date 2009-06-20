#!/bin/bash

function runbenchmark(){
java -Xbootclasspath/a:lib/provided/boot.jar -javaagent:target/multiverse-0.1.jar -classpath target/classes/test:lib/support/* org.benchy.BenchmarkMain ~/benchmarks <<< $1 EOF
}

#runbenchmark '{"benchmarkName":"baseline/cas/ContendedCasBenchmark","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.ContendedCasDriver",
#	"testcases":[
#		{"runCount":"1","threadCount":"1","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"2","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"3","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"4","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"5","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"6","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"7","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"8","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"9","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"10","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"11","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"12","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"13","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"14","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"15","count":"1000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"16","count":"1000000","warmupRunCount":"1"}
#	]}'

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