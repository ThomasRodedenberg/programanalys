#! /bin/bash

N=${1%.teal}

if [ x$N == x ] || [ ${N}.teal != $1 ] ; then
    echo "Usage: $0 file.teal"
    echo "'$N' vs $1"
    exit 1
fi

if [ x${PYTHON} == x ]; then
    PYTHON=python
fi
./hw3.sh ${N}.teal | sort -n -k 2,2 -k 3,3 -k 6,6 -k 7,7 > ${N}.out
${PYTHON} compare-hw3.py ${N}.expected  ${N}.out
