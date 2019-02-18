package io.kurumi.ntt;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.UserData;
import java.io.File;
import cn.hutool.core.io.FileUtil;
import io.kurumi.ntt.db.Permission;

public class Recovery {
    
    public static void main(String[] args) {
        
        File data = new File("./data.zip");
        
        ZipUtil.unzip(data,new File("./cache/rec"));

        File[] ul = new File("./cache/rec/data/users").listFiles();

        if (ul != null) {

            for (File userDataFile : ul) {

                int userId = Integer.parseInt(StrUtil.subBefore(userDataFile.getName(), ".json", true));

                UserData user = UserData.get(userId);
                
                JSONObject obj = new JSONObject(FileUtil.readUtf8String(userDataFile));
                
                user.id = userId;
                
                user.userName = obj.getStr("user_name");
                
                user.firstName = obj.getStr("name");
                
                user.isBot = obj.get("is_bot");
                
                user.save();
                
                if (obj.getBool("is_admin",false)) {
                    
                    Permission.setPermission(userId,1);
                    
                }
                

            }

        }
        
    }
    
}
