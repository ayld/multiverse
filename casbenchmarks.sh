#!/bin/bash

function runbenchmark(){
java -Xbootclasspath/a:lib/provided/boot.jar -javaagent:target/multiverse-0.1.jar -classpath target/classes/test:lib/support/* org.benchy.BenchmarkMain ~/benchmarks <<< $1 EOF
}

function createDiagram(){
java -classpath target/classes/test:lib/support/* org.benchy.DiagramMain ~/benchmarks /tmp/out.dat $1 $2 $3
}

runbenchmark '{"benchmarkName":"baseline/cas/AtomicLong","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.AtomicLongDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","count":"10000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/cas/AtomicInteger","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.AtomicIntegerDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","count":"10000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/cas/AtomicInteger","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.AtomicIntegerFieldUpdaterDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","count":"10000000","warmupRunCount":"1"}
	]}'

runbenchmark '{"benchmarkName":"baseline/cas/AtomicInteger","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.AtomicLongFieldUpdaterDriver",
	"testcases":[
		{"runCount":"1","threadCount":"1","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"2","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"3","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"4","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"5","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"6","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"7","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"8","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"9","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"10","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"11","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"12","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"13","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"14","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"15","count":"10000000","warmupRunCount":"1"},
		{"runCount":"1","threadCount":"16","count":"10000000","warmupRunCount":"1"}
	]}'


#runbenchmark '{"benchmarkName":"baseline/cas/AtomicReferenceFieldUpdater","driverClass":"org.multiverse.benchmarks.drivers.oldschool.cas.AtomicReferenceFieldUpdaterDriver",
#	"testcases":[
#		{"runCount":"1","threadCount":"1","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"2","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"3","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"4","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"5","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"6","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"7","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"8","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"9","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"10","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"11","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"12","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"13","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"14","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"15","count":"10000000","warmupRunCount":"1"},
#		{"runCount":"1","threadCount":"16","count":"10000000","warmupRunCount":"1"}
#	]}'

createDiagram 'baseline/cas/AtomicLong' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicLong"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomiclong.png"
plot "/tmp/out.dat" using 1:2 title "AtomicLong" with linespoint'

createDiagram 'baseline/cas/AtomicInteger' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicInteger"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomicinteger.png"
plot "/tmp/out.dat" using 1:2 title "AtomicInteger" with linespoint'

createDiagram 'baseline/cas/AtomicIntegerFieldUpdater' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicIntegerFieldUpdater"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomicintegerfieldupdater.png"
plot "/tmp/out.dat" using 1:2 title "AtomicIntegerFieldUpdater" with linespoint'

createDiagram 'baseline/cas/AtomicLongFieldUpdater' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicLongFieldUpdater"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomiclongfieldupdater.png"
plot "/tmp/out.dat" using 1:2 title "AtomicLongFieldUpdater" with linespoint'

#createDiagram 'baseline/cas/AtomicReferenceFieldUpdater' 'threadCount' 'transactions/second'
#gnuplot <<< '
#set title "AtomicReferenceFieldUpdater"
#set xlabel "threads"
#set ylabel "transactions/second"
#set grid
#set terminal png
#set output "/tmp/atomicreferencefieldupdater.png"
#plot "/tmp/out.dat" using 1:2 title "AtomicReferenceFieldUpdater" with linespoint'

createDiagram 'baseline/cas/AtomicLong;baseline/cas/AtomicInteger' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicLong vs AtomicInteger"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomiclongvsatomicinteger.png"
plot "/tmp/out.dat" using 1:2 title "AtomicInteger" with linespoint,\
     "/tmp/out.dat" using 1:3 title "AtomicLong" with linespoint'

createDiagram 'baseline/cas/AtomicLongFieldUpdater;baseline/cas/AtomicIntegerFieldUpdater' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicLongFieldUpdater vs AtomicIntegerFieldUpdater"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomiclongfieldupdatervsatomicintegerfieldupdater.png"
plot "/tmp/out.dat" using 1:2 title "AtomicLongFieldUpdater" with linespoint,\
     "/tmp/out.dat" using 1:3 title "AtomicIntegerFieldUpdater" with linespoint'

createDiagram 'baseline/cas/AtomicLong;baseline/cas/AtomicLongFieldUpdater' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicLong vs AtomicLongFieldUpdater"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomiclongvsatomiclongfieldupdater.png"
plot "/tmp/out.dat" using 1:2 title "AtomicLong" with linespoint,\
     "/tmp/out.dat" using 1:3 title "AtomicLongFieldUpdater" with linespoint'

createDiagram 'baseline/cas/AtomicInteger;baseline/cas/AtomicIntegerFieldUpdater' 'threadCount' 'transactions/second'
gnuplot <<< '
set title "AtomicInteger vs AtomicIntegerFieldUpdater"
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/atomicintegervsatomicintegerfieldupdater.png"
plot "/tmp/out.dat" using 1:2 title "AtomicInteger" with linespoint,\
     "/tmp/out.dat" using 1:3 title "AtomicIntegerFieldUpdater" with linespoint'     



