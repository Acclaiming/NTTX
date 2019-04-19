package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.track.FTTask;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.twitter.stream.*;

public class FollowersTrack extends Fragment {

    public static FollowersTrack INSTANCE = new FollowersTrack();

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;      

        switch (msg.command()) {

            case "tstart" : track(user,msg,true);break;
            case "tstop" : track(user,msg,false);break;

			case "sstart" : stream(user,msg,true);break;
			case "sstop" : stream(user,msg,false);break;

            default : return false;

        }

        return true;

    }

    void track(UserData user,Msg msg,boolean start) {

        if (!msg.isPrivate()) {

            msg.send("如果乃没有发送过信息给BOT 或者停用了BOT,BOT将无法发送关注者历史给乃 :)").failed();

        }

        if (!TAuth.exists(user.id)) {

            msg.send("你没有认证Twitter账号 :)").publicFailed();

            return;

        }

        if (FTTask.enable.contains(user.id)) {

            if (start) {

                msg.send("无需重复开启 :)").publicFailed();

            } else {

                FTTask.enable.remove(user.id.toString());
                FTTask.save();

                msg.send("已关闭 :)").exec();

            }

        } else {

            if (start) {

                FTTask.enable.add(user.id);
                FTTask.save();

                msg.send("已开启 :)").exec();


            } else {

                msg.send("你没有开启 :)").publicFailed();

            }

        }


    }

	void stream(UserData user,Msg msg,boolean start) {

        if (!msg.isPrivate()) {

            msg.send("如果乃没有发送过信息给BOT 或者停用了BOT,BOT将无法发送推文给乃 :)").failed();

        }

        if (!TAuth.exists(user.id)) {

            msg.send("你没有认证Twitter账号 :)").publicFailed();

            return;

        }

        if (SubTask.enable.contains(user.id.longValue())) {

            if (start) {

                msg.send("无需重复开启 :)").publicFailed();

            } else {

				synchronized (SubTask.enable) {

					SubTask.enable.remove(user.id.longValue());
					SubTask.save();
                    SubTask.needReset.set(true);
					SubTask.stop(user.id);

					msg.send("已关闭 :)").exec();

				}

            }

        } else {

            if (start) {

				synchronized (SubTask.enable) {

					SubTask.enable.put(user.id.longValue());
					SubTask.save();

                    SubTask.needReset.set(true);
                    
					msg.send("已开启 :)").exec();

				}


            } else {

                msg.send("你没有开启 :)").publicFailed();

            }

        }


    }


}
