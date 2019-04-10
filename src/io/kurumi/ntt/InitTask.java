package io.kurumi.ntt;

import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import java.util.Iterator;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.twitter.track.FTTask;
import io.kurumi.ntt.twitter.track.UTTask;
import io.kurumi.ntt.funcs.Backup;
import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.db.BotDB;

public class InitTask extends Thread {

    public void run() {

        BotLog.info("开始初始化BOT...");

        for (UserData user : BotDB.userDataIterable()) {

            if (!TAuth.exists(user.id)) continue;
            
            System.out.println("init task : " +user.userName());

            TAuth auth = TAuth.get(user.id);

            if (!user.contactable()) {

                TAuth.auth.remove(user.id.toString());

            } else if (!TAuth.avilable(user.id)) {

                new Send(user.id,"对不起！但是乃的认证 " + auth.getFormatedNameHtml() + " 已无法访问 移除了！ Σ( ﾟω / ").html().exec();

                TAuth.auth.remove(user.id.toString());

                BotLog.info("用户 " + user.userName() + " 的Twitter认证 " + auth.getFormatedName() + " 失效.");
				
            } 

        }

        TAuth.saveAll();

        BotLog.info("初始化 完成 :)");

        FTTask.start();
        UTTask.start();
        Backup.AutoBackupTask.INSTANCE.start();

    }

}
