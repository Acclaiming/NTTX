git fetch -f --depth=1 origin master:refs/heads/origin/master

git update-ref --no-deref HEAD refs/heads/origin/master

git checkout-index -fua && git clean -fdx

for jar in ./libs/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

find src -name "*.java" > sources.txt

mkdir ./classes

javac -d $(dirname $(readlink -f $0))/classes -classpath $CLASSPATH @sources.txt

export CLASSPATH=$CLASSPATH:./classes

java -classpath $CLASSPATH io.kurumi.ntt.Launcher