package io.kurumi.ntt.db;

import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.CData;
import cn.hutool.json.*;

public class UserPoint {

    public static final String KEY = "data/users/points";

    public static void set(UserData user, CData data) {

        BotDB.sNC(KEY, user.idStr, data);

    }

    public static CData get(UserData user) {

        JSONObject data = BotDB.gNC(KEY, user.idStr);

        if (data == null) {

            return null;

        }

        return new CData(data);

    }

    public static void remove(UserData user) {

        BotDB.set(KEY, user.idStr,null);

    }

}
