git fetch --depth=1 origin master && git checkout -f FETCH_HEAD && git clean -fdx

for jar in ./libs/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

find src -name "*.java" > sources.txt

mkdir ./classes

javac -d $(dirname $(readlink -f $0))/classes -classpath $CLASSPATH @sources.txt

export CLASSPATH=$CLASSPATH:./classes

java -classpath $CLASSPATH io.kurumi.ntt.Launcher