set title ''
set xlabel 'threads'
set ylabel 'transactions/second'
set grid
set terminal png
set output '/tmp/diagram.png'
plot '/tmp/graph.dat' using 1:2 with linespoint, \
     '/tmp/graph.dat' using 1:3 with linespoint, \
     '/tmp/graph.dat' using 1:4 with linespoint, \
     '/tmp/graph.dat' using 1:5 with linespoint
