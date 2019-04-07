package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import java.util.HashMap;
import com.pengrad.telegrambot.request.SendSticker;
import io.kurumi.ntt.utils.BotLog;

public class GroupRepeat extends Fragment {

    public static GroupRepeat INSTANCE = new GroupRepeat();

    public HashMap<Long,Msg> last = new HashMap<>();
    public HashMap<Long,Integer> count = new HashMap<>();

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        if (!msg.isCommand()) return false;

        Msg lastMsg = last.get(msg.chatId());

        Integer repeatCount = count.get(msg.chatId());

        if (repeatCount == null) repeatCount = 0;

        if (lastMsg != null && msg.text() != null && msg.text().equals(lastMsg.text())) {

            repeatCount ++;

            if (repeatCount == 1) {

                msg.send(msg.text()).exec();

                // repeatCount = 0;

                BotLog.debug("已处理群 " + msg.message().chat().title() + " 复读 : " + msg.text());

                // last.remove(msg.chatId());

            } else {
                
                repeatCount = 0;

                last.put(msg.chatId(),msg);

                //   BotLog.debug("已记录群 " + msg.message().chat().title() + " 复读 : " + msg.text());

            }

        } else if (lastMsg != null && msg.message().sticker() != null && msg.message().sticker().equals(lastMsg.message().sticker())) {

            repeatCount ++;

            if (repeatCount == 1) {

                bot().execute(new SendSticker(msg.chatId(),msg.message().sticker().fileId()));

                // repeatCount = 0;

                BotLog.debug("已处理群 " + msg.message().chat().title() + " 表情包 : 「" + msg.message().sticker().emoji() + " 从 " + msg.message().sticker().setName() + "」");

                // last.remove(msg.chatId());

            } else {

                last.put(msg.chatId(),msg);

                //     BotLog.debug("已记录群 " + msg.message().chat().title() + " 复读 : " + msg.text());

            }

        } else {
            
            repeatCount = 0;

            last.put(msg.chatId(),msg);

        }


        count.put(msg.chatId(),repeatCount);


        

        return false;

    }

}
