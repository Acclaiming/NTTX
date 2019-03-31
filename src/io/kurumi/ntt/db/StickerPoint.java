package io.kurumi.ntt.db;

import cn.hutool.json.JSONObject;

public class StickerPoint extends JSONObject {

    public StickerSet set;
    public int index;

    public String fileId;
    public String emoji;

    public StickerPoint(StickerSet set, int index, JSONObject json) {

        super(json);

        this.set = set;
        this.index = index;

        this.fileId = getStr("f");
        this.emoji = getStr("e");

    }

    public StickerPoint(StickerSet set, int index, com.pengrad.telegrambot.model.Sticker sticker) {

        this.set = set;
        this.index = index;

        this.fileId = sticker.fileId();
        this.emoji = sticker.emoji();

        put("f", fileId);

        put("e", emoji);

    }


}
