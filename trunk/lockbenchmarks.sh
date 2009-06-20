#!/bin/bash

function runbenchmark(){
java -Xbootclasspath/a:lib/provided/boot.jar -javaagent:target/multiverse-0.1.jar -classpath target/classes/test:lib/support/* org.benchy.BenchmarkMain ~/benchmarks <<< $1 EOF
}

function createDiagram(){
java -classpath target/classes/test:lib/support/* org.benchy.DiagramMain ~/benchmarks /tmp/out.dat $1 $2 $3
}

#runbenchmark '{"benchmarkName":"baseline/locks/IntrinsicLock","driverClass":"org.multiverse.benchmarks.drivers.oldschool.locks.ContendedIntrinsicLockDriver",
#	"testcases":[
#		{"runCount":"1","threadCount":"1","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"2","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"3","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"4","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"6","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"7","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"8","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"10","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"12","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"14","lockAndUnlockCount":"200000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"16","lockAndUnlockCount":"200000000","warmupRunCount":"1"}
#	]}'

#runbenchmark '{"benchmarkName":"baseline/locks/ReentrantLock/unfair","driverClass":"org.multiverse.benchmarks.drivers.oldschool.locks.ContendedLockDriver",
#	"testcases":[
#		{"runCount":"1","threadCount":"1","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"2","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"3","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"4","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"6","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"7","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"8","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"10","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"12","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"14","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"},
#		{"runCount":"1","threadCount":"16","lockAndUnlockCount":"100000000","warmupRunCount":"1", "fair":"false"}
#	]}'

runbenchmark '{"benchmarkName":"baseline/locks/ReentrantLock/fair","driverClass":"org.multiverse.benchmarks.drivers.oldschool.locks.ContendedLockDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"2","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"3","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"5","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"7","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"8","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"10","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"12","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"14","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"},
		{"runCount":"1","threadCount":"16","lockAndUnlockCount":"500000","warmupRunCount":"1", "fair":"true"}
	]}'


# ================= diagrams =======================

createDiagram 'baseline/locks/IntrinsicLock' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "Intrinsic lock"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/intrinsiclock.png"
plot "/tmp/out.dat" using 1:2 title "Intrinsic lock" with linespoint'

createDiagram 'baseline/locks/JuckLock/fair;baseline/locks/JuckLock/unfair;baseline/locks/IntrinsicLock' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "Intrinsic lock vs fair ReentrantLock vs unfair Reentrantlock"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/fairjuclock_unfairjuclock_intrinsiclock.png"
plot "/tmp/out.dat" using 1:2 title "fair ReentrantLock" with linespoint, \
     "/tmp/out.dat" using 1:3 title "unfair ReentrantLock" with linespoint, \
     "/tmp/out.dat" using 1:4 title "intrinsic lock" with linespoint'

createDiagram 'baseline/locks/ReentrantLock/fair;baseline/locks/IntrinsicLock' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "Intrinsic lock vs unfair ReentrantLock"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/intrinsiclock_vs_unfairjuclock.png"
plot "/tmp/out.dat" using 1:2 title "unfair ReentrantLock" with linespoint, \
     "/tmp/out.dat" using 1:3 title "intrinsic lock" with linespoint'

createDiagram 'baseline/locks/ReentrantLock/fair' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "fair java.util.concurrent.locks.ReentrantLock"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/fairreentrantlock.png"
plot "/tmp/out.dat" using 1:2 title "fair ReentrantLock" with linespoint'

createDiagram 'baseline/locks/ReentrantLock/unfair' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "unfair java.util.concurrent.locks.ReentrantLock"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/unfairreentrantlock.png"
plot "/tmp/out.dat" using 1:2 title "unfair ReentrantLock" with linespoint'