package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import com.pengrad.telegrambot.request.CreateNewStickerSet;

public class StickerManage extends Fragment {

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if ("sticker".equals(msg.command())) {

            if (!msg.isPrivate()) {

                msg.send("请使用私聊 :)").publicFailed();

            } else {

                sendMain(user,msg,false);

            }

            return true;

        }

        return false;

    }
    
    

    void sendMain(UserData user,Msg msg,boolean edit) {

        msg.sendTyping();

        msg.sendOrEdit(edit,"管理或创建你的贴纸集 :)")
        .buttons(new ButtonMarkup() {{
            
            
        }}).exec();

    }

}
