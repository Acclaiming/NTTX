git pull

for jar in ./libs/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

find src -name "*.java" > sources.txt

rm -rf ./classes

mkdir ./classes

javac -d $(dirname $(readlink -f $0))/classes -classpath $CLASSPATH @sources.txt -nowarn

rm -rf ./sources.txt

export CLASSPATH=./classes:$CLASSPATH

java -classpath $CLASSPATH io.kurumi.ntt.Launcher
