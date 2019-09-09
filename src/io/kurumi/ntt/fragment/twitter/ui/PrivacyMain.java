package io.kurumi.ntt.fragment.twitter.ui;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class PrivacyMain extends Fragment {

    public static final String POINT_PRIVACY = "twi_pri";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_PRIVACY);

    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

            callback.invalidQuery();

            return;

        }

        long accountId = NumberUtil.parseLong(params[0]);

        TAuth account = TAuth.getById(accountId);

        if (account == null) {

            callback.alert("无效的账号 .");

            callback.delete();

            return;

        }


        if (POINT_PRIVACY.equals(point)) {

            privacyMain(user, callback, account);

        }

    }

    void privacyMain(UserData user, Callback callback, TAuth account) {

        String message = "隐私模式设定选单 : [ " + account.archive().name + " ]";

        ButtonMarkup buttons = new ButtonMarkup();


    }


}
