package io.kurumi.ntt.cli;

import org.apache.commons.cli.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import java.util.*;
import com.pengrad.telegrambot.request.*;
import cn.hutool.core.util.*;

public class TopLevel extends CliUI {

    public static TopLevel INSTANCE = new TopLevel();
    
    public static final String COMMAND_START = "start";
    public static final String COMMAND_HELP = "help";
    public static final String COMMAND_RAND = "rand";

    @Override
    protected String cmdLineSyntax() {
        return "NTTBot";
    }

    @Override
    protected void applyCommands(HashMap<String, Options> commands) {

        commands.put("rand", new Options().addRequiredOption("m", "msg", true, "发送的信息"));

    }

    @Override
    protected void onCommand(UserData userData, DataObject obj, String commandName, CommandLine cmd) {

        switch (commandName) {

                case COMMAND_START : COMMAND_HELP :

                help(userData, obj, cmd);return;

                case COMMAND_RAND : 

                rand(userData, obj, cmd);return;
                
        }
        
        obj.send("嘤嘤嘤").exec();

    }

    public void help(UserData userData, DataObject obj, CommandLine cmd) {

        obj.send(printHelp()).exec();

    }

    private Random random = new Random();

    public void rand(UserData userData, DataObject obj, CommandLine cmd) {

        if (!cmd.hasOption("msg")) { obj.send(printHelp(COMMAND_RAND)).exec();return; }

        long lastSend = userData.ext.getLong("lastRandSend", -1L);

        if (System.currentTimeMillis() - lastSend < 1 * 60 * 60 * 1000) {

            if (!userData.isAdmin) {

                obj.send("一小时只能发一条哦！ Σ(ﾟ∀ﾟﾉ)ﾉ").exec();

                return;

            }

        }

        LinkedList<UserData> users = Constants.data.getUsers();

        users.remove(userData);

        if (users.size() == 0) {

            obj.send("发送失败 只有乃在用这个Bot！").exec();
            
            return;

        }

        UserData user = users.get(random.nextInt(users.size()));

        user.send("随机消息来自 : @" + userData.userName,ArrayUtil.join(cmd.getOptionValues("msg")," ")).exec();
        
        obj.reply("发送成功！ 已发给 : @" + user.userName).exec();

    }

}
