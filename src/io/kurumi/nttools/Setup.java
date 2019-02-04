package io.kurumi.nttools;

import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import java.util.LinkedList;
import java.util.Scanner;

public class Setup {

    MainFragment main;
    LinkedList<Fragment> fragments = new LinkedList<>();

    public Setup(MainFragment main) {

        this.main = main;

        addFregment(main);

    }

    public Setup addFregment(Fragment bot) {

        fragments.add(bot);

        return this;

    }

    static Scanner session = new Scanner(System.in);

    public void start() {

        for (Fragment bot :  fragments) {

            if (!main.tokens.containsKey(bot.name())) {

                main.tokens.put(bot.name(), loopInput("BotToken for " + bot.name()));

            }

            bot.initBot();

        }

        if (main.serverPort == -1) {

            main.serverPort = loopInputInt("本地端口");

        }

        if (main.serverDomain == null) {

            main.serverDomain = loopInput("域名");

        }

        main.save();

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
