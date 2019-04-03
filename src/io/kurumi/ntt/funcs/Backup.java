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

public class Backup extends Fragment {

    public static Backup INSTANCE = new Backup();
    
    @Override
    public boolean onNPM(UserData user,Msg msg) {
        
        if (!msg.isCommand()) return false;
        
        if (!"backup".equals(msg.command())) return false;
        
        if (!user.isDeveloper()) {
            
            msg.send("无权限").exec();
            
            return true;
            
        }
        
        File zip = ZipUtil.zip("./data","./cache/data.zip");

        msg.sendFile(zip);
        
        FileUtil.del(zip);
        
        return true;
        
   }
   
    public static class AutoBackupTask extends TimerTask {

        public static  AutoBackupTask INSTANCE = new AutoBackupTask();
        
        Timer timer;
        
        public void start() {

            stop();

            timer = new Timer("NTT Data Backup Task");
            timer.scheduleAtFixedRate(this,new Date(),60 * 60 * 1000);

        }

        public void stop() {

            if (timer != null) timer.cancel();

        }
        
        
        @Override
        public void run() {
            
            File zip = ZipUtil.zip("./data","./cache/data.zip");

            Launcher.INSTANCE.sendFile(Env.DEVELOPER_ID,zip);

            FileUtil.del(zip);
            
        }
        
       
       
       
   }

}
