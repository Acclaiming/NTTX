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
import io.kurumi.ntt.twitter.stream.SubTask;

public class InitTask extends Thread {

    public void run() {

        BotLog.info("初始化 完成 :)");

        FTTask.start();
        UTTask.start();
        SubTask.start();
        Backup.AutoBackupTask.INSTANCE.start();

    }

}
