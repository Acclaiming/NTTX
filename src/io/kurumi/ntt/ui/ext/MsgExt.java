package io.kurumi.ntt.ui.ext;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import java.util.*;
import io.kurumi.ntt.ui.*;

public class MsgExt {

    public static boolean isCommand(Message msg) {

        if (msg.text() == null) return false;
        
        return msg.text().startsWith("/");

    }

    public static String getCommandName(Message msg) {

        if (msg.text() == null) return null;
        
        if (!msg.text().contains("/")) return null;

        String body = StrUtil.subAfter(msg.text(), "/", false);

        if (body.contains(" ")) {

            return StrUtil.subBefore(body, " ", false);

        } else if (body.contains("@")) {

            return StrUtil.subBefore(body, "@", false);

        } else {

            return body;

        }

    }

    public static String[] NO_PARAMS = new String[0];

    public static String[] getCommandParms(Message msg) {

        if (!msg.text().contains("/")) return null;

        String body = StrUtil.subAfter(msg.text(), "/", false);

        if (body.contains(" ")) {

            return StrUtil.subAfter(body, " ", false).split(" ");

        } else if (body.contains("@")) {

            return ("@" + StrUtil.subAfter(body, "@", false)).split(" ");

        } else {

            return NO_PARAMS;

        }

    }

    public static void delete(Message msg) {
        
        Constants.bot.execute(new DeleteMessage(msg.chat().id(), msg.messageId()));

    }

}
