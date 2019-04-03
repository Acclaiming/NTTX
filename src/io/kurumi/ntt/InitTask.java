package io.kurumi.ntt;

import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import java.util.Iterator;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.twitter.track.FollowerTrackTask;
import io.kurumi.ntt.twitter.track.UserTrackTask;
import io.kurumi.ntt.funcs.Backup;
import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.utils.Html;

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
        UserTrackTask.start();
        Backup.AutoBackupTask.INSTANCE.start();

        String current = getVersion();

        if (current == null) {

            BotLog.error("无法取得版本 :(");

        } else {

            String version = Env.getOrDefault("version",current);

            if (!current.equals(version)) {

               String msg = "NTT已更新 :)";

              // new Send(Env.DEVELOPER_ID,msg).html().exec();
               new Send(Env.GROUP,msg,showVersion()).exec();
               
            }
            
        }

        BotLog.info("BOT 已正常运行 :)");

    }

    public String getVersion() {

        return RuntimeUtil.execForStr("git rev-parse HEAD");

    }
    
    public String showVersion() {
        
        return RuntimeUtil.execForStr("git log -1");
        
    }

}
