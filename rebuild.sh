rm -rf ./classes && mkdir ./classes

for jar in ./libs/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/tieba-api/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

find src -name "*.java" > sources.txt

echo ">>> Building Sources <<<"

javac -d $(dirname $(readlink -f $0))/classes -classpath $CLASSPATH @sources.txt && rm -rf ./sources.txt