package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.utils.T;

public class FollowUI extends Fragment {

    final String POINT_FOLLOW = "f|f";
    
    @Override
    public boolean onCallback(UserData user,Callback callback) {
        
        switch (callback.data.getPoint()) {
            
            case POINT_FOLLOW : follow(user,callback);break;
            
            default : return false;
            
        }
        
        return true;
    }

    void follow(UserData user,Callback callback) {
        
        if (T.checkUserNonAuth(user,callback)) return;
        
        
        
    }


}
