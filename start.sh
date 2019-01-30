export CLASSPATH=./classes

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

java -classpath $CLASSPATH io.kurumi.ntt.BotMain