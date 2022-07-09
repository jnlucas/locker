#!/bin/bash
nohup java BikeLock > log.txt 2> errors.txt < /dev/null &
PID=$!
echo $PID > pid.txt

