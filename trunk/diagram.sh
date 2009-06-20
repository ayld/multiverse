#!/bin/bash

function createDiagram(){
java -classpath target/classes/test:lib/support/* org.benchy.DiagramMain ~/benchmarks /tmp/out.dat $1 $2 $3
}




# ================= diagrams =======================

createDiagram 'baseline/stm/NoSharedStmNoSharedDataAndManualBenchmark;baseline/stm/SharedStmNoSharedDataAndManualBenchmark;baseline/stm/SharedStmNoSharedDataBenchmark;baseline/stm/SharedStmSharedBenchmark' 'threadCount' 'transactions/second'
gnuplot <<< '
set title ''
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/stm_completerangeofsharedandunshared.png"
plot "/tmp/out.dat" using 1:2 title "nothing shared & manual" with linespoint, \
     "/tmp/out.dat" using 1:3 title "shared stm no shared state & manual" with linespoint, \
     "/tmp/out.dat" using 1:4 title "shared stm no shared state" with linespoint, \
     "/tmp/out.dat" using 1:5 title "shared stm, shared data" with linespoint'

# ================= diagrams =======================

createDiagram 'baseline/cas/ContendedCasBenchmark' 'threadCount' 'duration(ns)'
gnuplot <<< '
set title ''
set xlabel "threads"
set ylabel "transactions/second"
set grid
set terminal png
set output "/tmp/plot2.png"
plot "/tmp/out.dat" using 1:2 with linespoint'