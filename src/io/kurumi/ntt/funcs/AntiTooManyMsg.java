package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import java.util.LinkedList;

public class AntiTooManyMsg extends Fragment {

    HashMap<UserData,HashMap<Long,LinkedList<Long>>> cache = new HashMap<>();

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        long now = System.currentTimeMillis();

        long chatId = msg.chatId();

        HashMap<Long, LinkedList<Long>> userCache  = cache.get(user);

        if (userCache == null) {

            userCache = new HashMap<>();

        }

        LinkedList<Long> messages = userCache.get(chatId);

        if (messages == null) {

            messages = new LinkedList<>();

        }

        messages.add(now);

        while (messages.getLast() -  messages.getFirst() > 10 * 1000) {

            messages.removeFirst();

            // remove old cache (10s)

        }

        if (messages.size() > 10) {

            // do some restrict

        }
        
        userCache.put(chatId,messages);
        
        cache.put(user,userCache);
        
        // save cache

        return false;

    }



}
