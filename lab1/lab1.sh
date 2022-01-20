#!/bin/sh

javac -sourcepath ./SharedClasses/src -d ./SharedClasses/bin -encoding UTF-8 ./SharedClasses/src/hr/fer/zemris/srsv/*.java
javac -sourcepath ./SEM/src -cp ./SharedClasses/bin  -d ./SEM/bin -encoding UTF-8 ./SEM/src/hr/fer/zemris/srsv/sem/*.java
javac -sourcepath ./UPR/src -cp ./SharedClasses/bin -d ./UPR/bin -encoding UTF-8 ./UPR/src/hr/fer/zemris/srsv/upr/*.java
javac -sourcepath ./RAS/src -cp ./SharedClasses/bin -d ./RAS/bin -encoding UTF-8 ./RAS/src/hr/fer/zemris/srsv/ras/*.java
javac -sourcepath ./AiP/src -cp ./SharedClasses/bin -d ./AiP/bin -encoding UTF-8 ./AiP/src/hr/fer/zemris/srsv/aip/*.java

x-terminal-emulator -e java -Dfile.encoding=UTF-8 -classpath ./SEM/bin hr.fer.zemris.srsv.sem.Main
x-terminal-emulator -e java -Dfile.encoding=UTF-8 -classpath ./UPR/bin hr.fer.zemris.srsv.upr.Main
x-terminal-emulator -e java -Dfile.encoding=UTF-8 -classpath ./RAS/bin hr.fer.zemris.srsv.ras.Main
x-terminal-emulator -e java -Dfile.encoding=UTF-8 -classpath ./AiP/bin hr.fer.zemris.srsv.aip.Main
