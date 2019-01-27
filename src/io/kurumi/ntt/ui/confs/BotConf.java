package io.kurumi.ntt.ui.confs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.request.*;
import com.pengrad.telegrambot.model.*;
import java.util.concurrent.atomic.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;

public class BotConf extends StrConf {

    public BotConf(UserBot bot,String name,String key) {
        super(bot,name,key);
    }

    @Override
    public AbsResuest onMessage(Message msg, AtomicBoolean refresh) {
        
        GetMeResponse resp = new TelegramBot(msg.text()).execute(new GetMe());

        if (!resp.isOk()) {
            
            return new SendMsg(msg,"无效的BotToken 如果Token正常 请重试...\n 或使用 /cancel 退出");
            
        }
        
        return super.onMessage(msg, refresh);
    }
    
}
