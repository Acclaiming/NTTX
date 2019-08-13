export CLASSPATH=./classes

for jar in libs/*.jar libs/**/*.jar; do

 export CLASSPATH=$CLASSPATH:$jar
 
done

java -classpath $CLASSPATH io.kurumi.ntt.Launcher