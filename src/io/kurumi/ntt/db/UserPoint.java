package io.kurumi.ntt.db;

import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.CData;
import cn.hutool.json.*;

public class UserPoint {

    public static final String KEY = "data/users/points";

    public static void set(UserData user, CData data) {

        BotDB.setJSON(KEY, user.idStr, data);

    }

    public static CData get(UserData user) {

        JSONObject data = BotDB.getJSON(KEY, user.idStr,false);

        if (data == null) {

            return null;

        }

        return new CData(data);

    }


}
