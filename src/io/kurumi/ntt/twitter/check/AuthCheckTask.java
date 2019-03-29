package io.kurumi.ntt.twitter.check;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import java.util.TimerTask;
import io.kurumi.ntt.Launcher;
import java.util.Iterator;
import com.pengrad.telegrambot.response.SendResponse;
import com.pengrad.telegrambot.request.DeleteMessage;

public class AuthCheckTask extends TimerTask {

    @Override
    public void run() {
     
        Iterator<Long> iter = UserData.INSTANCE.idList.iterator();
        
        while (iter.hasNext()) {
            
            long id = iter.next();
            
            UserData user = UserData.INSTANCE.getNoCache(id);

            SendResponse resp = new Send(user.id,"testIsBlocked").sync();

            if (!resp.isOk()) {
                
                iter.remove();

                UserData.INSTANCE.delObj(user);
                
                continue;
                
            } else {
                
                Launcher.INSTANCE.bot().execute(new DeleteMessage(user.id,resp.message().messageId()));
                
            }
            
            if (!TAuth.exists(user)) continue;
            
            TAuth auth = TAuth.get(user);
            
            if (!auth.refresh()) {
               
                
                
            }

        }
        
    }
    
}
