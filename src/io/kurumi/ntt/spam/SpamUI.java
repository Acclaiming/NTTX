package io.kurumi.ntt.spam;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.disc.DUser;
import io.kurumi.ntt.disc.DExApi;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.utils.CData;
import io.kurumi.ntt.disc.TAuth;

public class SpamUI extends Fragment {

    final String split = "----------------------------------";

    final String POINT_BACK = "s|b";

    final String POINT_PUBLIC_TAGS = "s|p";

    final String POINT_NEW = "s|n";
    final String POINT_INPUT_TAG_NAME = "s|it";

    final String POINT_TAG = "s|t";

    @Override
    public boolean onPrivMsg(UserData user, Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.commandName()) {

                case "/spam" : spamUI(user, msg, false);break;

                default : return false;

        }

        return true;

    }

    @Override
    public boolean onPoiPrivMsg(UserData user, Msg msg, CData point) {

        switch (point.getPoint()) {

                case POINT_NEW : onTagName(user, msg);break;

                default : return false;

        }

        return true;

    }

    DUser context(UserData user,Msg msg) {
        
        Integer userId = DExApi.getUserIdByTelegram(user.userName);

        if (userId == null) {

            msg.delete();
            
            msg.send("还没有绑定临风社账号 ~", "请在设置 - 个人信息中设置当前TelegramId >_<").exec();
            
            return null;

        }
        
        return DUser.get(userId);
        
    }

    void spamUI(UserData user, Msg msg, boolean edit) {

        if (context(user,msg) == null) return;

        (edit ? msg.edit(split) : msg.send(split))

            .buttons(new ButtonMarkup() {{

                    newButtonLine("「 公共分类 」", POINT_PUBLIC_TAGS);

                    // newButtonLine("分享个人列表", POINT_PUBLIC_TAGS);

                    // TODO

                    newUrlButtonLine("「 联封 | 论坛 」", "https://disc.kurumi.io/c/spam");

                    newUrlButtonLine("「 公式 | 闲聊 」", "https://t.me/joinchat/H5gBQ1N2Mx5gf3Jm1e6RgQ");

                }}).exec();

    }

    @Override
    public boolean onCallback(UserData user, Callback callback) {

        switch (callback.data.getIndex()) {

                case POINT_BACK : spamUI(user, callback, true);break;
                case POINT_PUBLIC_TAGS : publicTags(user, callback, true);break;
                case POINT_NEW : newTag(user, callback);break;
                case POINT_TAG : showTag(user,callback,Long.parseLong(callback.data.getIndex()),true);break;

                default : return false;

        }

        return true;

    }

    void publicTags(UserData user, Msg msg, boolean edit) {

        final DUser du = context(user,msg);
        
        if (du == null) return;

        (edit ? msg.edit(split) : msg.send(split))
            .buttons(new ButtonMarkup() {{

                    newButtonLine("「 << 返回主页 」", POINT_BACK);

                    if (du.moderator || du.admin) {

                        newButtonLine("「 ＋ 新建分类 」", POINT_NEW);

                    }

                    for (SpamTag tag : SpamTag.INSTANCE.all()) {

                        newButtonLine("「 " + tag.name + " 」", POINT_TAG, tag.id.toString());
                        
                    }

                }}).exec();
                
                

    }

    void newTag(UserData user, Callback callback) {

        DUser du = context(user,callback);
        
        if (du == null) return;
        
        if (!du.moderator && !du.admin) {
            
            callback.alert("Failed...");
            
            publicTags(user,callback,true);
            
            return;
            
        }
        
        callback.delete();

        callback.text("好");

        callback.send("现在发送新分类的名称 :").exec();

        user.point(cdata(POINT_NEW));

    }

    void onTagName(UserData user, Msg msg) {

        DUser du = context(user,msg);

        if (du == null) return;
        
        user.point(null);

        String tagName = msg.text();

        SpamTag newTag = SpamTag.INSTANCE.newObj();

        newTag.name = tagName;

        newTag.save();

        msg.send("创建成功 现在返回分类 ~").exec();

        publicTags(user, msg, false);

    }

    void showTag(UserData user, Msg msg, Long id, boolean edit) {

        Integer userId = DExApi.getUserIdByTelegram(user.userName);

        if (userId == null) {

            msg.delete();
            msg.send("还没有绑定临风社账号 ~", "请在设置 - 个人信息中设置当前TelegramId >_<").exec();

        }

        final DUser du = DUser.get(userId);

        final TAuth auth = TAuth.get(du);

        if (auth == null) {

            msg.delete();
            msg.send("还没有认证Twitter账号 ~", "请在设置中认证Twitter账号 >_<").exec();

        }
        
        StringBuilder tagContent = new StringBuilder(split);

        SpamTag tag = SpamTag.INSTANCE.get(id);
        
        tagContent.append("「 分类 | ").append(tag.name).append(" | 已").append(tag.enable.contains(auth.accountId) ? "启用" : "禁用").append(" 」");

        tagContent.append("\n\n").append(tag.desc);
        
        (edit ? msg.edit(tagContent.toString()) : msg.send(tagContent.toString()))

            .buttons(new ButtonMarkup() {{

                newButtonLine()
                .newButton("

                }}).exec();

    }

}
