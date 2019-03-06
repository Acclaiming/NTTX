package io.kurumi.ntt.db;

import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.CData;

public class UserPoint {

    public static final String KEY = "NTT_USER_POINT";

    public static boolean exists(UserData user) {

        if (user.isBot) return false;
        
        return BotDB.exists(KEY,user.id.toString());

    }

    public static void set(UserData user, CData data) {

        BotDB.set(KEY, user.idStr, data.toString());

    }

    public static CData get(UserData user) {

        String data = BotDB.get(KEY, user.idStr);

        if (data == null) {

            BotLog.warnWithStack("未检查的取用户指针 : 内容为空");

            return null;

        }

        return new CData(data);

    }

    public static void remove(UserData user) {

        BotDB.set(KEY, user.idStr,null);

    }

}
