package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.Env;
import com.pengrad.telegrambot.request.KickChatMember;

public class YourGroupRule extends Fragment {

    public static YourGroupRule INSTANCE = new YourGroupRule();
    
    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        if (msg.chatId() == Env.GROUP && msg.message().newChatMembers() != null) {

            for (User u : msg.message().newChatMembers()) {

                if (u.id() == 767682880) {


                    bot().execute(new KickChatMember(msg.chatId(),u.id()));

                    msg.delete();
                    
                    return true;

                }
                
            }
            
        }

        return false;

    }


}
