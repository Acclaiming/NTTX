package io.kurumi.ntt;

import io.kurumi.ntt.ui.ext.*;
import java.util.*;

public class Setup {

    static Data data = Constants.data;

    static Scanner session = new Scanner(System.in);

    public static void start() {

        System.out.println("正在开始初始化...");

        System.out.println();

        data.botToken = loopInput("BotToken");

        System.out.println();

        System.out.println("要使用认证和消息回调服务器吗？\n您必须反向代理本地端口到输入的域名");

        System.out.print("y / N : ");

        if (data.useServer = confirm()) {

            data.serverPort = loopInputInt("本地端口");
            data.serverDomain = loopInput("域名");
         //   data.authServerEnableSSL = loopInputBoolean();
            
        }
        
        data.save();

    }

    static String loopInput(String msg) {

        System.out.print("请输入" + msg + " : ");

        do {

            String content = session.next();

            if (defaultConfirm()) {

                return content;

            } else {
                
                System.out.print("请重新输入 : ");
                
            }

        } while(true);

    }

    static int loopInputInt(String msg) {

        System.out.print("输入" + msg + " : ");

        do {

            String str = session.next();

            try {

                int i = Integer.parseInt(str);

                if (defaultConfirm()) {

                    return i;
                    
                } else {

                    System.out.print("请重新输入 :");

                }

            } catch (Exception ex) {

                System.out.print("格式错误 请重新输入 :");

            }

        } while(true);

    }

    static boolean defaultConfirm() {

        System.out.print("确认吗 ？ y / N : ");

        return confirm();

    }

    static boolean confirm() {

        return "y".equals(session.next().toLowerCase());

    }

}
