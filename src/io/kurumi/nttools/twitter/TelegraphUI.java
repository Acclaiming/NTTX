package io.kurumi.nttools.twitter;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.utils.Telegraph;

public class TelegraphUI {

    public static void test(UserData u, Msg msg) {

        if (!msg.isCommand() || !"ph".equals(msg.commandName())) return;

        switch (msg.commandParms()[0]) {

                case "c" : {

                    msg.send(Telegraph.createAccount(msg.commandParms()[1], msg.commandParms()[2], msg.commandParms()[3]).toStringPretty()).exec();

                    return;

                }
                
                case "d" : 
                    
                    msg.send(Telegraph.createPageWithAuth(msg.commandParms()[1],msg.commandParms()[2],msg.commandParms()[3],msg.commandParms()[4],msg.commandParms()[5]).toStringPretty()).exec();

        }

    }

}
