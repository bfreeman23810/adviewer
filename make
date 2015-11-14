#!/bin/sh
#
# Brian Freeman
# make - Script to make the class files
# November 14, 2015
# 
########################################################################


#path to lib files
P=$(pwd)"/lib"

#go through lib files and add to class path
for jar in `ls $P| grep .jar `
do
	temp=$P"/"$jar":"$temp
done

CP=$temp":."

#echo ${CP}
#exit 0

SRC="./src/adviewer"
BIN="../../bin"

cd ${SRC}

pwd

#find all .java files and attrmpt to build them
find -name "*.java" > sources.txt
#more sources.txt

javac -cp $CP -d $BIN @sources.txt

