package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.Env;
import cn.hutool.core.util.ZipUtil;
import java.io.File;

public class Backup extends Fragment {

    public static Backup INSTANCE = new Backup();
    
    @Override
    public boolean onNPM(UserData user,Msg msg) {
        
        if (!msg.isCommand()) return false;
        
        if (!"backup".equals(msg.command())) return false;
        
        if (!Env.FOUNDER.equals(user.userName)) {
            
            msg.send("无权限").exec();
            
            return true;
            
        }
        
        File zip = ZipUtil.zip("./data","./cache/data.zip");

        msg.sendFile(zip);
        
        return true;
        
   }
   
    
}
