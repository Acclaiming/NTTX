package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.track.FTTask;
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

            msg.send("如果乃没有发送过信息给BOT 或者停用了BOT,BOT将无法发送关注者历史给乃 :)").failed();

        }

        if (!TAuth.exists(user)) {

            msg.send("你没有认证Twitter账号 :)").publicFailed();

            return;

        }

        if (FTTask.enable.containsKey(user.id.toString())) {

            if (start) {

                msg.send("无需重复开启 :)").publicFailed();

            } else {

                FTTask.enable.remove(user.id.toString());
                FTTask.save();
                SData.setJSONArray("cache","track/" + user.id,null);

                msg.send("已关闭 :)").exec();

            }

        } else {

            if (start) {

                FTTask.enable.put(user.id.toString(),true);
                FTTask.save();

                msg.send("已开启 :)").exec();


            } else {

                msg.send("你没有开启 :)").publicFailed();

            }

        }


    }

}
