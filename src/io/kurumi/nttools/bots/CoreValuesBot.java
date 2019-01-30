package io.kurumi.nttools.bots;

import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import com.pengrad.telegrambot.model.InlineQuery;
import io.kurumi.nttools.utils.UserData;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultContact;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.model.request.InlineQueryResultVenue;
import com.pengrad.telegrambot.model.request.InputTextMessageContent;
import com.pengrad.telegrambot.model.request.InlineQueryResultGame;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import io.kurumi.nttools.utils.Encoder;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;

public class CoreValuesBot extends Fragment {

    public CoreValuesBot(MainFragment main) { super(main); }

    @Override
    public String name() {

        return "CoreValuesBot";

    }

    static Encoder encoder = new Encoder(Encoder.coreValus);

    @Override
    public void processInlineQuery(UserData user, InlineQuery inlineQuery) {

        if (!StrUtil.isBlank(inlineQuery.query())) {

            InlineQueryResultArticle result = new InlineQueryResultArticle(inlineQuery.id(), "编码完成 点这里发送 y( ˙ᴗ. ) ~", encoder.encode(inlineQuery.query()));

            bot.execute(new AnswerInlineQuery(inlineQuery.id(), result));

        }

    }

    @Override
    public void processPrivateMessage(UserData user, Message msg) {
        
        String text = encoder.decode(msg.text());

        if (text != null) {
            
            bot.execute(new SendMessage(msg.chat().id(),"解码完成 : \n" + text).replyToMessageId(msg.messageId()));
            
        } else {
            
            bot.execute(new SendMessage(msg.chat().id(),"解码失败 请直接发送需要解码的内容"));
            
        }
        
    }

}
