#!/bin/bash
# Given the Wikipedia curid, this code finds the corresponding Wikidata Id for that article.
# which will be needed to 'join' the same articles in different languages.

cd ../

if [ $# -le 1 ] 
then
    echo "Usage: " $0 " <lang> <tsv-file, first-column containing wikipedia-pageids> <output-file-path>";
    echo "1. lang ";
    echo "2. tsv (.tsv):: ";
    echo "3. output file path (.tsv): ";
    exit 1;
fi

lang=$1
input=$2
output=$3

java -cp $CLASSPATH:dist/Wikipedia.jar:./lib/* indexer.CallWikipediaAPI $lang $input $output

echo "Complete."
