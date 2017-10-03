# Scout
Arguments:
-p/--player -> player package
-m/--map -> Landmark map
-em/--emap -> enemy map
-s/--scouts -> number of scouts
-n/--board -> board size
-t/--time -> number of turns
-e/--enemies -> number of enemies
-r/--repeats -> number of times to run the simulation, defaults to 1
-S/--seed -> seed for randomization, defaults to system current time millis
-f/--fps -> frames per second for gui
--gui -> gui enabled
--verbose -> verbose

The makefile gives you sample parameters. You can use the makefile for convenience if you want to. Makefile commands:
make compile
-> Compiles simulator
make gui
-> runs the simulator with gui
make run
-> runs without gui
make verbose
-> runs on verbose mode

Make sure to read scout.sim.Player, scout.sim.Outpost and scout.random.Player before you start to understand how communication happens!
Good luck!