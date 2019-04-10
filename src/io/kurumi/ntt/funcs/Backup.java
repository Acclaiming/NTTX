package io.kurumi.ntt.funcs;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import io.kurumi.ntt.Launcher;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RuntimeUtil;

public class Backup extends Fragment {

    public static Backup INSTANCE = new Backup();

    @Override
    public boolean onNPM(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        if (!"backup".equals(msg.command())) return false;

        if (!user.developer()) {

            msg.send("无权限").exec();

            return true;

        }

        backup();

        return true;

    }

    public static class AutoBackupTask extends TimerTask {

        public static  AutoBackupTask INSTANCE = new AutoBackupTask();

        Timer timer;

        public void start() {

            stop();

            Date next = new Date();

			if (next.getHours() < 12) {

				next.setHours(12);

			} else {

				next.setDate(next.getDate() + 1);
				next.setHours(0);

			}

            next.setMinutes(0);
            next.setSeconds(0);

            timer = new Timer("NTT Data Backup Task");
            timer.scheduleAtFixedRate(this,next,12 * 60 * 60 * 1000);

        }

        public void stop() {

            if (timer != null) timer.cancel();

        }


        @Override
        public void run() {

            backup();

        }




    }

    static void backup() {

        try {

            RuntimeUtil.exec(
                "mongodump",
                "-h",Env.getOrDefault("db_address","127.0.0.1") + ":" + Env.getOrDefault("db_port","27017"),
                "-d","NTTools",
                "-o",Env.DATA_DIR.getPath() + "/db"
            ).waitFor();

        } catch (InterruptedException e) {}

        File zip = ZipUtil.zip(Env.DATA_DIR.getPath(),Env.CACHE_DIR.getPath() + "/data.zip");

        FileUtil.del(Env.DATA_DIR + "/db");

        Launcher.INSTANCE.sendFile(Env.DEVELOPER_ID,zip);

        FileUtil.del(zip);


    }

}
