package io.kurumi.ntt.db;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.BotMain;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import cn.hutool.json.JSONArray;
import java.util.HashMap;
import java.util.Map;
import io.kurumi.ntt.utils.BotLog;

public class StickerSet extends JSONObject {

    public static final String KEY = "NTT_STICKER_SETS";

    public static HashMap<String,StickerSet> cache = new HashMap<>();

    public static StickerSet get(String name) {

        if (cache.containsKey(name)) return cache.get(name);

        String data = BotDB.jedis.hget(KEY, name);

        if (data == null) {

            StickerSet set = new StickerSet(name);

            if (!set.refresh()) {

                BotLog.debug("尝试获取不存在的贴纸集 : " + name);
                
                return null;

            }
            
            cache.put(name,set);
            
            return set;

        } else {

            StickerSet set = new StickerSet(name, data);

            cache.put(name, set);

            return set;

        }

    }


    public String name;
    public String title;

    public Boolean containsMasks;

    public LinkedList<StickerPoint> stickers = new LinkedList<>();

    public StickerSet(String name) {

        this.name = name;
        
    }

    public StickerSet(String name, String json) {

        super(json);

        this.name = name;

        load();

    }
    
    public StickerPoint get(int point) {
        
        return stickers.get(point - 1);
        
    }
    
    public void load() {
        
        title = getStr("t");

        containsMasks = getBool("c");

        JSONArray stickerArray = getJSONArray("s");

        for (int index = 0;index < stickerArray.size();index ++) {

            stickers.add(new StickerPoint(this, index + 1, stickerArray.getJSONObject(index)));

        }
        
    }

    public boolean refresh() {
        
        GetStickerSetResponse resp = BotMain.INSTANCE.bot().execute(new GetStickerSet(name));

        if (!resp.isOk()) {

            return false;

        }

        com.pengrad.telegrambot.model.StickerSet set = resp.stickerSet();

        title = set.title();

        containsMasks = set.containsMasks();

        stickers.clear();

        for (int index = 0;index < set.stickers().length;index ++) {

            com.pengrad.telegrambot.model.Sticker sticker = set.stickers()[set.stickers().length -1 - index ];

            stickers.add(new StickerPoint(this, set.stickers().length - index, sticker));

        }

        save();

        return true;

    }

    public void save() {

        put("t", title);

        put("c", containsMasks);

        put("s", stickers);

        BotDB.jedis.hset(KEY, name, toString());

    }
   
    public void override() {
        
        putAll(StickerSet.get(name));

        cache.put(name,this);
        
    }

}
