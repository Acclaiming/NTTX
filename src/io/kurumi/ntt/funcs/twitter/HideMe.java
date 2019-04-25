package io.kurumi.ntt.funcs.twitter;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.twitter.TAuth;

public class HideMe extends Fragment {

    public static HideMe INSTANCE = new HideMe();
    
    /*
    
    public static JSONArray hideList = LocalData.getJSONArray("data","hides",true);
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {
        
        if (!msg.isCommand()) return false;
        
        switch (msg.command()) {
            
            case "hide" : hide(user,msg);break;
            case "unhide" : unhide(user,msg);break;
            
            default : return false;
            
        }
        
        return true;
        
    }

    void hide(UserData user,Msg msg) {
     
        if (NTT.checkUserNonAuth(user,msg)) return;
        
        Long id = TAuth.get(user.id).accountId;

        if (hideList.contains(id)) {
            
            msg.send("无需重复开启 :)").publicFailed();
            
        } else {
            
            msg.send("乃的账号 " + TAuth.get(user.id).getFormatedNameHtml() + " 已经开启隐藏","其他用户将不会收到乃的账号更改 :)").html().exec();
            
            hideList.add(id);
            
            save();
            
        }
        
    }
    
    void unhide(UserData user,Msg msg) {

        if (NTT.checkUserNonAuth(user,msg)) return;

        Long id = TAuth.get(user.id).accountId;

        if (!hideList.contains(id)) {

            msg.send("你没有开启 :)").publicFailed();

        } else {

            msg.send("乃的账号 " + TAuth.get(user.id).getFormatedNameHtml()," 已经移除隐藏 :)").html().exec();

            hideList.remove(id);

            save();

        }

    }
    
    public void save() {
        
        LocalData.setJSONArray("data","hides",hideList);
        
    }
    
    */
    
}
