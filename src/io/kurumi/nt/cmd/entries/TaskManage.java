package io.kurumi.nt.cmd.entries;

import io.kurumi.nt.*;
import io.kurumi.nt.cmd.*;
import io.kurumi.nt.twitter.*;
import twitter4j.*;

public class TaskManage extends NTBaseCmd {

    public static void apply(final NTUser user, final NTMenu menu) {


        menu.item(new NTMenu.Item("流任务") {

                @Override
                public boolean run() {

                    startTask(user);

                    return false;

                }

            });

    }


    private static void startTask(NTUser user) {
        
        TwiAccount acc = UserManage.chooseAccount(user);
        
        UserList specialList = UserListManage.chooseUserList(acc);
        
        try {
            
            printSplitLine();
            
            println("正在开启流任务...");
            
            StreamTask task = new StreamTask(acc, specialList);
            
            task.run();
            
            println("启动成功 输入 stop 以停止任务...");
            
            printSplitLine();
            
            while(!input().equals("stop")) {}
            
            task.stop();
            
        } catch (TwitterException e) {
            
            e.printStackTrace();
            
            printSplitLine();
            
            println("启动失败");
            
            pause();
            
        }

    }


}
