package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.Env;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.util.LinkedList;
import java.io.FileFilter;
import java.util.List;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.CharsetUtil;
import io.kurumi.ntt.utils.BotLog;

public class Maven extends Fragment {

    public static Maven INSTANCE = new Maven();
    
    @Override
    public boolean onNPM(UserData user,Msg msg) {
        
        if (!msg.isCommand()) return false;
        
        switch (msg.command()) {
            
            case "mvn" : mvn(user,msg);break;
            
            default : return false;
            
        }
        
        return true;
        
    }
    
   void mvn(UserData user,Msg msg) {
       
       String cmd = "mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get \\\n" + 
           "-DrepoUrl=http://central.maven.org/maven2/ \\\n" +
       "-Dartifact=" + msg.params()[0] +  ":jar \\\n" + 
       "-DoutputDirectory=" + Env.CACHE_DIR.getPath();
       
       try {
           
           msg.send("执行Maven下载...").exec();
           
           Process process = RuntimeUtil.exec(cmd);

           if (process.waitFor() != 0) {
               
               msg.send("下载出错...").exec();
               
               return;
               
           }
           
           File root = new File(Env.CACHE_DIR,".m2/repository");
           
           msg.send("读取所有Jar...").exec();

           List<File> allFiles = FileUtil.loopFiles(root,new FileFilter() {

                   @Override
                   public boolean accept(File file) {
                       
                       return file.getName().endsWith(".jar");
                       
                   }
               });
       
               
          msg.send("正在合并 " + allFiles.size() + " 个jar...").exec();
           
          File cacheDir = new File(Env.CACHE_DIR,"maven/" + UUID.randomUUID().toString());
          
          for (File jar : allFiles) {
          
         ZipUtil.unzip(jar,cacheDir);
         
         }
         
         msg.send("正在打包...").exec();
          
        
           File outJar =  ZipUtil.zip(cacheDir);
           
           
           msg.send("正在发送... 这可能需要几分钟的时间...").exec();
           
           msg.sendFile(outJar);
           
           FileUtil.del(new File(Env.CACHE_DIR,".m2"));
           FileUtil.del(cacheDir);
           FileUtil.del(outJar);
           
       } catch (InterruptedException e) {
           
           BotLog.error("maven error",e);
           
       }

   }
   
    
    
}
