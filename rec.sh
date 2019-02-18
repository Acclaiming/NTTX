export CLASSPATH=./classes

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

java -classpath $CLASSPATH io.kurumi.ntt.Recovery
