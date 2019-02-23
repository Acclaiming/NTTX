package io.kurumi.ntt.spam;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.IDFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SpamTag extends JSONObject {

    public static HashMap<Long, SpamTag> cache = new HashMap<>();

    static {

        Map<String, String> all = BotDB.jedis.hgetAll(KEY);

        for (Map.Entry<String, String> tag : all.entrySet()) {

            Long id = Long.parseLong(tag.getKey());

            cache.put(id, new SpamTag(id, tag.getValue()));

        }

    }

    public Long id;
    public String name;
    public String desc = "无";
    public Boolean defaultOpen = false;
    public LinkedList<Integer> disable = new LinkedList<>();

    public SpamTag() {

        id = -1L;

    }

    public SpamTag(Long id, String json) {

        super(json);

        this.id = id;

        name = getStr("name");

        desc = getStr("desc");

        defaultOpen = getBool("default_open");

        JSONArray disableArray = getJSONArray("disable");

        for (int index = 0; index < disableArray.size(); index++) {

            disable.add(disableArray.getInt(index));

        }

    }

    public static LinkedList<SpamTag> getAll() {

        return new LinkedList<SpamTag>(cache.values());

    }

    public static SpamTag get(String id) {

        for (SpamTag tag : cache.values()) {

            if (id.equals(tag.name)) {

                return tag;

            }

        }

        return null;

    }

    public static SpamTag get(Long id) {

        return cache.containsKey(id) ? cache.get(id) : null;

    }

    public void save() {

        put("name", name);
        put("desc", desc);

        put("default_open", defaultOpen);

        put("disable", disable);

        if (id == -1L) {

            id = IDFactory.nextId(KEY);

        }

        BotDB.jedis.hset(KEY, id.toString(), toString());

    }

}
