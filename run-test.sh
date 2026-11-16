#!/bin/bash
# usage: ./run-test.sh <test> [extra args]   (renders off-screen with Xvfb and writes PNGs to ./shots)
cd "$(dirname "$0")"
CP=$(cat build/demo.cp 2>/dev/null)
T=$1; shift
xvfb-run -a -s "-screen 0 2400x2800x24" java -XX:+UseSerialGC -cp "$CP" com.star4droid.compat.demo.DesktopLauncher --test "$T" "$@" 2>&1 | grep -E "SHOT|Exception|Error|\tat |INFO|PERF|ASSERT" 
