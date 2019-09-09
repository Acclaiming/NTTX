package com.pengrad.telegrambot;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Update;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * stas
 * 11/1/15.
 */
public class BotUtils {

    private static Gson gson = new Gson();

    public static Update parseUpdate(String update) {
        Update obj = gson.fromJson(update, Update.class);
        obj.json = update;
        return obj;
    }

    static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

}
