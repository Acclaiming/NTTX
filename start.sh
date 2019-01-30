export CLASSPATH=./classes

for jar in find libs -name "*.jar" do
 export CLASSPATH=$CLASSPATH:$jar
done

java -classpath $CLASSPATH io.kurumi.ntt.BotMain