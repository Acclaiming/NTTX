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
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.exceptions.UtilException;

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

        try {
            
            if (msg.params().length == 0) {
                
                msg.send("/mvn [o:a:v] <maven url>").exec();
                
                return;
                
            }
            
            String url = msg.params().length > 1 ? msg.params()[1] : "http://central.maven.org/maven2/";
            
            String cmd = "mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get " + 
                "-DrepoUrl= " + url + " " +
                "-Dartifact=" + msg.params()[0] +  ":jar ";

            msg.send("执行Maven下载...").exec();

            Process process = RuntimeUtil.exec(null,Env.CACHE_DIR,cmd);

            if (process.waitFor() != 0) {
                
                msg.send(RuntimeUtil.getErrorResult(process)).exec();
                
                return;
                
            }

            File root = new File(Env.CACHE_DIR,"~/.m2/repository");
            
            List<File> allFiles = FileUtil.loopFiles(root,new FileFilter() {

                    @Override
                    public boolean accept(File file) {

                        System.out.println(file.toString());
                        
                        return file.getName().endsWith(".jar");

                    }

                });

            msg.send("正在合并 " + allFiles.size() + " 个jar...").exec();

            File cacheDir = new File(Env.CACHE_DIR,"maven");

            for (File jar : allFiles) {

                ZipUtil.unzip(jar,cacheDir);

            }

            msg.send("正在打包...").exec();

            File outJar =  ZipUtil.zip(cacheDir);

            msg.send("正在发送... 这可能需要几分钟的时间...").exec();

            msg.sendFile(outJar);

           RuntimeUtil.exec("rm -rf ~/.m2");
            FileUtil.del(cacheDir);
            FileUtil.del(outJar);
            
        } catch (InterruptedException e) {}


    }



}
