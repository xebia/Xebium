#!/bin/sh

# Make sure we have our jars close at hand.
# mvn dependency:copy-dependencies

# Standard options:
# -o -e 0


# java -Xmx256m -DmavenRepo="${M2_REPO}" -cp ${M2_REPO}/org/fitnesse/fitnesse/20100303/fitnesse-20100303.jar fitnesseMain.FitNesseMain -p 8001 -o -e 0 $1 $2 $3 $4 $5
mvn -Pfitnesse test
