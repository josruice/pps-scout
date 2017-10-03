n=20
s=5
e=20
t=200
repeats=1
p=g5
m=sparse_landmarks
em=random_enemymap
fps=5

all: compile

compile:
	javac scout/sim/Simulator.java

gui:
	java scout.sim.Simulator --fps ${fps} --gui -p ${p} -m ${m} -em ${em} -n ${n} -e ${e} -s ${s} -t ${t}

run:
	java scout.sim.Simulator -r ${repeats} -p ${p} -m ${m} -em ${em} -n ${n} -e ${e} -s ${s} -t ${t}

verbose:
	java scout.sim.Simulator -p ${p} -m ${m} -em ${em} -n ${n} -e ${e} -s ${s} -t ${t} --verbose
