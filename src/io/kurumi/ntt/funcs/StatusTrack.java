package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.track.FollowerTrackTask;
import io.kurumi.ntt.twitter.TAuth;

public class StatusTrack extends Fragment {

    public static StatusTrack INSTANCE = new StatusTrack();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {
        
        if (!msg.isCommand()) return false;      
        
        switch (msg.command()) {
            
            case "tstart" : track(user,msg,true);break;
            case "tstop" : track(user,msg,false);break;
                
            default : return false;
            
        }
       
        return true;
        
    }
    
    void track(UserData user,Msg msg,boolean start) {
        
        if (!msg.isPrivate()) {

            msg.send("如果乃没有发送过信息给BOT 或者停用了BOT,BOT将无法发送关注者历史给乃 :)").exec();

        }
        
        if (!TAuth.exists(user)) {
            
            msg.send("你没有认证Twitter账号 :)").exec();
            
            return;
            
        }
        
        if (FollowerTrackTask.enable.containsKey(user.idStr)) {
            
            if (start) {
                
                msg.send("无需重复开启 :)").exec();
                
            } else {
                
                FollowerTrackTask.enable.remove(user.idStr);
                FollowerTrackTask.save();
                BotDB.setJSONArray("cache","track/" + user.idStr,null);
                
                msg.send("已关闭 :)").exec();
                
            }
            
        } else {
        
        if (start) {
        
        FollowerTrackTask.enable.put(user.idStr,true);
        FollowerTrackTask.save();
        
            msg.send("已开启 :)").exec();
            
        
        } else {
            
            msg.send("你没有开启 :)").exec();
            
        }
        
        }
        
        
    }
    
}
