package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.utils.T;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.CData;

public class StatusUI extends Fragment {

    public static StatusUI INSTANCE = new StatusUI();
    
    final String POINT_SHOW = "s|s";
    
    @Override
    public boolean onCallback(UserData user,Callback callback) {
        
        switch (callback.data.getPoint()) {
            
            case POINT_SHOW : show(user,callback);break;
            
            default : return false;
            
        }
        
        return true;
    }

    public ButtonMarkup makeShowButton(final long statusId) {
        
        return new ButtonMarkup() {{
            
                CData show = cdata(POINT_SHOW);
                
                show.put("s",statusId);

                newButtonLine("「 展开上下文 」",show);
            
        }};
        
    }
    
    void show(UserData user,Callback callback) {
        
        Long statusId = callback.data.getLong("s");
        
        StatusArchive archive = BotDB.getStatus(statusId);

        if (archive == null) {
            
            callback.alert("无记录...");
            
            return;
            
        }
        
        callback.text("好 ~");
        
        callback.edit(archive.toHtml()).exec();
        
    }


}
