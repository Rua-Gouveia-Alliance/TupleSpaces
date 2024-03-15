#!/bin/bash
# test.sh - Simple testing script for projects

# Colors
RES=$(tput sgr0)
GREEN=$(tput setaf 2)$(tput bold)

# Paths
CLIENT="${PWD}/../Client"
TESTDIR="${PWD}/concurrency"

# Getting tests from the test directory
TESTS=$(ls "$TESTDIR"/*.in)

cd "${CLIENT}" || exit

# Executing all tests
for TEST in $TESTS; do
	TESTNAME=${TEST%.in}
	TESTNUM="${TESTNAME#"$TESTDIR"/}"
    CONCURRENT="${TESTNAME}.concurrent"

    echo "${GREEN} Running test ${TESTNUM}.${RES}"

    mvn --quiet compile exec:java -Dexec.args="localhost 5001 0" < "$CONCURRENT" &
    mvn --quiet compile exec:java -Dexec.args="localhost 5001 1 -debug" < "$TEST"

done
