package io.kurumi.nttools;

import io.kurumi.nttools.bots.*;
import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import io.kurumi.nttools.server.*;
import twitter4j.conf.*;
import java.io.*;

public class Launcher {
    
    public static Fragment mainBot;
    public static BotServer server;
    
    public static void main(String[] args) {
        
        mainBot = new MainFragment();
        
        mainBot.startGetUpdates();
        
        try {
            
            BotServer.INSTANCE.start();
            
        } catch (IOException exc) {
            
            exc.printStackTrace();
            
        }

    }
    
}
