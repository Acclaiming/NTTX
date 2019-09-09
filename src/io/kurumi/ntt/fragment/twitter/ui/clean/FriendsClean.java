package io.kurumi.ntt.fragment.twitter.ui.clean;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;

public class FriendsClean extends Fragment {

    public static String POINT_BB = "twi_bb";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_BB);

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

        if (params.length == 1) {

            friendsCleanMain(user, callback, account);

            return;

        }


    }

    void friendsCleanMain(UserData user, Callback callback, TAuth account) {


    }
}
