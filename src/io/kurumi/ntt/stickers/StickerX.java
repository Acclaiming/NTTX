package io.kurumi.ntt.stickers;

import java.util.HashMap;
import java.util.LinkedList;
import io.kurumi.ntt.db.StickerPoint;
import cn.hutool.core.util.RandomUtil;

public class StickerX {
    
    // TODO 整理
    
    public static LinkedList<StickerPoint> 开心 = new LinkedList<>();
    
    public static StickerPoint 开心() {
        
        return RandomUtil.randomEle(开心);
        
    }
    
    public static LinkedList<StickerPoint> 害羞 = new LinkedList<>();

    public static StickerPoint 害羞() {

        return RandomUtil.randomEle(害羞);

    }
    
    public static LinkedList<StickerPoint> 点赞 = new LinkedList<>();

    public static StickerPoint 点赞() {

        return RandomUtil.randomEle(点赞);

    }
    
    public static LinkedList<StickerPoint> 期待 = new LinkedList<>();

    public static StickerPoint 期待() {

        return RandomUtil.randomEle(期待);

    }
   
    public static LinkedList<StickerPoint> 发情 = new LinkedList<>();

    public static StickerPoint 发情() {

        return RandomUtil.randomEle(发情);

    }
    
    public static LinkedList<StickerPoint> 无奈 = new LinkedList<>();

    public static StickerPoint 无奈() {

        return RandomUtil.randomEle(无奈);

    }
    
    public static LinkedList<StickerPoint> 难受 = new LinkedList<>();

    public static StickerPoint 难受() {

        return RandomUtil.randomEle(难受);

    }
    
    static {
        
        开心.add(DVANG.开心);
        开心.add(DVANG.开心2);
        开心.add(DVANG.嘲笑);
        
        害羞.add(DVANG.害羞);
        害羞.add(DVANG.害羞2);
        
        点赞.add(DVANG.点赞);
        
        期待.add(DVANG.期待);
        
        发情.add(DVANG.发情);
        
    }
    
}
