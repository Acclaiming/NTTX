package io.kurumi.nttools;

import io.kurumi.nttools.fragments.*;
import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import io.kurumi.nttools.server.*;
import twitter4j.conf.*;
import java.io.*;
import io.kurumi.nttools.bots.CoreValuesBot;

public class Launcher {
    
    public static void main(String[] args) {
        
        MainFragment mainBot = new MainFragment(new File("./data"));
        
        CoreValuesBot coreValuesBot = new CoreValuesBot(mainBot);
        
        if (mainBot.token == null) {
            
            new Setup(mainBot).addFregment(coreValuesBot).start();
            
        }
        
        coreValuesBot.setWebHook();
        
        mainBot.startGetUpdates();
        
        try {
            
            BotServer.INSTANCE = new BotServer(mainBot);
            
            BotServer.INSTANCE.start();
            
        } catch (IOException exc) {
            
            exc.printStackTrace();
            
        }

    }
    
}
