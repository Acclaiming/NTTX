package io.kurumi.ntt.spam;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.utils.Markdown;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class TwiREC extends JSONObject {

    public static final String KEY = "NTT_REC";
    public static HashMap<Long, TwiREC> cache = new HashMap<>();

    static {

        Map<String, String> all = BotDB.jedis.hgetAll(KEY);

        for (Map.Entry<String, String> tag : all.entrySet()) {

            Long id = Long.parseLong(tag.getKey());

            cache.put(id, new TwiREC(id, tag.getValue()));

        }

    }

    public Long accountId;
    public String screenName;
    public String displayName;
    public LinkedHashMap<SpamTag, String> tags = new LinkedHashMap<>();

    public TwiREC() {
    }

    public TwiREC(Long id, String json) {

        super(json);

        accountId = id;

        screenName = getStr("screen_name");

        displayName = getStr("display_name");

        JSONObject tagMap = getJSONObject("tags");

        if (tagMap != null) {

            for (String tag : tagMap.keySet()) {

                Long tagId = Long.parseLong(tag);

                tags.put(SpamTag.get(tagId), tagMap.getStr(tag));

            }

        }

    }

    public static LinkedList<TwiREC> getAll() {

        return new LinkedList<TwiREC>(cache.values());

    }

    public static TwiREC get(Long id) {

        return cache.containsKey(id) ? cache.get(id) : null;

    }

    public String url() {

        return "https://twitter.com/" + screenName;

    }

    public String nameHtml() {

        return Markdown.toHtml(nameMarkdown());

    }

    public String nameMarkdown() {

        return "[" + displayName + "](" + url() + ")";

    }

    public void save() {

        put("screen_name", screenName);
        put("display_name", displayName);

        JSONObject tagMap = new JSONObject();

        for (SpamTag tag : tags.keySet()) {

            tagMap.put(tag.id.toString(), tags.get(tag));

        }

        put("tags", tagMap);

        BotDB.jedis.hset(KEY, accountId.toString(), toString());

    }

}
