package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.RandomUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class GroupRepeat extends Fragment {

	@Override
	public boolean msg() {
		
		return true;
		
	}

    public HashMap<Long, LinkedList<Msg>> msgs = new HashMap<>();
	
    @Override
    public void onGroup(UserData user, Msg msg) {
		
        synchronized (msgs) {

            if (msg.isCommand()) {

                msgs.remove(msg.chatId());


            } else {

                LinkedList<Msg> history = msgs.get(msg.chatId());

                if (history == null) {

                    history = new LinkedList<>();

                    history.add(msg);

                    msgs.put(msg.chatId(), history);


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

                        if (last.text() != null) {

                            msg.send(last.text()).replyTo(last.replyTo()).exec();


                        } else {

                            msg.sendSticker(last.message().sticker().fileId());

                        }

                    }

                    msgs.put(msg.chatId(), history);

                    return;

                }

                msgs.put(msg.chatId(), history);

                if (RandomUtil.randomInt(0, 40) == 9) {

                    if (msg.text() != null) {

                        msg.send(msg.text()).replyTo(msg.replyTo()).exec();

                    } else if (msg.message().sticker() != null) {

                        msg.sendSticker(msg.message().sticker().fileId());

                    }

                }

            }

        }

    }

    int parseUsers(LinkedList<Msg> msgs) {

        HashSet<UserData> users = new HashSet<>();

        for (Msg msg : msgs) {

            users.add(msg.from());

        }

        return users.size();

    }

}
