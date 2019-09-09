package io.kurumi.ntt.fragment.inline;

import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.StickerSet;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Query;

import java.util.HashMap;

public class ShowSticker extends Fragment {

    @Override
    public boolean query() {

        return true;

    }

    public static HashMap<Long, StickerSet> current = new HashMap<>();

    public static String PREFIX = "STICKER";

    @Override
    public void onQuery(UserData user, Query inlineQuery) {

        if (user == null || inlineQuery.text == null || !inlineQuery.text.startsWith(PREFIX)) return;

        if (current.containsKey(user.id)) {

            for (Sticker sticker : current.get(user.id).stickers()) {

                inlineQuery.sticker(sticker.fileId());

            }

            executeAsync(inlineQuery.update, inlineQuery.reply().cacheTime(0));

        } else if (inlineQuery.text != null && inlineQuery.text.length() > (PREFIX.length() + 1)) {

            String name = inlineQuery.text.substring(PREFIX.length() + 1).trim();

            final GetStickerSetResponse set = bot().execute(new GetStickerSet(name));

            if (!set.isOk()) {

                execute(inlineQuery.reply().cacheTime(114));

                return;

            }

            for (Sticker sticker : set.stickerSet().stickers()) {

                inlineQuery.sticker(sticker.fileId());

            }

            executeAsync(inlineQuery.reply().cacheTime(0));

        }

    }


}
