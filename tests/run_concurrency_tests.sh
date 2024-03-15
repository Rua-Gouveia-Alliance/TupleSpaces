#!/bin/bash
# test.sh - Simple testing script for projects

# Colors
RES=$(tput sgr0)
GREEN=$(tput setaf 2)$(tput bold)
RED=$(tput setaf 1)$(tput bold)

# Messages
ERROR="${RED}Unrecognized flag: '$1'$RES"

# Paths
CLIENT="${PWD}/../Client"
TESTDIR="${PWD}/concurrency"

# Show usage
help() {
	[ "$1" = "" ] || printf "%s\n\n" "$ERROR"
	printf "%s\n" "Usage: run_concurrency_tests [-t <testnum>] [-h]"
	printf "%s\n" "Flags:"
	printf "%s\n" "-t <testnum>: Run test number <testnum> instead of running all tests."
	printf "%s\n" "-h: Print this."
	exit 0
}

# Check for flags
while getopts :t:h FLAG; do
	case $FLAG in
		t) TESTS=$(ls "$TESTDIR"/*"$OPTARG".in);;
		h) help;;
		?) help "$OPTARG";;
	esac
done

# Default value if a flag is not provided
[ "$TEST" = "" ] && TESTS=$(ls "$TESTDIR"/*.in)

# Moving to the right directory
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
