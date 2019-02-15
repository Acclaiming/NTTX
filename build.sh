for jar in ./libs/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/twitter4j/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/markdown/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/jedis/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

find src -name "*.java" > sources.txt

rm -rf ./classes

mkdir ./classes

javac -d $(dirname $(readlink -f $0))/classes -classpath $CLASSPATH @sources.txt -nowarn

rm -rf ./sources.txt
