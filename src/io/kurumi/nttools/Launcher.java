package io.kurumi.nttools;

import io.kurumi.nttools.fragments.*;
import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import io.kurumi.nttools.server.*;
import twitter4j.conf.*;
import java.io.*;
import io.kurumi.nttools.bots.CoreValuesBot;

public class Launcher {

    public static boolean isAndroid; static {
       
        try {

            Class.forName("android.app.Activity");

            isAndroid = true;

        } catch (ClassNotFoundException e) {

            isAndroid = false;

        }

    }

    public static void main(String[] args) {
        
        File dataDir = new File("./data");
        
        if (isAndroid) {
            
            dataDir = new File("/sdcard/AppProjects/NTTools/data");
            
        } else {
            
            
            
        }
        
        NTTBot mainBot = new NTTBot(dataDir);

        CoreValuesBot coreValuesBot = new CoreValuesBot(mainBot);

        new Setup(mainBot)
            .addFregment(coreValuesBot)
            .start();

        mainBot.startGetUpdates();
        
        if (!isAndroid) {

            coreValuesBot.setWebHook();

            try {

                BotServer.INSTANCE = new BotServer(mainBot);

                BotServer.INSTANCE.start();

            } catch (IOException exc) {

                exc.printStackTrace();

            }


        }
        
        try {
            
            System.in.read();
            
        } catch (IOException e) {}

    }

}
