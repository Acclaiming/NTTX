package io.kurumi.ntt.db;

import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.CData;

public class UserPoint {

    public static final String KEY = "NTT_USER_POINT";

    public static boolean exists(UserData user) {

        if (user.isBot) return false;
        
        return BotDB.jedis.hexists(KEY, user.id.toString());

    }

    public static void set(UserData user, CData data) {

        BotDB.jedis.hset(KEY, user.id.toString(), data.toString());

    }

    public static CData get(UserData user) {

        String data = BotDB.jedis.hget(KEY, user.id.toString());

        if (data == null) {

            BotLog.warnWithStack("未检查的取用户指针 : 内容为空");

            return null;

        }

        return new CData(data);

    }

    public static void remove(UserData user) {

        BotDB.jedis.hdel(KEY, user.id.toString());

    }

}
