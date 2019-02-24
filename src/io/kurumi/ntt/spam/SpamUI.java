package io.kurumi.ntt.spam;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.disc.DUser;
import io.kurumi.ntt.disc.DExApi;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.Callback;

public class SpamUI extends Fragment {

    final String split = "----------------------------------";
    
    @Override
    public boolean onPrivMsg(UserData user, Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.commandName()) {

                case "/spam" : spamUI(user, msg,false);break;

                default : return false;

        }

        return true;

    }

    final String POINT_BACK = "s|b";
    final String POINT_PUBLIC_TAGS = "s|p";
    final String PO
    
    void spamUI(UserData user, Msg msg, boolean edit) {

        Integer userId = DExApi.getUserIdByTelegram(user.userName);

        if (userId == null) {

            msg.send("还没有绑定临风社账号 ~", "请在设置 - 个人信息中设置当前TelegramId >_<").exec();

        }

        (edit ? msg.edit(split) : msg.send(split))

            .buttons(new ButtonMarkup() {{

                    newButtonLine("「 公共分类 」", POINT_PUBLIC_TAGS);
                    
                    // newButtonLine("分享个人列表", POINT_PUBLIC_TAGS);
                    
                    // TODO
                    
                    newUrlButtonLine("「 联封 | 论坛 」","https://disc.kurumi.io/c/spam");
                    
                    newUrlButtonLine("「 公式 | 闲聊 」","https://t.me/joinchat/H5gBQ1N2Mx5gf3Jm1e6RgQ");

                }}).exec();

    }

    @Override
    public boolean onCallback(UserData user, Callback callback) {
        
        switch (callback.data.getIndex()) {
            
            case POINT_BACK : spamUI(user,callback,true);break;
            case POINT_PUBLIC_TAGS : publicTags(user,callback);break;
            
            default : return false;
            
        }
        
        return true;
        
    }

    void publicTags(UserData user, Callback callback) {
       
        Integer userId = DExApi.getUserIdByTelegram(user.userName);

        if (userId == null) {

            callback.delete();
            callback.send("还没有绑定临风社账号 ~", "请在设置 - 个人信息中设置当前TelegramId >_<").exec();

        }
        
        final DUser du = DUser.get(userId);
        
        callback.edit(split)
        .buttons(new ButtonMarkup() {{
            
            newButtonLine("「 << 返回主页 」",POINT_BACK);
            
            if (du.moderator || du.admin) {
                
                newButtonLine("「 ＋ 新建分类 」",POINT_NEW);
                
            }
            
            for (SpamTag tag : SpamTag.INSTANCE.all()) {
                
                
                
            }
            
        }}).exec();
        
    }

}
