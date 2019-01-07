#!/bin/bash

cd ../

homepath=`eval echo ~$USER`

if [ $# -lt 1 ] 
then
    echo "Usage: " $0 " <index-path>";
    echo "1. index-path: ";
    exit 1;
fi

prop_name="build/classes/getall-wikipediaid-"$col_name".properties"

index_path=`readlink -f $1`		# absolute path of where to store the index

# making the .properties file in 'build/classes'
cat > $prop_name << EOL

indexPath=$index_path
EOL
# .properties file created in 'build/classes' 

java -cp $CLASSPATH:dist/Wikipedia.jar:./lib/* indexer.GetAllWikidataId $prop_name

