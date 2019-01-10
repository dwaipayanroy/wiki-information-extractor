#!/bin/bash

cd ../

# readlink (for getting the absolute path) must be installed

homepath=`eval echo ~$USER`
# stopFilePath="$homepath/smart-stopwords"
# if [ ! -f $stopFilePath ]
# then
#     echo "Please ensure that the path of the stopword-list-file is set in the .sh file."
# else
#     echo "Using stopFilePath="$stopFilePath
# fi

toStore="YES" # YES/NO
storeTermVector="YES"
echo "Storing the content in index: "$toStore
echo "Storing the term-vectors: "$storeTermVector

if [ $# -le 1 ] 
then
    echo "Usage: " $0 " <spec-path> <index-path> [dump-path]";
    echo "1. spec-path: ";
    echo "2. index-path: ";
    echo "3. Language: ar/de/en/es/hi/ko/pt/ru/tr: ";
    exit 1;
fi

col_name=$(basename $1 .spec)
prop_name="build/classes/wikipedia-indexer-"$col_name".properties"
spec_path=`readlink -f $1`		# absolute address of the .properties file
coll_lang=$3

if [ ! -f $spec_path ]
then
    echo "Spec file not exists"
    exit 1;
fi
index_path=`readlink -f $2`		# absolute path of where to store the index

# making the .properties file in 'build/classes'
cat > $prop_name << EOL

collSpec=$spec_path

indexPath=$index_path

collLang=$coll_lang
EOL
# .properties file created in 'build/classes' 

java -cp $CLASSPATH:dist/Wikipedia.jar:./lib/* indexer.WikipediaIndexer $prop_name

cp $prop_name $index_path/.

echo "The .properties file is saved in the index directory: "$index_path/

