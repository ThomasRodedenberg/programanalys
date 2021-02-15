#! /bin/bash

N=${1%.teal}

if [ x$N == x ] || [ ${N}.teal != $1 ] ; then
    echo "Usage: $0 file.teal"
    echo "'$N' vs $1"
    exit 1
fi

./hw2.sh ${N}.teal > ${N}.out
python compare-hw2.py ${N}.expected  ${N}.out
