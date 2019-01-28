package io.kurumi.ntt.ui.confs;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.concurrent.atomic.*;

public class LongConf extends BaseConf<Long> {

    public LongConf(UserBot bot,String name,String key) {
        super(bot,name,key);
    }

    @Override
    public Long get() {

        return bot.data.getLong(key);

    }

    @Override
    public void applySetting(AbsSendMsg msg) {

        msg.singleLineButton(name + " : 「" + get() + "」",createQuery());

    }

    @Override
    public AbsResuest onCallback(DataObject obj, AtomicBoolean refresh) {

        bot.owner.point = createInputPoint();
        bot.owner.save();

        return new SendMsg(obj.chat(), "请输入新长整数 : (使用 /cancel 取消)");

    }

    @Override
    public AbsResuest onMessage(Message msg, AtomicBoolean back) {

        try {

            long value = Long.parseLong(msg.text());

            set(value);

            back.set(true);

            return new SendMsg(msg,name + "修改成功 ！");

        } catch (Exception exc) {

            return new SendMsg(msg, "不是一个有效的长整数！ 用 /cancel 取消更改");

        }

    }

}
