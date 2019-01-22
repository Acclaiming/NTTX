for jar in ./libs/hutool/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/twitter4j/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/taip/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/bot/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ./libs/markdown/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

find -name "src/*.java" > sources.txt

rm -rf ./classes

mkdir ./classes

javac -d $(dirname $(readlink -f $0))/classes -classpath $CLASSPATH @sources.txt -nowarn

rm -rf ./sources.txt
