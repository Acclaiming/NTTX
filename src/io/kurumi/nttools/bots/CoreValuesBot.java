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

public class CoreValuesBot extends Fragment {

    public CoreValuesBot(MainFragment main) { super(main); }
    
    @Override
    public String name() {
        
        return "CoreValuesBot";
        
    }

    static Encoder encoder = new Encoder(Encoder.coreValus);
    
    @Override
    public void processInlineQuery(UserData user, InlineQuery inlineQuery) {

        InlineQueryResultArticle result = new InlineQueryResultArticle(inlineQuery.id(), "编码完成", new InputTextMessageContent(encoder.encode(inlineQuery.query())));

        bot.execute(new AnswerInlineQuery(inlineQuery.id(),result));
        
        
    }

    @Override
    public void processChosenInlineQueryResult(UserData user, InlineQuery inlineQuery) {
        
       
        
    }

}
