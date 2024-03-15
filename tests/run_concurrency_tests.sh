#!/bin/bash
# test.sh - Simple testing script for projects

# Defining colors
RES=$(tput sgr0)
RED=$(tput setaf 1)$(tput bold)
GREEN=$(tput setaf 2)$(tput bold)
FAILED="${RED}failed$RES"
PASSED="${GREEN}passed$RES"
ERROR="${RED}Erro: a flag '$1' nao existe.$RES"

# Paths
CLIENT="${PWD}/../Client"
TESTDIR="${PWD}/concurrency"
RESULTS="${TESTDIR}/test-outputs"

# Getting tests from the test directory
TESTS=$(ls "$TESTDIR"/*.in)

# Creating results directory if it doesn't exist
{ test -e "${RESULTS}" && test -d "${RESULTS}"; } || mkdir "${RESULTS}"

cd ${CLIENT}

# Executing all tests
for TEST in $TESTS; do
	TESTNAME=${TEST%.in}
	TESTNUM="${TESTNAME#"$TESTDIR"/}"
	OUT="${RESULTS}/${TESTNUM}".out
    CONCURRENT="${TESTNAME}.concurrent"

    mvn --quiet compile exec:java -Dexec.args="localhost 5001 0" < "$CONCURRENT" &
    mvn --quiet compile exec:java -Dexec.args="localhost 5001 1 -debug" < "$TEST"

	#{ diff "$OUT" "${TESTNAME}".out > /dev/null && echo "$TESTNUM $PASSED"; } || echo "$TESTNUM $FAILED"
done
