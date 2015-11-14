#!/bin/sh

P="/root/workspace/java/adviewer/lib"

for jar in `ls $P| grep .jar `
do
	temp=$P"/"$jar":"$temp
done

CP=$temp":."

#echo ${CP}
#exit 0

SRC="./src/adviewer"
BIN="../../bin"

cd $SRC
pwd
find -name "*.java" > sources.txt
#more sources.txt
#javac -cp $CP -d $BIN @sources.txt
javac -cp $CP -d $BIN @sources.txt

#cd .$BIN
#java -cp $CP ADMain 
