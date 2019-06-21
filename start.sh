export CLASSPATH=./classes

for jar in ./libs/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/tieba-api/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done

for jar in ./libs/google-voice/*.jar;do

 export CLASSPATH=$CLASSPATH:$jar
 
done


java -classpath $CLASSPATH io.kurumi.ntt.Launcher