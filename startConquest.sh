#! /usr/bin/sh

CONQUEST_HOME=/mnt/wind/development/conquest
JAVA_HOME=/usr/java/jdk1.3

CLASSPATH=$CONQUEST_HOME/classes:$CONQUEST_HOME/lib/log4j-full.jar
echo "CLASSPATH=$CLASSPATH"

$JAVA_HOME/bin/java -classpath $CLASSPATH com.osi.conquest.Main $CONQUEST_HOME/classes