#!/bin/bash

cd ../

homepath=`eval echo ~$USER`

if [ $# -lt 5 ] 
then
    echo "Usage: " $0;
    echo "1. indexPathA";
    echo "2. indexPathB"; 
    echo "3. filePathSevenCol";
    echo "4. dumpPathA";
    echo "5. dumpPathB";

    exit 1;
fi

java -cp $CLASSPATH:dist/Wikipedia.jar:./lib/* indexer.DumpCommonArticles $1 $2 $3 $4 $5

