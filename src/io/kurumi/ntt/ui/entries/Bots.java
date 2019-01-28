package io.kurumi.ntt.ui.entries;

import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.bots.*;
import com.pengrad.telegrambot.model.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.bots.template.*;
import io.kurumi.ntt.ui.confs.*;
import cn.hutool.json.*;
import cn.hutool.log.*;
import com.pengrad.telegrambot.response.*;
import java.util.*;

public class Bots {

    public static final String MAIN = "bots|main";

    public static final String NEW_BOT = "bots|new";
    public static final String MANAGE_BOT = "bots|manage";
    public static final String START_BOT = "bots|start";
    public static final String STOP_BOT = "bots|stop";
    
    public static final String INPUT_NAME = "bots|input_name";

    public static final String POINT_INPUT_NAME = INPUT_NAME;

    public static AbsResuest onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case MAIN : return main(userData, obj.msg(), false);
                case NEW_BOT : return chooseType(userData, obj);
                case INPUT_NAME : return inputName(userData, obj);

                case MANAGE_BOT : 
                    
                    return manageBot(userData, obj);

                case BaseConf.CONF_CALLBACK :
                case BaseConf.CONF_BACK :

                return onConfCallback(userData, obj);
                
                case START_BOT : return startBot(userData,obj);
                
               case STOP_BOT : return stopBot(userData,obj);

        }

        return obj.reply().alert("没有那样的BOT主控制指针 : " + obj.getPoint());

    }

    private static AbsResuest startBot(UserData userData, DataObject obj) {
        
        AbsResuest resp = obj.getBot(userData).start(obj);

        manageBot(userData,obj).exec();
        
        return resp;
        
    }
    
    private static AbsResuest stopBot(UserData userData, DataObject obj) {

         obj.getBot(userData).interrupt();
         
         manageBot(userData,obj).exec();
         
         return obj.reply().text("Bot已停止...");

    }

    public static AbsResuest onConfInput(UserData userData, Message message) {

        return userData.point.getBot(userData).root.processInput(userData, message);

    }

    public static AbsResuest main(final UserData userData, Message message, Boolean send) {

        AbsSendMsg msg;

        String[] mainMsg = new String[] {
            
            "这是Bot菜单！可以创建自己的Bot ⊙∀⊙","",
            
            
        };
        
        if (send) msg = new SendMsg(message.chat(), mainMsg);
        else msg = new EditMsg(message,mainMsg);

        msg.singleLineButton("<< 返回主页", MainUI.BACK_TO_MAIN);

        msg.singleLineButton("新建Bot (◦˙▽˙◦)", NEW_BOT);

        for (UserBot bot : userData.bots) {

            msg.singleLineButton("管理 " + bot.name + " 「" + bot.type() + "」", MANAGE_BOT, bot);

        }

        return msg;

    }

    public static AbsResuest manageBot(final UserData userData, DataObject obj) {

        final UserBot bot = obj.getBot(userData);

        if (bot == null) return obj.reply().alert("不存在的Bot ...");

        if (bot.root == null) {

            bot.root = new ConfRoot(bot) {

                @Override
                public void refresh(DataObject obj) {

                    onConfCallback(userData, obj);

                }

            };

        }


        return new EditMsg(obj.msg(), "管理 " + bot.type() + " 「" + bot.name + "」") {{

                singleLineButton("<< 返回Bot菜单", MAIN);

                bot.root.applySettings(this);
                
                if (bot.enable) {
                    
                    singleLineButton("停止Bot", STOP_BOT,bot);
                    
                } else {
                    
                    singleLineButton("启动Bot", START_BOT,bot);
                    
                }
                
                

            }};
            
    //    BaseResponse resp = exec.exec();
        
      //  return obj.reply().alert(resp.errorCode() + " : " + resp.description());

    }   

    public static AbsResuest onConfCallback(UserData userData, DataObject obj) {

        UserBot bot = obj.getBot(userData);

        switch (obj.getPoint()) {

                case BaseConf.CONF_CALLBACK : return bot.root.onCallback(userData, obj);
                case BaseConf.CONF_BACK : 

                String key = obj.getStr("bk");
                String index = obj.getStr("bi");

                if (key != null && index == null) {

                    DataObject backTo = new DataObject(BaseConf.CONF_CALLBACK);
                    
                    backTo.put("k",key);
                    backTo.setindex(index);
                    
                    backTo.query = obj.query;

                    return onCallback(userData, backTo);

                } else {

                    return manageBot(userData, obj);

                }

        }

        return obj.reply().alert("非法的设置回调中心指针 : " + obj.getPoint());

    }

    public static AbsResuest chooseType(UserData userData, DataObject obj) {

        return new EditMsg(obj.msg(), "选择Bot类型 (●'◡'●)ﾉ") {{

                singleLineButton("取消添加", MAIN);

                singleLineButton(SeeYouNextTimeBot.TYPE, INPUT_NAME, SeeYouNextTimeBot.TYPE);
                singleLineButton(TwitterReFoBot.TYPE, INPUT_NAME, TwitterReFoBot.TYPE);
                
            }};

    }

    public static AbsResuest inputName(UserData userData, DataObject obj) {

        obj.deleteMsg();

        userData.point = new DataObject();
        userData.point.setPoint(POINT_INPUT_NAME);
        userData.point.setindex(obj.getIndex());

        userData.save();

        return obj.send("请输入Bot的标识 : (名称、随意、唯一)", "", "取消新建使用 /cancel");

    }

    public static AbsResuest onInputName(UserData userData, Message msg) {

        if (StrUtil.isBlank(msg.text())) {

            return new SendMsg(msg.chat(), "无效的BotName (つд⊂)", "请重新输入 >_<", "", "取消新建使用 /cancel");

        }
        
        UserBot bot = UserBot.create(userData.point.getIndex(), userData, msg.text().trim());

        userData.point = null;

        userData.bots.add(bot);

        userData.save();

        return main(userData, msg, true);



    }


}
