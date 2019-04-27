package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ReUtil;
import java.util.List;

public class RiceCakeMeme extends Fragment {

    public static RiceCakeMeme INSTANCE = new RiceCakeMeme();
    
    @Override
    public boolean onGroup(UserData user,Msg msg) {

        if (!msg.hasText() || msg.isCommand()) return false;

        List<String> match = ReUtil.findAllGroup0("羡慕(.*)\\.+",msg.text());

        if (match != null && !match.isEmpty()) {

            msg.reply("以后把" + match.get(0) + "做成锅包肉吃...").exec();

        } else if (msg.text().matches("羡慕\\.+")) {

            msg.reply("以后把你做成锅包肉吃...").exec();

        } else if (msg.text().contains("三花"))  {
            
            msg.reply("以后把你做成三花吃...").exec();
            
        }

        return false;

    }

}
