#!/bin/bash

function runbenchmark(){
java -Xbootclasspath/a:lib/provided/boot.jar -javaagent:target/multiverse-0.1.jar -classpath target/classes/test:lib/support/* org.benchy.executor.BenchmarkMain ~/benchmarks <<< $1 EOF
}

function createDiagram(){
java -classpath target/classes/test:lib/support/* org.benchy.graph.GraphMain ~/benchmarks /tmp/out.dat $1 $2 $3
}

runbenchmark '{"benchmarkName":"baseline/cas/AtomicLong","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.AtomicLongDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","count":"10000000","warmupRunCount":"1"}		
	]}'


createDiagram 'baseline/cas/AtomicIntegerFieldUpdater' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicIntegerFieldUpdater"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomicintegerfieldupdater.png"
plot "/tmp/out.dat" using 1:2 title "AtomicIntegerFieldUpdater" with linespoint'

