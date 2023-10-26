#!/bin/bash

if [ "$1" == "--clean" ]; then
    # Remove all the compiled .class files
    rm searchclient/*.class
    exit 0
fi

# Compile the Java code
javac searchclient/*.java

# Check for the algorithm flag
if [ $# -eq 1 ]; then
    algorithm=""
    memory=""
elif [ $# -eq 2 ]; then
    if [ "$2" == "dfs" ] || [ "$2" == "astar" ] || [ "$2" == "wastar" ] || [ "$2" == "greedy" ]; then
        algorithm="-"$2
        memory=""
    elif [ "$2" == "4" ]; then
        algorithm=""
        memory="-Xmx"$2"g"
    else
        echo "Invalid argument."
        exit 1
    fi
elif [ $# -eq 3 ]; then
    algorithm="-"$2
    memory="-Xmx"$3"g"
else
    echo "Invalid argument."
    exit 1
fi


# Run the compiled Java code
java -jar mavis.jar -l FinalLevels/"$1".lvl -c "java "$memory" searchclient.SearchClient "$algorithm"" -g -s 200 -t 180
