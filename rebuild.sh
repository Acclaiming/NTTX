for jar in ./libs/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/rome/*.jar;do

export CLASSPATH=$CLASSPATH:$jar
 
done

#for jar in ./libs//*.jar ;do
#
# export CLASSPATH=$CLASSPATH:$jar
# 
#done

find src -name "*.java" > sources.txt

echo ">>> Building Sources <<<"

mkdir ./build

javac -d $(dirname $(readlink -f $0))/build -classpath $CLASSPATH @sources.txt && rm -rf ./classes && mv ./build ./classes

rm -rf ./build ./sources.txt