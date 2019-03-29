package io.kurumi.ntt;

import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import java.util.Iterator;
import io.kurumi.ntt.utils.BotLog;

public class InitTask extends Thread {
    
    public void run() {
        
        Launcher.initIng.set(true);
        
        BotLog.info("开始初始化BOT...");
        
        Iterator<Long> iter = UserData.INSTANCE.idList.iterator();

        while (iter.hasNext()) {

            long id = iter.next();

            UserData user = UserData.INSTANCE.getNoCache(id);

            SendResponse resp = new Send(user.id,"testIsBlockedBot").sync();

            if (!resp.isOk()) {

            //    iter.remove();

           //     UserData.INSTANCE.delObj(user);

                BotLog.info("用户 " + user.userName() + " 已停用BOT");
                
                continue;

            } else {

                Launcher.INSTANCE.bot().execute(new DeleteMessage(user.id,resp.message().messageId()));

            }

            if (!TAuth.exists(user)) continue;

            TAuth auth = TAuth.get(user);

            if (!auth.refresh()) {

                new Send(user.id,"对不起！但是乃的账号 " + auth.getFormatedNameHtml() + " 无法访问 已移除！ Σ( ﾟω / ").html().exec();

                TAuth.auth.remove(user.idStr);
                
                BotLog.info("用户 " + user.userName() + " 的Twitter认证 " + auth.getFormatedName() + " 失效.");
                
                
            }

        }
        
        TAuth.saveAll();
        
        BotLog.info("初始化 完成 :)");
        
        BotLog.info("BOT 已正常运行 :)");
        
        Launcher.initIng.set(false);

    }
    
}
