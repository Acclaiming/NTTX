package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.*;
import io.kurumi.ntt.twitter.archive.*;

public class PMList extends Fragment {

    public static PMList INSTANCE = new PMList();
    
    @Override
    public boolean onNPM(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.command()) {

                case "pmlist" : pmList(user,msg);break;

                default : return false;

        }

        return true;

    }

    void pmList(UserData user,Msg msg) {

        if (!user.developer()) return;

        if (msg.params().length < 1) {

            msg.send("invaild params").exec();

            return;

        }

        String input = msg.params()[0];

        TAuth auth;

        if (NumberUtil.isLong(input)) {

            Long id = NumberUtil.parseLong(input);

            if (!TAuth.exists(id)) {

                msg.send("user not authed").exec();

                return;

            }

            auth = TAuth.get(id);

        } else {

            if (input.contains("t.me/")) {

                input = StrUtil.subAfter(input,"t.me/",true);

            }

            if (input.startsWith("@")) {

                input = input.substring(1);

            }

            UserData target = BotDB.getUserData(input);

            if (target == null) {

                msg.send("user not found").exec();

                return;

            }

            if (TAuth.exists(target.id)) {


                msg.send("user not authed").exec();

                return;

            }

            auth = TAuth.get(user.id);

            Twitter api = auth.createApi();

            try {
                
                DirectMessageList msgs;
                
                if (msg.params().length == 2) {
                
                 msgs = api.getDirectMessages(50,msg.params()[1]);
                
                } else {
                    
                    msgs = api.getDirectMessages(50);
                    
                }
                
                String next = msgs.getNextCursor();
                
                if (next != null) {
                    
                    msg.send("next curser : ").exec();
                    msg.send(next).exec();
                    
                }
                
                long id = auth.accountId;

                for (DirectMessage dm : msgs) {
                    
                    msg.send(parseDirectMessage(dm,id)).sync();
                    
                }

            } catch (TwitterException e) {
                
                msg.send(e.toString()).exec();
                
            }

        }



    }
    
    String parseDirectMessage(DirectMessage dm,long id) {
        
        StringBuilder format = new StringBuilder();
        
        UserArchive target;
        
        if (dm.getSenderId() == id) {
            
            target = BotDB.saveUser(dm.getRecipient());
            
            format.append("发送给 " + target.urlHtml());
            
        } else {
            
            target = BotDB.saveUser(dm.getSender());
            format.append("收到从 " + target.urlHtml());
        }
        
        format.append(" : ");
        
        if (dm.getText() != null) format.append(dm.getText());
        
        if (dm.getMediaEntities() != null) {
 
            for (MediaEntity media : dm.getMediaEntities()) {
                
                format.append(" ");
                format.append(Html.a("媒体文件",media.getMediaURLHttps()));
                
            }
            
        }
        
        return format.toString();
        
    }

}
        
