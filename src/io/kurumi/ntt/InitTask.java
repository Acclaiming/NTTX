package io.kurumi.ntt;

import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import java.util.Iterator;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.twitter.track.FollowerTrackTask;

public class InitTask extends Thread {

    public void run() {

        BotLog.info("开始初始化BOT...");

        Long[] ids = UserData.INSTANCE.idList.toArray(new Long[UserData.INSTANCE.idList.size()]);

        for (long id : ids) {

            UserData user = new UserData(UserData.INSTANCE.dirName,id);

            /*
            
            SendResponse resp = new Send(user.id,"testIsBlockedBot").disableNotification().sync();

            if (!resp.isOk()) {

                UserData.INSTANCE.delObj(user);

                BotLog.info("用户 " + user.userName() + " 已停用BOT");

                continue;

            } else {

                Launcher.INSTANCE.bot().execute(new DeleteMessage(user.id,resp.message().messageId()));

            }
            
            */

            if (!TAuth.exists(user)) continue;

            TAuth auth = TAuth.get(user);

            if (!auth.refresh()) {

                new Send(user.id,"对不起！但是乃的认证 " + auth.getFormatedNameHtml() + " 已无法访问 移除了！ Σ( ﾟω / ").html().exec();
                
                TAuth.auth.remove(user.idStr);

                BotLog.info("用户 " + user.userName() + " 的Twitter认证 " + auth.getFormatedName() + " 失效.");

            }

        }
        
        TAuth.saveAll();

        BotLog.info("初始化 完成 :)");
        
        FollowerTrackTask.start();

        BotLog.info("BOT 已正常运行 :)");

    }

}
