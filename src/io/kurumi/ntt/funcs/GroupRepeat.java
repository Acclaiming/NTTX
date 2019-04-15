package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import com.pengrad.telegrambot.model.*;
import cn.hutool.core.util.*;

public class GroupRepeat extends Fragment {

    public static GroupRepeat INSTANCE = new GroupRepeat();

    public HashMap<Long,LinkedList<Msg>> msgs = new HashMap<>();

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        if (msg.isCommand()) {

			msgs.remove(msg.chatId());

		} else {

			LinkedList<Msg> history = msgs.get(msg.chatId());

			if (history == null) {

				history = new LinkedList<>();

				history.add(msg);

			} else {

				Msg last = history.getLast();

				if (msg.hasText() && last.hasText() && msg.text().equals(last.text())) {

					history.add(msg);

				} else if (msg.message().sticker() != null && last.message().sticker() != null && msg.message().sticker().equals(last.message().sticker())) {

					history.add(msg);

				} else {

					history.clear();

					history.add(msg);

				}

				if (parseUsers(history) == 3) {

					if (last.text() != null) 

						msg.send(last.text()).replyTo(last.replyTo()).exec();


				} else {

					msg.sendSticker(last.message().sticker().fileId());

				}

				return false;

			}

			if (RandomUtil.randomInt(0,40) == 9) {

				if (msg.text() != null) {

					msg.send(msg.text()).replyTo(msg.replyTo()).exec();

				} else if (msg.message().sticker() != null) {

					msg.sendSticker(msg.message().sticker().fileId());

				}

			}

		}


        return false;

    }

	int parseUsers(LinkedList<Msg> msgs) {

		LinkedHashSet<UserData> users = new LinkedHashSet<>();

		for (Msg msg : msgs) {

			users.add(msg.from());

		}

		return users.size();

	}

}
