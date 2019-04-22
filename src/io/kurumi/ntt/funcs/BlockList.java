package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.*;
import com.pengrad.telegrambot.request.*;
import cn.hutool.core.util.*;

public class BlockList extends Fragment {

    public static BlockList INSTANCE = new BlockList();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {
        
        switch (T.checkCommand(msg)) {
            
            case "bl" : pullBlockList(user,msg);break;
            
            default: return false;
            
        }
        
        return true;
        
    }

    void pullBlockList(UserData user,Msg msg) {
      
        if (T.checkUserNonAuth(user,msg)) return;
        
        msg.sendTyping();
        
        try {
            
            long[] ids = TApi.getAllBlockIDs(TAuth.get(user.id).createApi());

            msg.sendUpdatingFile();
            
            bot().execute(new SendDocument(msg.chatId(),ArrayUtil.join(ids,"\n")).fileName(TAuth.get(user.id).accountId  + "-" + System.currentTimeMillis() + ".csv"));
            
        } catch (TwitterException e) {
            
            msg.send("拉取失败 (" + e.getErrorCode() + ")... 你的认证可能失效或者到达了API调用上限。").exec();
            
        }

    }
    
}
