git fetch --depth=1 origin master && git reset --hard origin/master && git clean -fdx

for jar in ./libs/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

find src -name "*.java" > sources.txt

rm -rf ./cache
mkdir ./cache

javac -d $(dirname $(readlink -f $0))/cache -classpath $CLASSPATH @sources.txt

rm -rf ./sources.txt

rm -rf ./classes

mv cache classes

export CLASSPATH=$CLASSPATH:./classes

java -classpath $CLASSPATH io.kurumi.ntt.Launcher