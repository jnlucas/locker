#!/bin/bash
nohup java -cp .:mssql-jdbc-10.2.1.jre11.jar BikeLock > log.txt 2> errors.txt < /dev/null &
PID=$!
echo $PID > pid.txt

