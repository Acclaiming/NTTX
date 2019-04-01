package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;

public class MusicSearch extends Fragment {

    // String apiUrl = "http://127.0.0.1:11212/";
    String apiUrlPublic = "https://napi.kurumi.io/";

    boolean usePublicApi = false;

    String searchId(String keywords) {

        HttpResponse resp = HttpUtil.createGet(apiUrlPublic + "/search")
            .form("keywords",keywords)
            .execute();

        if (resp.isOk()) {

            JSONObject data = new JSONObject(resp.body());
            
            if (data.getByPath("result.songCount",Integer.class) > 0) {
                
                JSONArray songs = data.getByPath("result.songs",JSONArray.class);
                
                for (int index = 0;index < songs.size();index ++) {}
                
            }
            
        }
        
        return null;

    }

    @Override
    public boolean onMsg(UserData user,Msg msg) {


        return super.onMsg(user,msg);
    }

}
