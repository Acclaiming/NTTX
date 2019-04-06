package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.Env;

public class YourGroupRule extends Fragment {

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {
        // TODO: Implement this method
        
        if (msg.chatId() == Env.GROUP && msg.message().newChatMembers() != null) {
            
            for (User u : msg.message().newChatMembers()) {
            
           bot().execute(new RestrictChatMember(msg.chatId(),u.id())
           .canSendMessages(true)
           .canSendMediaMessages(true)
           .canSendOtherMessages(false)
           .canAddWebPagePreviews(true));
            
           }
        }
        
        return super.onGroupMsg(user,msg,superGroup);
    }
    
    
    
}
