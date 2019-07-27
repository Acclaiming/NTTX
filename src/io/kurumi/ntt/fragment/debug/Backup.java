package io.kurumi.ntt.fragment.debug;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.ZipUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.io.File;
import java.util.Date;
import java.util.TimerTask;

public class Backup extends Fragment {

    public static void start() {

        Date next = new Date();

		if (next.getHours() < 12) {

			next.setHours(12);

		} else {

			next.setDate(next.getDate() + 1);
			next.setHours(0);

		}

        next.setMinutes(0);
        next.setSeconds(0);

		BotFragment.mainTimer.scheduleAtFixedRate(AutoBackupTask.INSTANCE,next,12 * 60 * 60 * 1000);

    }

    static void backup(long chatId) {

        try {

            RuntimeUtil.exec(
				"mongodump",
				"-h",Env.getOrDefault("db_address","127.0.0.1") + ":" + Env.getOrDefault("db_port","27017"),
				"-d","NTTools",
				"-o",Env.DATA_DIR.getPath() + "/db"
            ).waitFor();

        } catch (InterruptedException e) {
        }

		File dest = new File(Env.CACHE_DIR,"data.zip");

		FileUtil.del(dest);

        File zip = ZipUtil.zip(Env.DATA_DIR.getPath(),dest.getPath());

        FileUtil.del(Env.DATA_DIR + "/db");

        Launcher.INSTANCE.sendFile(chatId,zip);


    }

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("backup");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

        backup(msg.chatId());

    }

    public static class AutoBackupTask extends TimerTask {

        public static AutoBackupTask INSTANCE = new AutoBackupTask();

        @Override
        public void run() {

            backup(Env.LOG_CHANNEL);

        }


    }

}
