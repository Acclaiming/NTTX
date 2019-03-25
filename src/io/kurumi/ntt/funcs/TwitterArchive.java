package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.twitter.archive.StatusArchive;
import io.kurumi.ntt.twitter.TAuth;
import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;

public class TwitterArchive extends Fragment {

    public static TwitterArchive INSTANCE = new TwitterArchive();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {
        
        if (!msg.isCommand()) return false;
        
        switch (msg.command()) {
            
            case "status" : statusArchive(user,msg);break;
            
            default : return false;
            
        }
        
        return true;
        
    }
    
    void statusArchive(UserData user,Msg msg) {
        
        if (msg.params().length != 1) {
            
            msg.send("用法 /status <推文链接|ID>").exec();
            
            return;
            
        }
        
        String input = msg.params()[0];
        
        Long statusId = -1L;
        
        if (NumberUtil.isNumber(input)) {
            
            statusId = NumberUtil.parseLong(input);
            
        } else if (input.contains("twitter.com/")) {
            
            input = StrUtil.subAfter(input,"status/",true);
            
            if (input.contains("?")) {
                
                input = StrUtil.subBefore(input,"?",false);
                
            }
            
            if (NumberUtil.isNumber(input))  {
                
                statusId = NumberUtil.parseLong(input);
                
            }
            
        }
        
        if (statusId == -1L) {
            
            msg.send("用法 /status <推文链接|ID>").exec();
            
            return;
            
        }
        
        if (StatusArchive.INSTANCE.exists(statusId)) {
            
            msg.send("存档存在 :)").exec();
            
            msg.send(StatusArchive.INSTANCE.get(statusId).toMarkdown()).markdown().exec();
            
        } else if (TAuth.exists(user)) {
            
            msg.send("正在拉取 :)").exec();
            
            Twitter api = TAuth.get(user).createApi();

            try {
                
                Status status = api.showStatus(statusId);
                
                StatusArchive newStatus = StatusArchive.INSTANCE.getOrNew(statusId);

                newStatus.read(status);
                
                newStatus.save();
                
                msg.send("已存档 :)").exec();
                
                msg.send(newStatus.toMarkdown()).markdown().exec();
              
            } catch (TwitterException e) {
                
                msg.send("存档失败 :( 推文还在吗？是锁推推文吗？").exec();
                
            }

        } else {
            
            msg.send("存档不存在 :( 乃没有认证账号 无法通过API读取推文... 请使用 /tauth 认证 ( ⚆ _ ⚆ )").exec();
            
        }
        
    }
    
}
