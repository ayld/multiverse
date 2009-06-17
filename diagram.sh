#!/bin/bash

function createDiagram(){
java -classpath target/classes/test:lib/support/* org.benchy.DiagramMain ~/benchmarks /tmp/out.dat $1 $2 $3
}


# ================= diagrams =======================

createDiagram 'baseline/locks/FairJucLock;baseline/locks/UnfairJucLock;baseline/locks/IntrinsicLock' 'threadCount' 'duration(ns)'
gnuplot <<< '
set title ''
set xlabel "threads"
set ylabel "duration(ns)"
set grid
set terminal png
set output "/tmp/locks.png"
plot "/tmp/out.dat" using 1:2 title "fair juc Lock" with linespoint, \
     "/tmp/out.dat" using 1:3 title "unfair juc lock" with linespoint, \
     "/tmp/out.dat" using 1:4 title "intrinsic lock" with linespoint'

createDiagram 'baseline/stack/LinkedBlockingQueue;baseline/stack/LinkedBlockingQueue' 'threadCount' 'duration(ns)'
gnuplot <<< '
set title ''
set xlabel "producerCount"
set ylabel "duration(ns)"
set grid
set terminal png
set output "/tmp/arrayblockingqueues.png"
plot "/tmp/out.dat" using 1:2 title "unfair juc lock" with linespoint, \
     "/tmp/out.dat" using 1:3 title "intrinsic lock" with linespoint'

# ================= diagrams =======================

createDiagram 'baseline/locks/UnfairJucLock;baseline/locks/IntrinsicLock' 'threadCount' 'duration(ns)'
gnuplot <<< '
set title ''
set xlabel "threads"
set ylabel "duration(ns)"
set grid
set terminal png
set output "/tmp/locks2.png"
plot "/tmp/out.dat" using 1:2 title "unfair juc lock" with linespoint, \
     "/tmp/out.dat" using 1:3 title "intrinsic lock" with linespoint'

# ================= diagrams =======================

createDiagram 'baseline/NoSharedStmNoSharedDataAndManualBenchmark;baseline/SharedStmNoSharedDataAndManualBenchmark;baseline/SharedStmNoSharedDataBenchmark;baseline/SharedStmSharedBenchmark' 'threadCount' 'transactions/second'
gnuplot <<< '
set title ''
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/plot1.png"
plot "/tmp/out.dat" using 1:2 title "nothing shared & manual" with linespoint, \
     "/tmp/out.dat" using 1:3 title "shared stm no shared state & manual" with linespoint, \
     "/tmp/out.dat" using 1:4 title "shared stm no shared state" with linespoint, \
     "/tmp/out.dat" using 1:5 title "shared stm, shared data" with linespoint'

# ================= diagrams =======================

createDiagram 'baseline/ContendedCasBenchmark' 'threadCount' 'duration(ns)'
gnuplot <<< '
set title ''
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/plot2.png"
plot "/tmp/out.dat" using 1:2 with linespoint'