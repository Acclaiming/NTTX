package io.kurumi.ntt.db;

import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.CData;

public class UserPoint {

    public static final String KEY = "NTT_USER_POINT";

    public static void set(UserData user, CData data) {

        BotDB.sNC(KEY, user.idStr, data.toString());

    }

    public static CData get(UserData user) {

        String data = BotDB.gNC(KEY, user.idStr);

        if (data == null) {

            return null;

        }

        return new CData(data);

    }

    public static void remove(UserData user) {

        BotDB.set(KEY, user.idStr,null);

    }

}
