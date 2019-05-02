package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ReUtil;
import java.util.List;
import cn.hutool.core.util.StrUtil;

public class RiceCakeMeme extends Fragment {

    public static RiceCakeMeme INSTANCE = new RiceCakeMeme();

    @Override
    public boolean onGroup(UserData user,Msg msg) {

        if (ChineseAction.disable.contains(msg.chatId().longValue())) return false;
        
        if (!msg.hasText() || msg.isCommand()) return false;

        if (msg.text().startsWith("羡慕") && msg.text().endsWith("...")) {

            if (msg.isReply()) {

                String name = msg.replyTo().from().firstName;
                
                if (name.contains("的")) name = StrUtil.subAfter(name,"的",true);
                
                msg.reply("以后把" + name + "做成锅包肉吃...").html().exec();

            } else {
                
                String content = StrUtil.subBetween(msg.text(),"羡慕","...");
                
                msg.reply("以后把" + (content == null || content.isEmpty() ? user.name() : content) + "做成锅包肉吃...").html().exec();

            }

        }

        return false;

    }

}

