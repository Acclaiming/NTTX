package io.kurumi.nttools.raffle;

import io.kurumi.nttools.timer.TimerTask;
import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.nttools.model.Callback;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.nttools.twitter.TwitterUI;

public class RaffleUI extends FragmentBase implements TimerTask  {

    public static final RaffleUI INSTANCE = new RaffleUI();

    private static final String POINT_ADD = "r|a";
    private static final String POINT_MY = "r|m";
    private static final String POINT_DEL = "r|d";
    private static final String POINT_OPEN = "r|o";

    private static final String POINT_CHOOSE_ACCOUNT = "r|c|a";
    private static final String POINT_INPUT_STATUS = "r|i|s";

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (user.point != null) {

            switch (user.point.getPoint()) {

                    default : return false;

            }

        } else if (!msg.isCommand()) return false;

        switch (msg.commandName()) {

                case "raffle" : sendMain(user, msg, true);break;

                default : return false;

        }

        return true;

    }

    private void sendMain(UserData user, Msg msg, boolean edit) {

        if (!edit) {

            deleteLastSend(user, msg, "raffle_ui");

        }

        String[] raffleMsg = new String[] {

            "你好呀！ 这是一个奇怪的Twitter抽奖功能 (｡>∀<｡)",

            "首先需要在 /twitter 认证账号呢 不然没法调用接口 >_<",

        };

        BaseResponse resp = sendOrEdit(msg, edit, raffleMsg).buttons(new ButtonMarkup() {{

                    newButtonLine("「 新建抽奖 」", POINT_ADD);
                    newButtonLine("「 我的抽奖 」", POINT_MY);
                    newButtonLine("「 删除抽奖 」", POINT_DEL);
                    newButtonLine("「 立即开奖 」", POINT_OPEN);

                }}).exec();

        saveLastSent(user, msg, "raffle_ui", resp);

    }

    private static String[] noAccount = new String[] {

        "还没有认证Twitter账号 🤔",
        "这个功能使用的TwitterApi需要用户上下文 (",
        "使用 /newTwitterAuth 认证",

    };


    public void add(UserData user, Callback callback) {

        if (user.twitterAccounts.isEmpty()) {

            callback.alert(noAccount);
            
            return;

        } else { 

            callback.confirm();

        }
        
        TwitterUI.INSTANCE.choseAccount(user,callback,cdata(POINT_CHOOSE_ACCOUNT));

    }
    
    public void onChooseAccount(UserData user,Msg msg) {
        
        user.point.setPoint(POINT_INPUT_STATUS);
        user.save();
        
        msg.send("好 现在输入抽奖名称 :");
        
    }

    @Override
    public boolean processCallbackQuery(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

                case POINT_ADD : add(user, callback);break;
                
                case POINT_CHOOSE_ACCOUNT : onChooseAccount(user,callback);break;

                default : return false;

        }

    }

    @Override
    public void run(MainFragment fragment) {
    }

}
