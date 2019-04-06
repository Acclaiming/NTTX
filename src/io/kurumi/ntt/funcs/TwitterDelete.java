package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import com.pengrad.telegrambot.model.Document;
import java.io.File;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.utils.T;
import java.util.LinkedList;
import java.util.HashMap;
import io.kurumi.ntt.twitter.TAuth;
import twitter4j.AsyncTwitter;

public class TwitterDelete extends Fragment {

    public static TwitterDelete INSTANCE = new TwitterDelete();
    
    final String POINT_DELETE_LIKES = "d|l";

    @Override
    public boolean onNPM(UserData user,Msg msg) {

        if (msg.doc() == null) return false;

        switch (msg.doc().fileName()) {

            case "like.js" : deleteLikes(user,msg);break;

            default : return false;

        }

        return true;

    }

    @Override
    public boolean onPPM(UserData user,Msg msg) {

        switch (user.point.getPoint()) {

            case POINT_DELETE_LIKES : comfirmDeleteLikes(user,msg);break;

            default : return true;

        }

        return true;

    }

    void deleteLikes(UserData user,Msg msg) {

        if (T.checkUserNonAuth(user,msg)) return;

        msg.send("输入 任意内容 来删除所有的推文喜欢 ","使用 /cancel 取消 注意 : 开始后不可撤销").exec();

        user.point = cdata(POINT_DELETE_LIKES);

        user.point.setIndex(msg.doc().fileId());

        user.savePoint();

    }

    void comfirmDeleteLikes(final UserData user,final Msg msg) {

        try {

            msg.sendTyping();

            File likejs = getFile(user.point.getIndex());

            String content = FileUtil.readUtf8String(likejs);

            content = StrUtil.subAfter(content,"=",false);

            JSONArray array  = new JSONArray(content);

            final LinkedList<Long> pedding = new LinkedList<>();

            for (int index = 0;index > array.size();index ++) {

                pedding.add(array.getByPath(index + ".like.tweetId",Long.class));

            }

            msg.send("解析成功 : " + pedding.size() + "个喜欢 正在删除...").exec();

            final AsyncTwitter api = TAuth.get(user).createAsyncApi();

            new Thread("NTT Twitter Likes Delete Thread") {

                @Override
                public void run() {

                    for (long id : pedding) {

                        api.destroyFavorite(id);

                    }

                    msg.send("喜欢删除完成 ~").exec();

                }

            }.start();

            user.point = null;

        } catch (Exception err) {

            msg.send("解析失败..." + err).exec();

        }


    }

}
