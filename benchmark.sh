#!/bin/bash

function runbenchmark(){
java -Xbootclasspath/a:lib/provided/boot.jar -javaagent:target/multiverse-0.1.jar -classpath target/classes/test:lib/support/* org.benchy.BenchmarkMain ~/benchmarks <<< $1 EOF
}

runbenchmark '{"benchmarkName":"baseline/stack/LinkedBlockingQueue","driverClass":"org.multiverse.benchmarks.drivers.oldschool.queue.ContendedLinkedBlockingQueueDriver",
	"testcases":[
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"1","consumerCount":"1", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"2","consumerCount":"2", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"3","consumerCount":"3", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"4","consumerCount":"4", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"5","consumerCount":"5", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"6","consumerCount":"6", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"7","consumerCount":"7", "count":"2000000", "capacity":"1000"},
		{"runCount":"1", "warmupRunCount":"1", "producerCount":"8","consumerCount":"8", "count":"2000000", "capacity":"1000"}
	]}'

#runbenchmark '{"benchmarkName":"baseline/stack/ArrayBlockingQueue/unfair","driverClass":"org.multiverse.benchmarks.drivers.oldschool.queue.ContendedArrayBlockingQueueDriver",
#	"testcases":[
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"1","consumerCount":"1", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"2","consumerCount":"2", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"3","consumerCount":"3", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"4","consumerCount":"4", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"5","consumerCount":"5", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"6","consumerCount":"6", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"7","consumerCount":"7", "count":"2000000", "capacity":"1000", "fair":"false"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"8","consumerCount":"8", "count":"2000000", "capacity":"1000", "fair":"false"}
#	]}'

#runbenchmark '{"benchmarkName":"baseline/stack/ArrayBlockingQueue/fair","driverClass":"org.multiverse.benchmarks.drivers.oldschool.queue.ContendedArrayBlockingQueueDriver",
#	"testcases":[
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"1","consumerCount":"1", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"2","consumerCount":"2", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"3","consumerCount":"3", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"4","consumerCount":"4", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"5","consumerCount":"5", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"6","consumerCount":"6", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"7","consumerCount":"7", "count":"2000000", "capacity":"1000", "fair":"true"},
#		{"runCount":"1", "warmupRunCount":"1", "producerCount":"8","consumerCount":"8", "count":"2000000", "capacity":"1000", "fair":"true"}
#	]}'

runbenchmark '{"benchmarkName":"baseline/locks/FairJucLock","driverClass":"org.multiverse.benchmarks.drivers.oldschool.locks.ContendedLockDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"2","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"3","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"5","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"7","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"8","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"10","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"12","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"14","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"16","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"true"}
	]}'

runbenchmark '{"benchmarkName":"baseline/locks/UnfairJucLock","driverClass":"org.multiverse.benchmarks.drivers.oldschool.locks.ContendedLockDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"2","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"3","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"4","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"6","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"7","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"8","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"10","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"12","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"14","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"},
		{"runCount":"1","threadCount":"16","lockAndUnlockCount":"100000","warmupRunCount":"1", "fair":"false"}
	]}'

runbenchmark '{"benchmarkName":"baseline/locks/IntrinsicLock","driverClass":"org.multiverse.benchmarks.drivers.oldschool.locks.ContendedIntrinsicLockDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","lockAndUnlockCount":"100000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","lockAndUnlockCount":"100000","warmupRunCount":"1"}
	]}'


runbenchmark '{"benchmarkName":"baseline/ContendedCasBenchmark","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.ContendedCasDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","count":"1000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","count":"1000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/NoSharedStmNoSharedDataAndManualBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.NoSharedStmNoSharedDataAndManualDriver",
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

runbenchmark '{"benchmarkName":"baseline/SharedStmNoSharedDataAndManualBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.SharedStmNoSharedDataAndManualDriver",
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

runbenchmark '{"benchmarkName":"baseline/SharedStmNoSharedDataBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.SharedStmNoSharedDataDriver",
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

runbenchmark '{"benchmarkName":"baseline/SharedStmSharedBenchmark","driverClass":"org.multiverse.benchmarks.drivers.shared.SharedStmSharedDataDriver",
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

