#!/bin/sh

# Standard options:
# -o -e 0

java -Xmx256m -DmavenRepo="${M2_REPO}" -cp ${M2_REPO}/org/fitnesse/fitnesse/20100303/fitnesse-20100303.jar fitnesseMain.FitNesseMain -o -e 0 $1 $2 $3 $4 $5
