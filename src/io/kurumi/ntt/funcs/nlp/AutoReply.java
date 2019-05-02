package io.kurumi.ntt.funcs.nlp;

import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import io.kurumi.ntt.utils.TentcentNlp;
import java.util.Random;
import cn.hutool.core.util.RandomUtil;
import java.sql.Struct;
import cn.hutool.core.util.StrUtil;

public class AutoReply extends Function {

    public static AutoReply INSTANCE = new AutoReply();
    
    public static JSONArray disable = LocalData.getJSONArray("data","disable_action",true);

    @Override
    public void functions(LinkedList<String> names) {

        names.add("reply");

    }

    @Override
    public int target() {

        return Group;

    }

    @Override
    public boolean onGroup(UserData user,Msg msg) {
        
        if (!msg.hasText() || msg.isCommand() || disable.contains(msg.chatId().longValue())) return false;
        
        String text = msg.text();
        
        if (msg.text().contains("@NTToolsBot") || (msg.isReply() && msg.replyTo().from().id.equals(origin.me.id())) || ((TentcentNlp.nlpTextpolar(text) >= 0) && RandomUtil.randomInt(0, 31) == 9)) {
            
            String reply = TentcentNlp.nlpTextchat(((Long)(user.id < 0 ? user.id * -1 : user.id)).toString(),text);
            
            msg.reply(reply).exec();
            
        }
        
        return false;
        
    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (NTT.checkGroupAdmin(msg)) return;

        if (params.length == 1 && "off".equals(params[0])) {

            if (disable.contains(msg.chatId().longValue())) {

                msg.send("无需重复关闭 ~").exec();

            } else {

                disable.add(msg.chatId());

                LocalData.setJSONArray("daat","disable_action",disable);

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (!disable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                disable.remove(msg.chatId());

                LocalData.setJSONArray("daat","disable_action",disable);

                msg.send("已开启 ~").exec();

            }

        }


    }
    
    
}
