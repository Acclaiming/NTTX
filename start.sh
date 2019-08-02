export CLASSPATH=./classes

for jar in ./libs/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/mstd/*.jar;do

export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/mongodb/*.jar;do

export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/rome/*.jar;do

export CLASSPATH=$CLASSPATH:$jar
 
done

java -classpath $CLASSPATH io.kurumi.ntt.Launcher