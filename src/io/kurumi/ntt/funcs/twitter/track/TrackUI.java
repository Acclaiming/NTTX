package io.kurumi.ntt.funcs.twitter.track;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.utils.*;
import java.util.*;

public class TrackUI extends TwitterFunction {

    public static Data<TrackSetting> data = new Data<TrackSetting>(TrackSetting.class);
    
    public static class TrackSetting {
        
        public long id;
        
        public boolean followers = false;
        public boolean followingInfo = false;
        public boolean followersInfo = false;
        
    }
    
    @Override
    public void functions(LinkedList<String> names) {
       
        names.add("track");
        
    }
    
    final String POINT_SWITCH_FOLLOWERS = "tr|f";

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,final TAuth account) {
       
        msg.send("一个监听的设置... (做成按钮UI了 (❁´▽`❁)").buttons(makeSettings(user.id,account)).exec();
        
    }
    
    ButtonMarkup makeSettings(long userId,final TAuth account) {
        
        final TrackSetting setting = data.containsId(userId) ? data.getById(userId) : new TrackSetting();
        
        return new ButtonMarkup() {{


                newButtonLine((setting.followers ? "「 关闭" : "「 开启") + " 关注者监听 」",cdata(POINT_SWITCH_FOLLOWERS,account.id.toString()));
                newButtonLine((setting.followers ? "「 关闭" : "「 开启") + " 关注者监听 」",cdata(POINT_SWITCH_FOLLOWERS,account.id.toString()));


            }};
        
    }

    @Override
    public void onCallback(UserData user,Callback callback,String point,CData data) {
    }
    
}
