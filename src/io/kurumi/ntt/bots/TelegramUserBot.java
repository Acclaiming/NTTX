package io.kurumi.ntt.bots;

import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;
import cn.hutool.log.*;
import io.kurumi.ntt.ui.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;

public abstract class TelegramUserBot extends UserBot {

    public static final String UPDATE_TYPE_MESSAGE = "message";
    public static final String UPDATE_TYPE_EDITED_MESSAGE = "edited_message";
    public static final String UPDATE_TYPE_CHANNEL_POST = "channel_post";
    public static final String UPDATE_TYPE_EDITED_CHANNEL_POST = "edited_channel_post";
    public static final String UPDATE_TYPE_INLINE_QUERY = "inline_query";
    public static final String UPDATE_TYPE_CHOSEN_INLINE_RESULT = "chosen_inline_result";
    public static final String UPDATE_TYPE_CALLBACK_QUERY = "callback_query";
    public static final String UPDATE_TYPE_SKIPPING_QUERY = "shipping_query";
    public static final String UPDATE_TYPE_PRE_CHECKOUT_QUERY = "pre_checkout_query";

    public BotConf botTokenConf = new BotConf(this, "Bot Api Token", "bot_token");
    
    public TelegramBot bot;
    public User thisUser;


    public TelegramUserBot(UserData owner, String name) {
        super(owner, name);
    }

    @Override
    public void confs(ConfRoot confs) {

        confs.add(botTokenConf);

    }

    public Log log = StaticLog.get(name);

    @Override
    public void startAtBackground() {
        

        String token = botTokenConf.get();

        if (bot == null && token != null) {

            bot = new TelegramBot(token);

            GetMeResponse resp = bot.execute(new GetMe());

            if (!resp.isOk()) {

                bot = null;

                interrupt();

                return;

            }

            thisUser = resp.user();

            String callbackUrl = "https://" + Constants.authandwebhook.domain + "/" + token;

            BaseResponse init = bot.execute(new SetWebhook().url(callbackUrl).allowedUpdates(allowUpdates()));

            if (!init.isOk()) {

                bot = null;

                interrupt();

                return;

            }

            BotControl.telegramBots.put(token,this);

            enable = true;

        }
        
        
   }
    
    @Override
    public AbsResuest start(DataObject obj) {

        String token = botTokenConf.get();

        if (bot != null) {

            return obj.reply().alert("Bot already started ...");

        } else if (token == null) { 

            interrupt();

            return obj.reply().alert("BotToken 未设置 >_<");

        }  else {

            bot = new TelegramBot(token);

            GetMeResponse resp = bot.execute(new GetMe());

            if (!resp.isOk()) {

                bot = null;

                interrupt();

                return obj.reply().alert("Bot init failed :" + resp.errorCode() + "\n\n" + resp.description() + "\n\n is your token invailed？ ");

            }

            thisUser = resp.user();

            String callbackUrl = "https://" + Constants.authandwebhook.domain + "/" + token;

            BaseResponse init = bot.execute(new SetWebhook().url(callbackUrl).allowedUpdates(allowUpdates()));

            if (!init.isOk()) {

                bot = null;

                interrupt();

                return obj.reply().alert("set webhook failed : " + init.errorCode() + "\n\n" + init.description() + "\n\n请截图联系 @HiedaNaKan 修复");

            }
            
            BotControl.telegramBots.put(token,this);
            
            enable = true;

            return obj.reply().text("Bot已经启动！");

        }

    }

    @Override
    public void interrupt() {

        super.interrupt();

        if (bot != null) {

            BaseResponse resp =bot.execute(new DeleteWebhook());

            if (!resp.isOk()) {

                log.error("delete webhook failed : " + resp.errorCode() + "\n\n" + resp.description());

            }
            
            

        }
        
        bot = null;
        
        enable = false;

    }

    public abstract String[] allowUpdates();
    public abstract AbsResuest processUpdate(Update update);

}
