package io.kurumi.ntt.fragment.inline;

import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.StickerSet;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Query;
import java.util.HashMap;

public class ShowSticker extends Fragment {
	
	public static HashMap<Long,StickerSet> current = new HashMap<>();

	@Override
	public void onQuery(UserData user,Query inlineQuery) {

		if (user == null || inlineQuery.text == null || !inlineQuery.text.startsWith("SM_CH")) return;

		if (current.containsKey(user.id)) {

			for (Sticker sticker : current.get(user.id).stickers()) {

				inlineQuery.sticker(sticker.fileId());

			}

			execute(inlineQuery.reply().cacheTime(0));

		} else {

			inlineQuery.article("(*σ´∀`)σ","(*σ´∀`)σ",null,null);

			execute(inlineQuery.reply().cacheTime(0));

		}

	}
	
	
}
